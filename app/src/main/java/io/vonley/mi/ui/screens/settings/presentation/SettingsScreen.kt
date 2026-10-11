package io.vonley.mi.ui.screens.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.vonley.mi.common.templates.components.ChoiceDialog
import io.vonley.mi.common.templates.components.ConfirmDialog
import io.vonley.mi.common.templates.components.InputDialog
import io.vonley.mi.common.templates.components.LocalSnackbarHostState
import io.vonley.mi.common.templates.components.SectionHeader
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.ui.screens.settings.domain.Settings
import io.vonley.mi.ui.theme.MiTheme
import io.vonley.mi.utils.SharedPreferenceManager

private enum class SettingsDialog { FtpUser, FtpPassword, PayloadPort, JbPort, ScanInterval, Clear }

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(vm) {
        vm.refresh()
        vm.messages.collect { snackbar.showSnackbar(it) }
    }
    SettingsContent(
        settings = state,
        onFtpUser = vm::setFtpUser,
        onFtpPassword = vm::setFtpPassword,
        onPayloadFeature = vm::setPayloadFeature,
        onJbPort = vm::setJbPort,
        onScanInterval = vm::setScanInterval,
        onJbService = vm::setJbServiceEnabled,
        onCache = vm::setCacheEnabled,
        onClear = vm::clearAll,
    )
}

@Composable
fun SettingsContent(
    settings: Settings,
    onFtpUser: (String) -> Unit,
    onFtpPassword: (String) -> Unit,
    onPayloadFeature: (Feature) -> Unit,
    onJbPort: (Int) -> Unit,
    onScanInterval: (Int) -> Unit,
    onJbService: (Boolean) -> Unit,
    onCache: (Boolean) -> Unit,
    onClear: () -> Unit,
) {
    var dialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }
    val dismiss = { dialog = null }

    when (dialog) {
        SettingsDialog.FtpUser -> InputDialog(
            title = "FTP Username",
            label = "Username",
            initial = settings.ftpUser,
            placeholder = "anonymous",
            onConfirm = onFtpUser,
            onDismiss = dismiss,
        )

        SettingsDialog.FtpPassword -> InputDialog(
            title = "FTP Password",
            label = "Password",
            secret = true,
            onConfirm = onFtpPassword,
            onDismiss = dismiss,
        )

        SettingsDialog.PayloadPort -> ChoiceDialog(
            title = "Default payload port",
            options = SharedPreferenceManager.PAYLOAD_FEATURES,
            selected = settings.payloadFeature,
            label = { "${it.title} (port ${it.ports.first()})" },
            onSelect = onPayloadFeature,
            onDismiss = dismiss,
        )

        SettingsDialog.JbPort -> InputDialog(
            title = "Jailbreak server port",
            label = "Port",
            initial = settings.jbPort.toString(),
            keyboardType = KeyboardType.Number,
            validate = { if (SettingsViewModel.isValidPort(it)) null else "Use a port between 1024 and 65535" },
            onConfirm = { onJbPort(it.toInt()) },
            onDismiss = dismiss,
        )

        SettingsDialog.ScanInterval -> ChoiceDialog(
            title = "Scan for devices every…",
            options = SharedPreferenceManager.SCAN_INTERVALS,
            selected = settings.scanInterval,
            label = ::intervalLabel,
            onSelect = onScanInterval,
            onDismiss = dismiss,
        )

        SettingsDialog.Clear -> ConfirmDialog(
            title = "Clear everything?",
            message = "This removes saved consoles, repositories and all settings.",
            confirm = "Clear",
            onConfirm = onClear,
            onDismiss = dismiss,
        )

        null -> Unit
    }

    LazyColumn {
        item { SectionHeader("Info") }
        item {
            Row(
                "Wifi SSID: ${if (settings.wifiConnected) settings.wifiSsid.ifEmpty { "Unknown" } else "Not connected"}",
                if (settings.wifiConnected) settings.deviceIp else "Not connected"
            )
        }
        item { Row("Known target", settings.targetName ?: "None yet") }
        item { Row("Known firmware", settings.targetVersion ?: "Unknown") }

        item { SectionHeader("FTP") }
        item { Row("User", settings.ftpUser.ifEmpty { "anonymous (default)" }) { dialog = SettingsDialog.FtpUser } }
        item {
            Row("Password", if (settings.ftpPasswordSet) "••••••••" else "anonymous (default)") {
                dialog = SettingsDialog.FtpPassword
            }
        }

        item { SectionHeader("Server") }
        item {
            Row("Default payload port", "${settings.payloadFeature.title} (port ${settings.payloadFeature.ports.first()})") {
                dialog = SettingsDialog.PayloadPort
            }
        }
        item { Row("Jailbreak server port", settings.jbPort.toString()) { dialog = SettingsDialog.JbPort } }
        item {
            Row("Scan for devices every…", intervalLabel(settings.scanInterval)) { dialog = SettingsDialog.ScanInterval }
        }
        item {
            SwitchRow("Enable jailbreak server", "Lets a PS4 load the jailbreak from this phone", settings.jbServiceEnabled, onJbService)
        }
        item {
            SwitchRow(
                "Cache host files",
                if (settings.cacheEnabled) "Mi server will cache" else "Mi server will not cache",
                settings.cacheEnabled,
                onCache
            )
        }

        item { SectionHeader("App") }
        item { Row("Clear everything", "Removes cached & saved data") { dialog = SettingsDialog.Clear } }
        item { Row("Development", "Mr-Smithy-x") }
        item { Row("Version", settings.appVersion) }
    }
}

private fun intervalLabel(seconds: Int): String =
    if (seconds < 60) "$seconds seconds" else "${seconds / 60} min"

@Composable
private fun Row(title: String, summary: String, onClick: (() -> Unit)? = null) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
    HorizontalDivider()
}

@Composable
private fun SwitchRow(title: String, summary: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) },
        modifier = Modifier.clickable { onChange(!checked) },
    )
    HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettings() {
    MiTheme {
        SettingsContent(
            settings = Settings(
                wifiConnected = true, wifiSsid = "Home", deviceIp = "192.168.1.20",
                targetName = "192.168.1.45", targetVersion = "9.00",
                ftpUser = "", ftpPasswordSet = false, payloadFeature = Feature.GOLDENHEN,
                jbPort = 8080, scanInterval = 15, jbServiceEnabled = true, cacheEnabled = false,
                appVersion = "1.1.1-alpha",
            ),
            onFtpUser = {}, onFtpPassword = {}, onPayloadFeature = {}, onJbPort = {},
            onScanInterval = {}, onJbService = {}, onCache = {}, onClear = {},
        )
    }
}
