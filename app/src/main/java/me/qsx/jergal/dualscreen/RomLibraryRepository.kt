package me.qsx.jergal.dualscreen

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.view.Display
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.absoluteValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.qsx.jergal.ui.theme.ThemeColorRole
import org.json.JSONArray
import org.json.JSONObject

internal data class RomLibraryState(
    val games: List<RetroGame> = emptyList(),
    val isLoading: Boolean = false,
    val romRootUri: Uri? = null,
    val romRootLabel: String? = null,
    val platformIndexUrl: String = LauncherSettingsStore.DEFAULT_PLATFORM_INDEX_URL,
    val libraryMessage: String = "Choose a ROM folder in Settings to scan your ROM library.",
    val syncMessage: String? = null,
    val platformSummaries: List<PlatformSummary> = emptyList(),
)

internal data class PlatformSummary(
    val platformShortname: String,
    val platformName: String,
    val romCount: Int,
    val emulatorName: String?,
    val status: String,
)

internal object RomLibraryRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    private val _state = MutableStateFlow(RomLibraryState(isLoading = true))
    private var initialized = false

    val state = _state.asStateFlow()

    fun ensureInitialized(context: Context) {
        synchronized(this) {
            if (initialized) {
                return
            }
            initialized = true
        }
        refresh(context)
    }

    fun refresh(
        context: Context,
        forceRemoteSync: Boolean = false,
    ) {
        val appContext = context.applicationContext
        scope.launch {
            refreshInternal(appContext, forceRemoteSync)
        }
    }

    suspend fun updateRomRoot(
        context: Context,
        uri: Uri,
        label: String?,
    ) {
        LauncherSettingsStore.saveRomRoot(context.applicationContext, uri, label)
        refreshInternal(context.applicationContext, forceRemoteSync = true)
    }

    suspend fun updatePlatformIndexUrl(
        context: Context,
        url: String,
    ) {
        LauncherSettingsStore.savePlatformIndexUrl(context.applicationContext, url)
        refreshInternal(context.applicationContext, forceRemoteSync = true)
    }

    fun launchGame(
        activity: Activity,
        game: RetroGame,
    ): Boolean {
        val romUri = game.romUri ?: return false
        val launchArgumentsTemplate = game.launchArgumentsTemplate ?: return false
        val packageName = game.launchPackageName ?: return false
        val intent = buildLaunchIntent(game, launchArgumentsTemplate) ?: return false
        val targets = DisplayTargetResolver.resolve(activity)
        val options = ActivityOptions.makeBasic().apply {
            setLaunchDisplayId(targets.topDisplayId)
        }
        return runCatching {
            activity.grantUriPermission(
                packageName,
                romUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.clipData = ClipData.newUri(activity.contentResolver, game.title, romUri)
            activity.startActivity(intent, options.toBundle())
        }.isSuccess
    }

    private suspend fun refreshInternal(
        context: Context,
        forceRemoteSync: Boolean,
    ) {
        refreshMutex.withLock {
            val settings = LauncherSettingsStore.load(context)
            _state.value = _state.value.copy(
                isLoading = true,
                romRootUri = settings.romRootUri,
                romRootLabel = settings.romRootLabel,
                platformIndexUrl = settings.platformIndexUrl,
            )

            val romRootUri = settings.romRootUri
            if (romRootUri == null) {
                DualScreenCoordinator.updateLibrary(emptyList())
                LauncherSettingsStore.savePlatformLaunchConfigs(context, emptyList())
                _state.value = RomLibraryState(
                    isLoading = false,
                    romRootUri = null,
                    romRootLabel = null,
                    platformIndexUrl = settings.platformIndexUrl,
                    libraryMessage = "Choose a ROM folder in Settings to scan your ROM library.",
                    syncMessage = null,
                    platformSummaries = emptyList(),
                )
                return
            }

            val rootDocument = resolveTreeRoot(context, romRootUri)
            if (rootDocument == null) {
                DualScreenCoordinator.updateLibrary(emptyList())
                LauncherSettingsStore.savePlatformLaunchConfigs(context, emptyList())
                _state.value = RomLibraryState(
                    isLoading = false,
                    romRootUri = romRootUri,
                    romRootLabel = settings.romRootLabel,
                    platformIndexUrl = settings.platformIndexUrl,
                    libraryMessage = "The selected ROM folder is no longer readable. Choose it again in Settings.",
                    syncMessage = null,
                    platformSummaries = emptyList(),
                )
                return
            }

            val localPlatforms = scanPlatformDirectories(context, rootDocument.uri)
            if (localPlatforms.isEmpty()) {
                DualScreenCoordinator.updateLibrary(emptyList())
                LauncherSettingsStore.savePlatformLaunchConfigs(context, emptyList())
                _state.value = RomLibraryState(
                    isLoading = false,
                    romRootUri = romRootUri,
                    romRootLabel = settings.romRootLabel ?: rootDocument.name,
                    platformIndexUrl = settings.platformIndexUrl,
                    libraryMessage = "No first-level platform folders with readable files were found under the selected ROM root.",
                    syncMessage = null,
                    platformSummaries = emptyList(),
                )
                return
            }

            val launcherPackages = loadLauncherApps(context.packageManager)
                .mapTo(linkedSetOf()) { it.packageName }
            val syncResult = syncPlatformConfigs(
                context = context,
                indexUrl = settings.platformIndexUrl,
                targetShortnames = localPlatforms.mapTo(linkedSetOf()) { it.shortname },
                forceRemoteSync = forceRemoteSync,
            )

            val games = mutableListOf<RetroGame>()
            val platformSummaries = mutableListOf<PlatformSummary>()
            val savedLaunchConfigs = mutableListOf<SavedPlatformLaunchConfig>()

            localPlatforms.forEach { platformDirectory ->
                val platformConfig = syncResult.platformConfigs[platformDirectory.shortname]
                val resolvedPlayer = resolveInstalledPlayer(platformConfig, launcherPackages)
                if (platformConfig != null && resolvedPlayer != null) {
                    savedLaunchConfigs += SavedPlatformLaunchConfig(
                        platformShortname = platformDirectory.shortname,
                        platformName = platformConfig.platformName,
                        playerName = resolvedPlayer.name,
                        packageName = resolvedPlayer.packageName,
                        activityName = resolvedPlayer.activityName,
                        amStartArguments = resolvedPlayer.amStartArguments,
                    )
                }

                val romFiles = platformConfig?.let {
                    filterRomFiles(
                        files = platformDirectory.files,
                        platformConfig = it,
                        resolvedPlayer = resolvedPlayer,
                    )
                }.orEmpty()

                val platformName = platformConfig?.platformName ?: platformDirectory.folderName
                val emulatorName = resolvedPlayer?.name
                val status = when {
                    platformConfig == null -> "No matching remote platform config was found."
                    romFiles.isEmpty() -> "No matching ROM files were found in this folder."
                    resolvedPlayer == null -> "No installed emulator matched this platform config."
                    else -> "Ready"
                }
                platformSummaries += PlatformSummary(
                    platformShortname = platformDirectory.shortname,
                    platformName = platformName,
                    romCount = romFiles.size,
                    emulatorName = emulatorName,
                    status = status,
                )

                if (platformConfig == null) {
                    return@forEach
                }

                games += romFiles.map { romFile ->
                    romFile.toRetroGame(
                        platformConfig = platformConfig,
                        resolvedPlayer = resolvedPlayer,
                    )
                }
            }

            val sortedGames = games.sortedBy { it.title.lowercase(Locale.ROOT) }
            LauncherSettingsStore.savePlatformLaunchConfigs(context, savedLaunchConfigs)
            DualScreenCoordinator.updateLibrary(sortedGames)

            val libraryMessage = when {
                sortedGames.isNotEmpty() -> {
                    val platformCount = platformSummaries.count { it.romCount > 0 }
                    "${sortedGames.size} games across $platformCount platforms."
                }
                else -> "No supported ROMs were found under the selected ROM root."
            }

            _state.value = RomLibraryState(
                games = sortedGames,
                isLoading = false,
                romRootUri = romRootUri,
                romRootLabel = settings.romRootLabel ?: rootDocument.name,
                platformIndexUrl = settings.platformIndexUrl,
                libraryMessage = libraryMessage,
                syncMessage = syncResult.message,
                platformSummaries = platformSummaries.sortedBy { it.platformName.lowercase(Locale.ROOT) },
            )
        }
    }
}

