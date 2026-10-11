package io.vonley.mi.ui.screens.consoles.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.vonley.mi.Mi
import io.vonley.mi.R
import io.vonley.mi.common.templates.components.ConfirmDialog
import io.vonley.mi.common.templates.components.EmptyState
import io.vonley.mi.common.templates.components.InputDialog
import io.vonley.mi.common.templates.components.LoadingState
import io.vonley.mi.common.templates.components.LocalSnackbarHostState
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.screens.consoles.domain.model.Console
import io.vonley.mi.ui.screens.consoles.domain.model.activeFeatures
import io.vonley.mi.ui.screens.consoles.domain.model.featureString
import io.vonley.mi.ui.screens.consoles.domain.model.protocolFeatures
import io.vonley.mi.ui.screens.consoles.domain.usecase.AddConsoleUseCase
import io.vonley.mi.ui.screens.consoles.presentation.protocol.ProtocolSheet
import io.vonley.mi.ui.theme.MiTheme

@Composable
fun ConsolesScreen(vm: ConsoleViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(vm) {
        vm.messages.collect { snackbar.showSnackbar(it) }
    }

    ConsolesContent(
        state = state,
        onSelect = vm::select,
        onControls = vm::openControls,
        onTogglePin = { vm.togglePin(it) },
        onAdd = { vm.add(it) },
        onRefresh = vm::refresh,
    )

    state.sheetTarget?.let { target ->
        ProtocolSheet(target = target, onDismiss = vm::dismissControls)
    }
}

@Composable
fun ConsolesContent(
    state: ConsolesUiState,
    onSelect: (Console) -> Unit,
    onControls: (Console) -> Unit,
    onTogglePin: (Console) -> Unit,
    onAdd: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var unverified by remember { mutableStateOf<Console?>(null) }

    if (showAdd) {
        InputDialog(
            title = "Add console",
            label = "Console IP",
            placeholder = "192.168.?.?",
            keyboardType = KeyboardType.Decimal,
            validate = { if (AddConsoleUseCase.isValidIpv4(it.trim())) null else "Enter an IPv4 address" },
            onConfirm = onAdd,
            onDismiss = { showAdd = false },
        )
    }
    unverified?.let { console ->
        ConfirmDialog(
            title = "Warning",
            message = "${console.ip} does not seem to have any open ports. Would you still like to connect?",
            confirm = "Connect",
            onConfirm = { onSelect(console) },
            onDismiss = { unverified = null },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.loading -> LoadingState("Looking for consoles…")
            state.error != null -> EmptyState(state.error, action = "Retry", onAction = onRefresh)
            state.consoles.isEmpty() -> EmptyState(
                "No consoles found yet. Enable FTP, PS3MAPI or webMAN on your console, or add it by IP.",
                action = "Scan now",
                onAction = onRefresh,
            )

            else -> LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(onClick = onRefresh) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Text("Scan", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                items(state.consoles, key = { it.ip }) { console ->
                    ConsoleCard(
                        console = console,
                        isTarget = console.ip == state.targetIp,
                        onClick = {
                            if (console.activeFeatures.isEmpty()) unverified = console else onSelect(console)
                        },
                        onControls = { onControls(console) },
                        onTogglePin = { onTogglePin(console) },
                    )
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showAdd = true },
            icon = { Icon(painterResource(R.drawable.icon_svg_add), contentDescription = null) },
            text = { Text("Add console") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

private val Console.cardColor: Color
    get() = when {
        !lastKnownReachable -> Color(0xFF8A8A8A)
        type == PlatformType.PS4 -> Mi.Color.QUATERNARY
        type == PlatformType.PS3 -> Mi.Color.QUINARY
        else -> Mi.Color.TERTIARY
    }

@Composable
fun ConsoleCard(
    console: Console,
    isTarget: Boolean,
    onClick: () -> Unit,
    onControls: () -> Unit,
    onTogglePin: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = console.cardColor, contentColor = Color.White),
        border = if (isTarget) BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer) else null,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_svg_monitor),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                val title = if (console.name == console.ip) console.type.name else "${console.name} (${console.type.name})"
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(console.ip, style = MaterialTheme.typography.bodyMedium)
                Text(
                    buildString {
                        append(console.featureString.ifEmpty { "No services detected" })
                        if (!console.lastKnownReachable) append(" · offline")
                        if (isTarget) append(" · target")
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (console.protocolFeatures.isNotEmpty()) {
                IconButton(onClick = onControls) {
                    Icon(Icons.Filled.Tune, contentDescription = "Console controls")
                }
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    if (console.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (console.pinned) "Unpin" else "Pin",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewConsoles() {
    MiTheme {
        ConsolesContent(
            state = ConsolesUiState(
                loading = false,
                targetIp = "192.168.1.184",
                consoles = listOf(
                    Console("192.168.1.184", "192.168.1.184", PlatformType.PS4, listOf(Feature.RPI, Feature.KLOG, Feature.FTP), true, "Home", pinned = true),
                    Console("192.168.1.185", "Living room", PlatformType.PS3, listOf(Feature.WEBMAN, Feature.PS3MAPI, Feature.FTP), true, "Home"),
                    Console("192.168.1.1", "192.168.1.1", PlatformType.UNKNOWN, listOf(Feature.FTP), false, "Home"),
                )
            ),
            onSelect = {}, onControls = {}, onTogglePin = {}, onAdd = {}, onRefresh = {},
        )
    }
}
