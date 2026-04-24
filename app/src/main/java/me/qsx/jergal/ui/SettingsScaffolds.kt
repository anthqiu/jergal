package me.qsx.jergal.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.qsx.jergal.dualscreen.RomLibraryRepository

@Composable
internal fun LauncherSettingsScreen(activity: Activity) {
    val libraryState by RomLibraryRepository.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var indexUrlDraft by rememberSaveable(libraryState.platformIndexUrl) {
        mutableStateOf(libraryState.platformIndexUrl)
    }
    val treePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { selectedUri ->
        if (selectedUri == null) {
            return@rememberLauncherForActivityResult
        }
        runCatching {
            activity.contentResolver.takePersistableUriPermission(
                selectedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        val label = resolveTreeLabel(activity, selectedUri)
        coroutineScope.launch {
            RomLibraryRepository.updateRomRoot(
                context = activity,
                uri = selectedUri,
                label = label,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Choose a ROM root and control where platform definitions are synchronized from.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsSection(
                title = "ROM Library",
                body = libraryState.romRootLabel
                    ?: "No ROM root is configured yet.",
            ) {
                Button(
                    onClick = {
                        treePicker.launch(libraryState.romRootUri)
                    },
                ) {
                    Text("Choose ROM Folder")
                }
            }

            SettingsSection(
                title = "Platform Subscription",
                body = libraryState.syncMessage
                    ?: "The platform index is used to fetch config files for the folders under your ROM root.",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = indexUrlDraft,
                        onValueChange = { indexUrlDraft = it },
                        label = {
                            Text("Platform index URL")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = {
                                val normalizedUrl = indexUrlDraft.trim()
                                if (normalizedUrl.isNotBlank()) {
                                    coroutineScope.launch {
                                        RomLibraryRepository.updatePlatformIndexUrl(
                                            context = activity,
                                            url = normalizedUrl,
                                        )
                                    }
                                }
                            },
                        ) {
                            Text("Save and Sync")
                        }
                        TextButton(
                            onClick = {
                                RomLibraryRepository.refresh(
                                    context = activity,
                                    forceRemoteSync = true,
                                )
                            },
                        ) {
                            Text("Refresh Now")
                        }
                    }
                }
            }

            SettingsSection(
                title = "Detected Platforms",
                body = libraryState.libraryMessage,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                Column {
                    if (libraryState.isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = "Scanning ROM folders and syncing platform configs.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (!libraryState.isLoading && libraryState.platformSummaries.isEmpty()) {
                        Text(
                            text = "No platform folders are ready yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        )
                    }
                    libraryState.platformSummaries.forEach { summary ->
                        ListItem(
                            headlineContent = {
                                Text(summary.platformName)
                            },
                            supportingContent = {
                                Text(
                                    text = "${summary.romCount} ROMs • ${summary.emulatorName ?: "No emulator"} • ${summary.status}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

private fun resolveTreeLabel(
    activity: Activity,
    treeUri: Uri,
): String? {
    val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    return activity.contentResolver.query(
        documentUri,
        arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (!cursor.moveToFirst()) {
            return@use null
        }
        cursor.getString(0)
    }
}