private data class TreeDocument(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
)

private data class LocalPlatformDirectory(
    val shortname: String,
    val folderName: String,
    val files: List<RomFileCandidate>,
)

private data class RomFileCandidate(
    val fileName: String,
    val title: String,
    val uri: Uri,
    val path: String?,
)

private data class PlatformIndex(
    val baseUri: String,
    val entries: Map<String, PlatformIndexEntry>,
)

private data class PlatformIndexEntry(
    val filename: String,
    val platformName: String,
    val platformShortname: String,
)

private data class PlatformConfig(
    val platformName: String,
    val platformShortname: String,
    val acceptedFilenameRegex: Regex?,
    val scraperSources: List<String>,
    val players: List<PlayerConfig>,
)

private data class PlayerConfig(
    val name: String,
    val acceptedFilenameRegex: Regex?,
    val amStartArguments: String,
)

private data class ResolvedPlayerConfig(
    val name: String,
    val acceptedFilenameRegex: Regex?,
    val amStartArguments: String,
    val packageName: String,
    val activityName: String,
)

private data class PlatformSyncResult(
    val platformConfigs: Map<String, PlatformConfig>,
    val message: String?,
)

private fun resolveTreeRoot(
    context: Context,
    treeUri: Uri,
): TreeDocument? {
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    return queryDocument(context, treeUri, documentUri)
}

