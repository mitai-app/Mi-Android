package io.vonley.mi.ui.screens.packages.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.vonley.mi.Mi
import io.vonley.mi.R
import io.vonley.mi.common.templates.components.ConfirmDialog
import io.vonley.mi.common.templates.components.EmptyState
import io.vonley.mi.common.templates.components.LoadingState
import io.vonley.mi.common.templates.components.LocalSnackbarHostState
import io.vonley.mi.common.templates.components.SectionHeader
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.PackageType
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo
import io.vonley.mi.ui.theme.MiTheme

@Composable
fun PayloadsScreen(vm: PackageViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(vm) {
        vm.refreshTarget()
        vm.messages.collect { snackbar.showSnackbar(it) }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) vm.addLocal(uris)
    }
    PayloadsContent(
        state = state,
        onQueryChange = vm::onQueryChange,
        onAddRepo = vm::addRepo,
        onSendPackage = vm::sendPackage,
        onPickFiles = { picker.launch("*/*") },
        onRemove = vm::remove,
        onSendQueue = vm::sendQueue,
        onClearSent = vm::clearSent,
    )
}

@Composable
fun PayloadsContent(
    state: PackagesUiState,
    onQueryChange: (String) -> Unit,
    onAddRepo: () -> Unit,
    onSendPackage: (Package) -> Unit,
    onPickFiles: () -> Unit,
    onRemove: (QueuedPayload) -> Unit,
    onSendQueue: () -> Unit,
    onClearSent: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(setOf<String>()) }
    var confirmPackage by remember { mutableStateOf<Package?>(null) }
    var confirmQueue by remember { mutableStateOf(false) }

    confirmPackage?.let { pkg ->
        ConfirmDialog(
            title = "Send ${pkg.name}?",
            message = "Mi will download ${pkg.name} ${pkg.version} for your PS4's firmware and send it to the console.",
            confirm = "Send",
            onConfirm = { onSendPackage(pkg) },
            onDismiss = { confirmPackage = null },
        )
    }
    if (confirmQueue) {
        ConfirmDialog(
            title = "Send payloads?",
            message = "Send ${state.queue.joinToString { it.payload.name }}?\n\n.bin files go to ${state.binTarget}; " +
                    ".pkg files are installed through the Remote Package Installer.",
            confirm = "Send",
            onConfirm = onSendQueue,
            onDismiss = { confirmQueue = false },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
            item { SectionHeader("Send from this phone") }
            if (state.queue.isEmpty()) {
                item {
                    Text(
                        "Pick .bin or .pkg files to send them to the selected console.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                items(state.queue, key = { it.payload.name }) { item ->
                    QueuedPayloadRow(item, enabled = !state.sending, onRemove = { onRemove(item) })
                }
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { confirmQueue = true }, enabled = !state.sending) {
                            Text(if (state.sending) "Sending…" else "Send")
                        }
                        if (state.queue.any { it.status == PayloadStatus.Sent }) {
                            OutlinedButton(onClick = onClearSent, enabled = !state.sending) { Text("Clear sent") }
                        }
                    }
                }
            }

            item { SectionHeader("Repositories") }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Enter repo url or search by packages…") },
                    singleLine = true,
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
            if (state.queryIsLink) {
                item {
                    Button(
                        onClick = onAddRepo,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("Add repository", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            } else {
                when (val repos = state.repos) {
                    RepoListState.Loading -> item { LoadingState("Loading repositories…") }
                    is RepoListState.Error -> item { EmptyState(repos.message) }
                    is RepoListState.Success -> {
                        if (repos.repos.isEmpty()) item { EmptyState("There's no repository…") }
                        repos.repos.forEach { repo ->
                            val isOpen = repo.link in expanded
                            item(key = "repo:${repo.link}") {
                                RepoCard(repo = repo, expanded = isOpen) {
                                    expanded = if (isOpen) expanded - repo.link else expanded + repo.link
                                }
                            }
                            if (isOpen) {
                                items(repo.packages.toList(), key = { "pkg:${repo.link}:${it.name}:${it.version}" }) { pkg ->
                                    RepoPackageItem(pkg) { confirmPackage = pkg }
                                }
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onPickFiles,
            icon = { Icon(painterResource(R.drawable.icon_svg_upload_two), contentDescription = null) },
            text = { Text("Pick files") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@Composable
private fun QueuedPayloadRow(item: QueuedPayload, enabled: Boolean, onRemove: () -> Unit) {
    val (label, color) = when (item.status) {
        PayloadStatus.Queued -> "Ready" to MaterialTheme.colorScheme.onSurfaceVariant
        PayloadStatus.Sending -> "Sending…" to Mi.Color.Mi.BLUE
        PayloadStatus.Sent -> "Sent" to Mi.Color.Mi.GREEN
        PayloadStatus.Failed -> (item.error ?: "Failed") to Mi.Color.Mi.RED
    }
    ListItem(
        headlineContent = { Text(item.payload.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${item.payload.size / 1024} KB · $label", color = color) },
        leadingContent = {
            Icon(painterResource(R.drawable.icon_svg_file), contentDescription = null, tint = color)
        },
        trailingContent = {
            IconButton(onClick = onRemove, enabled = enabled) {
                Icon(Icons.Filled.Close, contentDescription = "Remove")
            }
        },
    )
}

@Composable
private fun RepoCard(repo: Repo, expanded: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black, contentColor = Color.White),
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            AsyncImage(
                model = repo.banner,
                placeholder = painterResource(R.mipmap.help),
                error = painterResource(R.mipmap.help),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(repo.title, style = MaterialTheme.typography.titleMedium)
                    Text(repo.author, style = MaterialTheme.typography.bodySmall)
                    Text(repo.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Composable
private fun RepoPackageItem(pkg: Package, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = pkg.icon,
                placeholder = painterResource(R.drawable.icon_svg_inbox),
                error = painterResource(R.drawable.icon_svg_inbox),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(pkg.name, style = MaterialTheme.typography.titleSmall)
                Text("${pkg.author} · ${pkg.version} · ${pkg.type.name.lowercase()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPayloads() {
    val goldhen = Package(
        link = "https://github.com/GoldHEN/GoldHEN",
        name = "GoldHen",
        author = "SiSTRo",
        version = "2.2.4",
        type = PackageType.PLUGIN,
        icon = "",
        dl = mapOf("v900" to "https://example.com/goldhen_900.bin"),
    )
    MiTheme {
        PayloadsContent(
            state = PackagesUiState(
                repos = RepoListState.Success(
                    listOf(Repo("https://example.com/packages.json", "Repository", "Mi", "", "Common PS4 payloads", arrayOf(goldhen)))
                ),
                binTarget = "Golden Hen (port 9090)",
            ),
            onQueryChange = {}, onAddRepo = {}, onSendPackage = {}, onPickFiles = {},
            onRemove = {}, onSendQueue = {}, onClearSent = {},
        )
    }
}
