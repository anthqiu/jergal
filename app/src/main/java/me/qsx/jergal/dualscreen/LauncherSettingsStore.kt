package me.qsx.jergal.dualscreen

import android.content.Context
import android.net.Uri
import org.json.JSONObject

internal data class LauncherSettings(
    val romRootUri: Uri? = null,
    val romRootLabel: String? = null,
    val platformIndexUrl: String = LauncherSettingsStore.DEFAULT_PLATFORM_INDEX_URL,
)

internal data class SavedPlatformLaunchConfig(
    val platformShortname: String,
    val platformName: String,
    val playerName: String,
    val packageName: String,
    val activityName: String,
    val amStartArguments: String,
)

internal object LauncherSettingsStore {
    const val DEFAULT_PLATFORM_INDEX_URL =
        "https://raw.githubusercontent.com/inssekt/CocoonFE/main/platforms/index.json"

    private const val PREFS_NAME = "jergal_settings"
    private const val KEY_ROM_ROOT_URI = "rom_root_uri"
    private const val KEY_ROM_ROOT_LABEL = "rom_root_label"
    private const val KEY_PLATFORM_INDEX_URL = "platform_index_url"
    private const val KEY_PLATFORM_LAUNCH_CONFIGS = "platform_launch_configs"

    fun load(context: Context): LauncherSettings {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val romRootUri = preferences.getString(KEY_ROM_ROOT_URI, null)?.let(Uri::parse)
        val romRootLabel = preferences.getString(KEY_ROM_ROOT_LABEL, null)
        val platformIndexUrl = preferences.getString(KEY_PLATFORM_INDEX_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_PLATFORM_INDEX_URL
        return LauncherSettings(
            romRootUri = romRootUri,
            romRootLabel = romRootLabel,
            platformIndexUrl = platformIndexUrl,
        )
    }

    fun saveRomRoot(
        context: Context,
        uri: Uri,
        label: String?,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROM_ROOT_URI, uri.toString())
            .putString(KEY_ROM_ROOT_LABEL, label)
            .apply()
    }

    fun savePlatformIndexUrl(
        context: Context,
        url: String,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PLATFORM_INDEX_URL, url)
            .apply()
    }

    fun savePlatformLaunchConfigs(
        context: Context,
        configs: Collection<SavedPlatformLaunchConfig>,
    ) {
        val root = JSONObject()
        configs.forEach { config ->
            root.put(
                config.platformShortname,
                JSONObject()
                    .put("platformName", config.platformName)
                    .put("playerName", config.playerName)
                    .put("packageName", config.packageName)
                    .put("activityName", config.activityName)
                    .put("amStartArguments", config.amStartArguments),
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PLATFORM_LAUNCH_CONFIGS, root.toString())
            .apply()
    }

    fun loadPlatformLaunchConfigs(context: Context): Map<String, SavedPlatformLaunchConfig> {
        val rawJson = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PLATFORM_LAUNCH_CONFIGS, null)
            ?: return emptyMap()
        return runCatching {
            val root = JSONObject(rawJson)
            buildMap {
                root.keys().forEach { shortname ->
                    val entry = root.optJSONObject(shortname) ?: return@forEach
                    put(
                        shortname,
                        SavedPlatformLaunchConfig(
                            platformShortname = shortname,
                            platformName = entry.optString("platformName"),
                            playerName = entry.optString("playerName"),
                            packageName = entry.optString("packageName"),
                            activityName = entry.optString("activityName"),
                            amStartArguments = entry.optString("amStartArguments"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }
}