private fun scanPlatformDirectories(
    context: Context,
    rootDocumentUri: Uri,
): List<LocalPlatformDirectory> {
    return queryChildren(context, rootDocumentUri, rootDocumentUri)
        .filter { it.isDirectory }
        .map { child ->
            LocalPlatformDirectory(
                shortname = child.name.lowercase(Locale.ROOT),
                folderName = child.name,
                files = scanRomFilesInPlatformFolder(context, rootDocumentUri, child.uri),
            )
        }
        .filter { it.files.isNotEmpty() }
}

private fun scanRomFilesInPlatformFolder(
    context: Context,
    treeUri: Uri,
    platformFolderUri: Uri,
): List<RomFileCandidate> {
    val stack = ArrayDeque<Uri>()
    val results = mutableListOf<RomFileCandidate>()
    stack.add(platformFolderUri)
    while (stack.isNotEmpty()) {
        val parent = stack.removeFirst()
        queryChildren(context, treeUri, parent).forEach { child ->
            if (child.name.startsWith(".")) {
                return@forEach
            }
            if (child.isDirectory) {
                stack.add(child.uri)
                return@forEach
            }
            results += RomFileCandidate(
                fileName = child.name,
                title = prettifyRomTitle(child.name),
                uri = child.uri,
                path = deriveDocumentPath(context, child.uri),
            )
        }
    }
    return results.sortedBy { it.title.lowercase(Locale.ROOT) }
}

private fun queryChildren(
    context: Context,
    treeUri: Uri,
    parentDocumentUri: Uri,
): List<TreeDocument> {
    val parentDocumentId = runCatching { DocumentsContract.getDocumentId(parentDocumentUri) }.getOrNull()
        ?: return emptyList()
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
    val projection = arrayOf(
        Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_MIME_TYPE,
    )
    return buildList {
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(Document.COLUMN_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(Document.COLUMN_MIME_TYPE)
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex) ?: continue
                val mimeType = cursor.getString(mimeTypeIndex)
                add(
                    TreeDocument(
                        uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
                        name = name,
                        isDirectory = mimeType == Document.MIME_TYPE_DIR,
                    )
                )
            }
        }
    }
}

