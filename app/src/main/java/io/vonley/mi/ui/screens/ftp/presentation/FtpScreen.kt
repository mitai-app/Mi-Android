package io.vonley.mi.ui.screens.ftp.presentation

import android.net.Uri
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.vonley.mi.R
import io.vonley.mi.common.templates.components.ConfirmDialog
import io.vonley.mi.common.templates.components.EmptyState
import io.vonley.mi.common.templates.components.InputDialog
import io.vonley.mi.common.templates.components.LoadingState
import io.vonley.mi.common.templates.components.LocalSnackbarHostState
import io.vonley.mi.ui.screens.ftp.domain.model.FtpConnection
import io.vonley.mi.ui.screens.ftp.domain.model.RemoteFile
import io.vonley.mi.ui.theme.MiTheme

private sealed interface FtpDialog {
    data class Rename(val file: RemoteFile) : FtpDialog
    data class Delete(val file: RemoteFile) : FtpDialog
    data class ConfirmUpload(val uri: Uri, val replacing: RemoteFile?) : FtpDialog
}

@Composable
fun FtpScreen(vm: FTPViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }

    var dialog by remember { mutableStateOf<FtpDialog?>(null) }
    var replacing by remember { mutableStateOf<RemoteFile?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) dialog = FtpDialog.ConfirmUpload(uri, replacing)
        replacing = null
    }

    when (val current = dialog) {
        is FtpDialog.Rename -> InputDialog(
            title = "Rename ${current.file.name}",
            label = "New name",
            initial = current.file.name,
            validate = { name ->
                when {
                    name.isBlank() -> "Enter a name"
                    '/' in name -> "Names cannot contain /"
                    else -> null
                }
            },
            onConfirm = { vm.rename(current.file, it) },
            onDismiss = { dialog = null },
        )

        is FtpDialog.Delete -> ConfirmDialog(
            title = "Delete",
            message = "Are you sure you want to delete ${current.file.name}?",
            confirm = "Delete",
            onConfirm = { vm.delete(current.file) },
            onDismiss = { dialog = null },
        )

        is FtpDialog.ConfirmUpload -> ConfirmDialog(
            title = if (current.replacing != null) "Replace ${current.replacing.name}?" else "Upload file?",
            message = if (current.replacing != null) {
                "The selected file will overwrite ${current.replacing.name} on the console."
            } else {
                "The selected file will be uploaded to the current directory."
            },
            confirm = if (current.replacing != null) "Replace" else "Upload",
            onConfirm = {
                current.replacing?.let { vm.replace(it, current.uri) } ?: vm.upload(current.uri)
            },
            onDismiss = { dialog = null },
        )

        null -> Unit
    }

    FtpContent(
        state = state,
        onConnect = vm::connect,
        onOpen = vm::open,
        onUp = vm::navigateUp,
        onRefresh = vm::refresh,
        onUpload = { picker.launch("*/*") },
        onRename = { dialog = FtpDialog.Rename(it) },
        onDelete = { dialog = FtpDialog.Delete(it) },
        onDownload = vm::download,
        onReplace = { replacing = it; picker.launch("*/*") },
    )
}

@Composable
fun FtpContent(
    state: FTPUiState,
    onConnect: () -> Unit,
    onOpen: (RemoteFile) -> Unit,
    onUp: () -> Unit,
    onRefresh: () -> Unit,
    onUpload: () -> Unit,
    onRename: (RemoteFile) -> Unit,
    onDelete: (RemoteFile) -> Unit,
    onDownload: (RemoteFile) -> Unit,
    onReplace: (RemoteFile) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            if (state.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (!state.hasTarget) {
                EmptyState("Select a console on the Consoles tab to browse its files.")
            } else when (val connection = state.connection) {
                FtpConnection.Disconnected -> EmptyState("Not connected.", action = "Connect", onAction = onConnect)
                FtpConnection.Connecting -> LoadingState("Connecting to the console…")
                is FtpConnection.Failed -> EmptyState(connection.message, action = "Retry", onAction = onConnect)
                is FtpConnection.Connected -> {
                    PathBar(connection.path, onUp = onUp, onRefresh = onRefresh)
                    HorizontalDivider()
                    if (connection.files.isEmpty()) {
                        EmptyState("This folder is empty.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                            items(connection.files, key = { it.name }) { file ->
                                RemoteFileRow(
                                    file = file,
                                    onOpen = { onOpen(file) },
                                    onRename = { onRename(file) },
                                    onDelete = { onDelete(file) },
                                    onDownload = { onDownload(file) },
                                    onReplace = { onReplace(file) },
                                )
                            }
                        }
                    }
                }
            }
        }
        if (state.hasTarget && state.connection is FtpConnection.Connected) {
            ExtendedFloatingActionButton(
                onClick = onUpload,
                icon = { Icon(painterResource(R.drawable.ic_svg_upload), contentDescription = null) },
                text = { Text("Upload") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun PathBar(path: String, onUp: () -> Unit, onRefresh: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        IconButton(onClick = onUp, enabled = path != "/") {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Parent directory")
        }
        Text(
            path,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRefresh) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RemoteFileRow(
    file: RemoteFile,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDownload: () -> Unit,
    onReplace: () -> Unit,
) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    val tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = { if (file.isDirectory) onOpen() else menu = true },
            onLongClick = { menu = true },
        ),
        leadingContent = {
            Icon(
                painterResource(if (file.isDirectory) R.drawable.icon_svg_folder else R.drawable.icon_svg_file),
                contentDescription = null,
                tint = tint,
            )
        },
        headlineContent = { Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            val details = listOfNotNull(
                if (file.isDirectory) null else Formatter.formatShortFileSize(context, file.size),
                file.modifiedMillis?.let { DateUtils.getRelativeTimeSpanString(it).toString() },
            ).joinToString(" · ")
            if (details.isNotEmpty()) Text(details)
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Actions for ${file.name}")
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; onRename() })
                    if (!file.isDirectory) {
                        DropdownMenuItem(text = { Text("Download") }, onClick = { menu = false; onDownload() })
                        DropdownMenuItem(text = { Text("Replace") }, onClick = { menu = false; onReplace() })
                    }
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewFtp() {
    MiTheme {
        FtpContent(
            state = FTPUiState(
                hasTarget = true,
                connection = FtpConnection.Connected(
                    host = "192.168.1.45",
                    path = "/data/GoldHEN",
                    files = listOf(
                        RemoteFile("payloads", true, 0, null),
                        RemoteFile("config.ini", false, 2048, 1_700_000_000_000),
                    )
                )
            ),
            onConnect = {}, onOpen = {}, onUp = {}, onRefresh = {}, onUpload = {},
            onRename = {}, onDelete = {}, onDownload = {}, onReplace = {},
        )
    }
}
