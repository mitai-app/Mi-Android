package io.vonley.mi.ui.screens.consoles.presentation.protocol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.vonley.mi.common.templates.components.ConfirmDialog
import io.vonley.mi.di.network.protocols.webman.models.Game
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.consoles.domain.model.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolSheet(
    target: Client,
    onDismiss: () -> Unit,
    vm: ProtocolViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var lastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(target.ip) { vm.bind(target) }
    LaunchedEffect(vm) { vm.messages.collect { lastMessage = it } }
    DisposableEffect(Unit) { onDispose { vm.unbind() } }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${target.name} · ${target.ip}", style = MaterialTheme.typography.titleLarge)
            if (state.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            lastMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.features.forEach { feature ->
                when (feature) {
                    Feature.PS3MAPI -> NotifierPanel(
                        title = "PS3MAPI",
                        status = when (state.ps3mapiConnected) {
                            null -> "Connecting…"
                            true -> "Connected"
                            false -> "Not connected"
                        },
                        onAction = { vm.perform(feature, it) },
                        onNotify = { vm.notify(feature, it) },
                    )

                    Feature.CCAPI -> NotifierPanel(
                        title = "CCAPI",
                        status = null,
                        onAction = { vm.perform(feature, it) },
                        onNotify = { vm.notify(feature, it) },
                    )

                    Feature.WEBMAN -> WebmanPanel(
                        state = state,
                        onAction = { vm.perform(feature, it) },
                        onNotify = { vm.notify(feature, it) },
                        onDisc = vm::disc,
                        onPlay = vm::play,
                        onReload = vm::loadGames,
                    )

                    Feature.KLOG -> KLogPanel(lines = state.klog, streaming = state.klogStreaming)
                    else -> Unit
                }
            }
            if (state.features.isEmpty()) {
                Text("This console has no remote controls available.")
            }
        }
    }
}

@Composable
private fun Panel(title: String, status: String?, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                status?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
            }
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConsoleActions(onAction: (ConsoleAction) -> Unit) {
    var pending by remember { mutableStateOf<ConsoleAction?>(null) }
    pending?.let { action ->
        ConfirmDialog(
            title = action.label,
            message = "${action.label} the console?",
            confirm = action.label,
            onConfirm = { onAction(action) },
            onDismiss = { pending = null },
        )
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ConsoleAction.entries.forEach { action ->
            AssistChip(
                onClick = { if (action == ConsoleAction.Beep) onAction(action) else pending = action },
                label = { Text(action.label) },
            )
        }
    }
}

@Composable
private fun NotifyField(onNotify: (String) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    val send = {
        if (message.isNotBlank()) {
            onNotify(message.trim())
            message = ""
        }
    }
    OutlinedTextField(
        value = message,
        onValueChange = { message = it },
        label = { Text("Send a notification") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { send() }),
        trailingIcon = {
            IconButton(onClick = send, enabled = message.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun NotifierPanel(
    title: String,
    status: String?,
    onAction: (ConsoleAction) -> Unit,
    onNotify: (String) -> Unit,
) {
    Panel(title, status) {
        ConsoleActions(onAction)
        NotifyField(onNotify)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WebmanPanel(
    state: ProtocolUiState,
    onAction: (ConsoleAction) -> Unit,
    onNotify: (String) -> Unit,
    onDisc: (DiscAction) -> Unit,
    onPlay: (Game) -> Unit,
    onReload: () -> Unit,
) {
    Panel("webMAN", if (state.gamesLoading) "Loading games…" else null) {
        ConsoleActions(onAction)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DiscAction.entries.forEach { action ->
                AssistChip(onClick = { onDisc(action) }, label = { Text(action.label) })
            }
            AssistChip(onClick = onReload, label = { Text("Reload games") })
        }
        NotifyField(onNotify)
        state.games.filterValues { it.isNotEmpty() }.forEach { (type, games) ->
            Text(type.name, style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(games, key = { it.link + it.title }) { game -> GameTile(game) { onPlay(game) } }
            }
        }
    }
}

@Composable
private fun GameTile(game: Game, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(112.dp)) {
        AsyncImage(
            model = game.icon.ifEmpty { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.DarkGray)
        )
        Text(
            game.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Composable
private fun KLogPanel(lines: List<String>, streaming: Boolean) {
    val listState = rememberLazyListState()
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.scrollToItem(lines.lastIndex)
    }
    Panel("Kernel log", if (streaming) "Streaming" else "Disconnected") {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF111111))
                .padding(8.dp)
        ) {
            items(lines) { line ->
                Text(
                    KLogFormatter.format(line),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFE0E0E0),
                )
            }
        }
    }
}

/** Highlights the subsystem tag GoldHEN prefixes each kernel log line with. */
object KLogFormatter {
    private val tagColors = mapOf(
        "[GoldHEN]" to Color(0xFFE0B341),
        "[ShellUI]" to Color(0xFFFF3531),
        "[SceShellUI]" to Color(0xFFFF3531),
        "[SceShellCore]" to Color(0xFFFF3531),
        "[SceWorkaroundCtl]" to Color(0xFFFFB12F),
        "[Theme/I]" to Color(0xFF8C7DFF),
        "[SharedWebView]" to Color.Magenta,
        "[SceHidConfigService]" to Color.Cyan,
        "[SceNetEv]" to Color(0xFF5B8FFF),
        "[SceLncService]" to Color(0xFF8C7DFF),
        "[ScePatchChecker]" to Color(0xFFE0B341),
        "[BgDailyChecker]" to Color(0xFFE0B341),
        "[SceSystemStateMgr]" to Color(0xFF7D93D6),
        "[AppMgr Trace]" to Color(0xFF7D93D6),
        "[SceRnpsAppMgr]" to Color(0xFF7D93D6),
        "[sceProcessStarter]" to Color(0xFF5B8FFF),
        "[LoginMgr]" to Color(0xFFFF917D),
        "[SceUserServiceServer]" to Color(0xFFE0B341),
        "[SceUserService]" to Color(0xFFE0B341),
        "<klog>" to Color(0xFFE0B341),
    )

    fun format(line: String): AnnotatedString = buildAnnotatedString {
        val text = line.removePrefix("<118>")
        append(text)
        tagColors.forEach { (tag, color) ->
            val start = text.indexOf(tag)
            if (start >= 0) addStyle(SpanStyle(color = color), start, start + tag.length)
        }
    }
}