private fun queryDocument(
    context: Context,
    treeUri: Uri,
    documentUri: Uri,
): TreeDocument? {
    val projection = arrayOf(
        Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_MIME_TYPE,
    )
    return context.contentResolver.query(documentUri, projection, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) {
            return@use null
        }
        val documentId = cursor.getString(cursor.getColumnIndexOrThrow(Document.COLUMN_DOCUMENT_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(Document.COLUMN_DISPLAY_NAME)) ?: return@use null
        val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Document.COLUMN_MIME_TYPE))
        TreeDocument(
            uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
            name = name,
            isDirectory = mimeType == Document.MIME_TYPE_DIR,
        )
    }
}

private fun syncPlatformConfigs(
    context: Context,
    indexUrl: String,
    targetShortnames: Set<String>,
    forceRemoteSync: Boolean,
): PlatformSyncResult {
    val cacheDirectory = File(context.filesDir, "platform-cache").apply { mkdirs() }
    val indexCacheFile = File(cacheDirectory, "index.json")

    var indexLoadedFromCache = false
    val indexText = if (!forceRemoteSync && indexCacheFile.exists()) {
        indexLoadedFromCache = true
        indexCacheFile.readText()
    } else {
        tryDownloadText(indexUrl)?.also { indexCacheFile.writeText(it) }
            ?: indexCacheFile.takeIf { it.exists() }?.readText()?.also {
                indexLoadedFromCache = true
            }
    }
        ?: return PlatformSyncResult(
            platformConfigs = emptyMap(),
            message = "The platform index could not be downloaded and no cached index was available.",
        )

    val platformIndex = runCatching { parsePlatformIndex(indexText) }.getOrElse {
        return PlatformSyncResult(
            platformConfigs = emptyMap(),
            message = "The platform index could not be parsed.",
        )
    }

    val platformConfigs = buildMap {
        targetShortnames.forEach { shortname ->
            val entry = platformIndex.entries[shortname] ?: return@forEach
            val configCacheFile = File(cacheDirectory, entry.filename)
            var configLoadedFromCache = false
            val configUrl = resolvePlatformConfigUrl(indexUrl, platformIndex.baseUri, entry.filename)
            val configText = if (!forceRemoteSync && configCacheFile.exists()) {
                configLoadedFromCache = true
                configCacheFile.readText()
            } else {
                tryDownloadText(configUrl)?.also { configCacheFile.writeText(it) }
                    ?: configCacheFile.takeIf { it.exists() }?.readText()?.also {
                        configLoadedFromCache = true
                    }
            }
            if (configText == null) {
                return@forEach
            }
            val parsedConfig = runCatching { parsePlatformConfig(configText) }.getOrNull() ?: return@forEach
            put(shortname, parsedConfig)
            if (configLoadedFromCache) {
                return@forEach
            }
        }
    }

    val message = when {
        indexLoadedFromCache -> "Platform configs were loaded from cached subscription data."
        platformConfigs.isEmpty() -> "No platform configs were available for the scanned folders."
        else -> "Platform configs are synchronized."
    }
    return PlatformSyncResult(
        platformConfigs = platformConfigs,
        message = message,
    )
}

private fun resolveInstalledPlayer(
    platformConfig: PlatformConfig?,
    launcherPackages: Set<String>,
): ResolvedPlayerConfig? {
    platformConfig ?: return null
    return platformConfig.players.firstNotNullOfOrNull { player ->
        val component = parseComponent(player.amStartArguments) ?: return@firstNotNullOfOrNull null
        if (component.packageName !in launcherPackages) {
            return@firstNotNullOfOrNull null
        }
        ResolvedPlayerConfig(
            name = player.name,
            acceptedFilenameRegex = player.acceptedFilenameRegex,
            amStartArguments = player.amStartArguments,
            packageName = component.packageName,
            activityName = component.className,
        )
    }
}

private fun filterRomFiles(
    files: List<RomFileCandidate>,
    platformConfig: PlatformConfig,
    resolvedPlayer: ResolvedPlayerConfig?,
): List<RomFileCandidate> {
    val playerRegexes = if (resolvedPlayer != null) {
        listOfNotNull(resolvedPlayer.acceptedFilenameRegex)
    } else {
        platformConfig.players.mapNotNull { it.acceptedFilenameRegex }
    }

    return files.filter { file ->
        val platformMatches = platformConfig.acceptedFilenameRegex?.matches(file.fileName) ?: true
        val playerMatches = if (playerRegexes.isEmpty()) {
            true
        } else {
            playerRegexes.any { it.matches(file.fileName) }
        }
        platformMatches && playerMatches
    }
}

