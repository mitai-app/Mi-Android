package io.vonley.mi.ui.screens.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.vonley.mi.Mi
import io.vonley.mi.R
import io.vonley.mi.common.templates.components.LocalSnackbarHostState
import io.vonley.mi.common.templates.components.MessageDialog
import io.vonley.mi.common.templates.components.SectionHeader
import io.vonley.mi.di.network.JbEvent
import io.vonley.mi.di.network.JbLog
import io.vonley.mi.models.AppUpdate
import io.vonley.mi.models.Device
import io.vonley.mi.models.MIEnumCMD
import io.vonley.mi.ui.screens.home.presentation.article.ArticleCard
import io.vonley.mi.ui.screens.home.presentation.log.LogCard
import io.vonley.mi.ui.theme.MiTheme

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(vm) {
        vm.refresh()
        vm.events.collect { event ->
            when (event) {
                is JbEvent.JailbreakSucceeded -> dialog = "Jailbreak complete" to
                        "${event.message}\nPssst... Turn on FTP in GoldHEN so Mi can detect the PS4!"

                is JbEvent.JailbreakFailed -> dialog = "Jailbreak failed" to event.message
                is JbEvent.PayloadSent -> snackbar.showSnackbar(event.message)
                is JbEvent.SendPayloadFailed ->
                    snackbar.showSnackbar("Unable to reach the PS4 after ${event.attempts} attempts")
            }
        }
    }

    dialog?.let { (title, message) ->
        MessageDialog(title = title, message = message, onDismiss = { dialog = null })
    }

    HomeContent(state)
}

@Composable
fun HomeContent(state: HomeUiState) {
    var showInstructions by remember { mutableStateOf(false) }
    if (showInstructions) {
        MessageDialog(
            title = "ミ (mi)",
            message = instructions(state.hostAddress),
            onDismiss = { showInstructions = false }
        )
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            HostCard(
                hostAddress = state.hostAddress,
                running = state.serverRunning,
                device = state.device,
                onInfo = { showInstructions = true }
            )
        }
        state.update?.let { update -> item { UpdateCard(update) } }
        items(Mi.ARTICLES.toList(), key = { it.name }) { ArticleCard(article = it) }
        item { SectionHeader("Logs") }
        if (state.logs.isEmpty()) {
            item {
                Text(
                    "Nothing yet. Open ${state.hostAddress} in your PS4 browser to start.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            items(state.logs.asReversed(), key = { it.id }) { LogCard(log = it) }
        }
    }
}

@Composable
private fun HostCard(hostAddress: String, running: Boolean, device: Device?, onInfo: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Mi.Color.QUATERNARY,
            contentColor = androidx.compose.ui.graphics.Color.White
        ),
    ) {
        Row(modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 16.dp, end = 4.dp)) {
            Column(
                modifier = Modifier.weight(1f).padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    if (running) "Jailbreak host" else "Jailbreak host is off",
                    style = MaterialTheme.typography.labelLarge
                )
                SelectionContainer {
                    Text(hostAddress, style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    device?.let { "Last seen: ${it.device} (${it.version}) · ${it.ip}" }
                        ?: "Type this address into your PS4 browser",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onInfo, modifier = Modifier.align(Alignment.Top)) {
                Icon(painterResource(R.drawable.icon_svg_info), contentDescription = "Instructions")
            }
        }
    }
}

@Composable
private fun UpdateCard(update: AppUpdate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("New update: ${update.version}", style = MaterialTheme.typography.titleMedium)
            if (update.changes.isNotBlank()) Text(update.changes, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun instructions(hostAddress: String) =
    "Thank you for downloading Mi. Start by typing $hostAddress into your PS4 browser and start the " +
            "jailbreak process. When the jailbreak completes GoldHEN will be loaded. To upload bin files " +
            "go to your PS4 settings -> GoldHEN and enable \"BinLoader Server\"; enable FTP there as well " +
            "to use the FTP tab.\n\nHINT: ENABLE FTP ON YOUR PS4 SO THAT MI CAN DISCOVER YOUR DEVICE AUTOMATICALLY."

@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    val device = Device("PlayStation 4 9.00", "9.00", "192.168.1.45")
    MiTheme {
        HomeContent(
            HomeUiState(
                hostAddress = "http://192.168.1.20:8080",
                serverRunning = true,
                device = device,
                logs = listOf(
                    JbLog(1, "Initializing setup", MIEnumCMD.INITIATED, device),
                    JbLog(2, "Jailbreak complete", MIEnumCMD.SUCCESS, device),
                    JbLog(3, "[Payload:Wait] First we will wait 20 seconds"),
                ),
            )
        )
    }
}