private fun RomFileCandidate.toRetroGame(
    platformConfig: PlatformConfig,
    resolvedPlayer: ResolvedPlayerConfig?,
): RetroGame {
    val accentPair = accentRolesFor(platformConfig.platformShortname)
    val metadataSource = platformConfig.scraperSources.firstOrNull()
    val playerLabel = resolvedPlayer?.name ?: "Install a matching emulator"
    val heroSummary = buildString {
        append("ROM file: ")
        append(fileName)
        append(". ")
        append(
            if (resolvedPlayer != null) {
                "Launches with ${resolvedPlayer.name}."
            } else {
                "No installed emulator currently matches this platform."
            }
        )
        if (metadataSource != null) {
            append(" Metadata source configured: ")
            append(metadataSource)
            append('.')
        }
    }
    return RetroGame(
        id = "${platformConfig.platformShortname}:${uri}",
        title = title,
        logo = buildLogo(title),
        tagline = platformConfig.platformName,
        heroSummary = heroSummary,
        playerLabel = playerLabel,
        platformLabel = platformConfig.platformName,
        accentRole = accentPair.first,
        secondaryAccentRole = accentPair.second,
        romUri = uri,
        romPath = path,
        launchArgumentsTemplate = resolvedPlayer?.amStartArguments,
        launchPackageName = resolvedPlayer?.packageName,
        launchActivityName = resolvedPlayer?.activityName,
        metadataSources = platformConfig.scraperSources,
        isLaunchable = resolvedPlayer != null,
    )
}

private fun buildLaunchIntent(
    game: RetroGame,
    template: String,
): Intent? {
    val tokens = tokenizeAmStartArguments(template)
    var launchComponent: ComponentName? = null
    var action: String? = null
    var data: Uri? = null
    var mimeType: String? = null
    val categories = mutableListOf<String>()
    val stringExtras = linkedMapOf<String, String>()
    val booleanExtras = linkedMapOf<String, Boolean>()
    val intExtras = linkedMapOf<String, Int>()
    val stringArrayExtras = linkedMapOf<String, Array<String>>()
    var flags = Intent.FLAG_ACTIVITY_NEW_TASK

    var index = 0
    while (index < tokens.size) {
        when (tokens[index]) {
            "-n" -> {
                val componentSpec = tokens.getOrNull(index + 1)?.replaceLaunchPlaceholders(game)
                launchComponent = componentSpec?.let(::parseComponentName)?.let { parsed ->
                    ComponentName(parsed.packageName, parsed.className)
                }
                index += 2
            }
            "-a" -> {
                action = tokens.getOrNull(index + 1)?.replaceLaunchPlaceholders(game)
                index += 2
            }
            "-d" -> {
                val value = tokens.getOrNull(index + 1)?.replaceLaunchPlaceholders(game)
                data = value?.let(::parseDataArgument)
                index += 2
            }
            "-t" -> {
                mimeType = tokens.getOrNull(index + 1)?.replaceLaunchPlaceholders(game)
                index += 2
            }
            "-c" -> {
                tokens.getOrNull(index + 1)?.replaceLaunchPlaceholders(game)?.let(categories::add)
                index += 2
            }
            "-e", "--es" -> {
                val key = tokens.getOrNull(index + 1)
                val value = tokens.getOrNull(index + 2)?.replaceLaunchPlaceholders(game)
                if (key != null && value != null) {
                    stringExtras[key] = value
                }
                index += 3
            }
            "--ez" -> {
                val key = tokens.getOrNull(index + 1)
                val value = tokens.getOrNull(index + 2)?.replaceLaunchPlaceholders(game)?.toBooleanStrictOrNull()
                if (key != null && value != null) {
                    booleanExtras[key] = value
                }
                index += 3
            }
            "--ei" -> {
                val key = tokens.getOrNull(index + 1)
                val value = tokens.getOrNull(index + 2)?.replaceLaunchPlaceholders(game)?.toIntOrNull()
                if (key != null && value != null) {
                    intExtras[key] = value
                }
                index += 3
            }
            "--esa" -> {
                val key = tokens.getOrNull(index + 1)
                val value = tokens.getOrNull(index + 2)?.replaceLaunchPlaceholders(game)
                if (key != null && value != null) {
                    stringArrayExtras[key] = value.split(',')
                        .map(String::trim)
                        .filter(String::isNotBlank)
                        .toTypedArray()
                }
                index += 3
            }
            "--activity-clear-task" -> {
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TASK
                index += 1
            }
            "--activity-clear-top" -> {
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
                index += 1
            }
            "--activity-no-history" -> {
                flags = flags or Intent.FLAG_ACTIVITY_NO_HISTORY
                index += 1
            }
            "--activity-new-task" -> {
                flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
                index += 1
            }
            else -> {
                index += 1
            }
        }
    }

    return Intent().apply {
        component = launchComponent
        this.action = action ?: Intent.ACTION_VIEW
        if (data != null && mimeType != null) {
            setDataAndType(data, mimeType)
        } else {
            data?.let(::setData)
            mimeType?.let(::setType)
        }
        categories.forEach(::addCategory)
        stringExtras.forEach(::putExtra)
        booleanExtras.forEach(::putExtra)
        intExtras.forEach(::putExtra)
        stringArrayExtras.forEach(::putExtra)
        addFlags(flags)
    }.takeIf { it.component != null }
}

private fun tokenizeAmStartArguments(arguments: String): List<String> {
    return arguments
        .lineSequence()
        .flatMap { line -> line.trim().split(Regex("\\s+")).asSequence() }
        .filter { it.isNotBlank() }
        .toList()
}

private fun parseDataArgument(value: String): Uri {
    return if (value.startsWith("/")) {
        Uri.fromFile(File(value))
    } else {
        Uri.parse(value)
    }
}

private data class ParsedComponentName(
    val packageName: String,
    val className: String,
)

private fun parseComponent(
    amStartArguments: String,
): ParsedComponentName? {
    val tokens = tokenizeAmStartArguments(amStartArguments)
    val componentIndex = tokens.indexOf("-n")
    if (componentIndex < 0) {
        return null
    }
    return tokens.getOrNull(componentIndex + 1)?.let(::parseComponentName)
}

private fun parseComponentName(rawValue: String): ParsedComponentName? {
    val parts = rawValue.split('/', limit = 2)
    if (parts.size != 2) {
        return null
    }
    val packageName = parts[0]
    val className = if (parts[1].startsWith('.')) {
        packageName + parts[1]
    } else {
        parts[1]
    }
    return ParsedComponentName(
        packageName = packageName,
        className = className,
    )
}

private fun String.replaceLaunchPlaceholders(game: RetroGame): String {
    return replace("{file.uri}", game.romUri?.toString().orEmpty())
        .replace("{file.path}", game.romPath ?: game.romUri?.toString().orEmpty())
}

private fun parsePlatformIndex(rawJson: String): PlatformIndex {
    val root = JSONObject(rawJson)
    val platformEntries = buildMap {
        root.optJSONArray("platformList")?.forEachObject { entry ->
            val shortname = entry.optString("platformShortname")
            val filename = entry.optString("filename")
            if (shortname.isBlank() || filename.isBlank()) {
                return@forEachObject
            }
            put(
                shortname.lowercase(Locale.ROOT),
                PlatformIndexEntry(
                    filename = filename,
                    platformName = entry.optString("platformName"),
                    platformShortname = shortname.lowercase(Locale.ROOT),
                ),
            )
        }
    }
    return PlatformIndex(
        baseUri = root.optString("baseUri"),
        entries = platformEntries,
    )
}

private fun parsePlatformConfig(rawJson: String): PlatformConfig {
    val root = JSONObject(rawJson)
    val platform = root.optJSONObject("platform") ?: JSONObject()
    return PlatformConfig(
        platformName = platform.optString("name"),
        platformShortname = platform.optString("shortname").lowercase(Locale.ROOT),
        acceptedFilenameRegex = platform.optString("acceptedFilenameRegex").toRegexOrNull(),
        scraperSources = buildList {
            platform.optJSONArray("scraperSourceList")?.forEachString(::add)
        },
        players = buildList {
            root.optJSONArray("playerList")?.forEachObject { player ->
                val playerName = player.optString("name")
                val amStartArguments = player.optString("amStartArguments")
                if (playerName.isBlank() || amStartArguments.isBlank()) {
                    return@forEachObject
                }
                add(
                    PlayerConfig(
                        name = playerName,
                        acceptedFilenameRegex = player.optString("acceptedFilenameRegex").toRegexOrNull(),
                        amStartArguments = amStartArguments,
                    )
                )
            }
        },
    )
}

private fun String.toRegexOrNull(): Regex? {
    if (isBlank()) {
        return null
    }
    return runCatching { Regex(this, setOf(RegexOption.IGNORE_CASE)) }.getOrNull()
}

private fun JSONArray.forEachObject(block: (JSONObject) -> Unit) {
    for (index in 0 until length()) {
        optJSONObject(index)?.let(block)
    }
}

private fun JSONArray.forEachString(block: (String) -> Unit) {
    for (index in 0 until length()) {
        optString(index).takeIf { it.isNotBlank() }?.let(block)
    }
}

private fun resolvePlatformConfigUrl(
    indexUrl: String,
    baseUri: String,
    filename: String,
): String {
    if (baseUri.isNotBlank()) {
        val normalizedBase = if (baseUri.endsWith('/')) baseUri else "$baseUri/"
        return URL(URL(normalizedBase), filename).toString()
    }
    val normalizedIndexUrl = if (indexUrl.endsWith("/index.json")) {
        indexUrl.substringBeforeLast('/') + "/"
    } else {
        "$indexUrl/"
    }
    return URL(URL(normalizedIndexUrl), filename).toString()
}

private fun tryDownloadText(url: String): String? {
    return runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.instanceFollowRedirects = true
        connection.inputStream.bufferedReader().use { reader ->
            reader.readText()
        }
    }.getOrNull()
}

private fun prettifyRomTitle(fileName: String): String {
    val withoutExtension = fileName.substringBeforeLast('.', fileName)
    return withoutExtension
        .replace('_', ' ')
        .replace('.', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun buildLogo(title: String): String {
    val words = title.split(Regex("\\s+")).filter(String::isNotBlank)
    return when {
        words.isEmpty() -> "ROM"
        words.size == 1 -> words.first().take(10).uppercase(Locale.ROOT)
        else -> words.take(3).joinToString(" ") { it.take(4).uppercase(Locale.ROOT) }
    }
}

private fun accentRolesFor(shortname: String): Pair<ThemeColorRole, ThemeColorRole> {
    val accents = listOf(
        ThemeColorRole.Primary to ThemeColorRole.Secondary,
        ThemeColorRole.Secondary to ThemeColorRole.PrimaryContainer,
        ThemeColorRole.Tertiary to ThemeColorRole.SecondaryContainer,
        ThemeColorRole.PrimaryContainer to ThemeColorRole.Tertiary,
        ThemeColorRole.SecondaryContainer to ThemeColorRole.Primary,
        ThemeColorRole.TertiaryContainer to ThemeColorRole.Secondary,
    )
    return accents[shortname.hashCode().absoluteValue % accents.size]
}

private fun deriveDocumentPath(
    context: Context,
    documentUri: Uri,
): String? {
    if (!DocumentsContract.isDocumentUri(context, documentUri)) {
        return null
    }
    val documentId = runCatching { DocumentsContract.getDocumentId(documentUri) }.getOrNull() ?: return null
    val parts = documentId.split(':', limit = 2)
    if (parts.size != 2) {
        return null
    }
    val volume = parts[0]
    val relativePath = parts[1]
    val volumeRoot = if (volume.equals("primary", ignoreCase = true)) {
        "/storage/emulated/0"
    } else {
        "/storage/$volume"
    }
    return if (relativePath.isBlank()) {
        volumeRoot
    } else {
        "$volumeRoot/$relativePath"
    }
}
