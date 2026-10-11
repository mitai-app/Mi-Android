package io.vonley.mi.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.common.templates.components.LocalSnackbarHostState
import io.vonley.mi.intents.MiService
import io.vonley.mi.ui.screens.consoles.presentation.ConsolesScreen
import io.vonley.mi.ui.screens.ftp.presentation.FtpScreen
import io.vonley.mi.ui.screens.home.presentation.HomeScreen
import io.vonley.mi.ui.screens.packages.presentation.PayloadsScreen
import io.vonley.mi.ui.screens.settings.presentation.SettingsScreen
import io.vonley.mi.ui.theme.MiTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Started from the foreground activity: background starts are restricted on API 26+.
        startService(Intent(this, MiService::class.java))
        setContent {
            MiTheme { MiApp() }
        }
    }
}

@Composable
fun MiApp(vm: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val targetSummary by vm.targetSummary.collectAsStateWithLifecycle()

    NotificationPermissionRequest(onGranted = vm::onNotificationsAllowed)

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            topBar = { MiTopBar(targetSummary) },
            bottomBar = { MiBottomBar(navController) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = MiDestination.Home.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(MiDestination.Home.route) { HomeScreen() }
                composable(MiDestination.Consoles.route) { ConsolesScreen() }
                composable(MiDestination.Payloads.route) { PayloadsScreen() }
                composable(MiDestination.Ftp.route) { FtpScreen() }
                composable(MiDestination.Settings.route) { SettingsScreen() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiTopBar(targetSummary: String) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ミ Mi", style = MaterialTheme.typography.titleLarge)
                Text(
                    targetSummary,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    )
}

@Composable
private fun MiBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination
    NavigationBar {
        MiDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = current?.hierarchy?.any { it.route == destination.route } == true,
                icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                label = { Text(destination.title) },
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

/** The server's status notification needs POST_NOTIFICATIONS on Android 13+. */
@Composable
private fun NotificationPermissionRequest(onGranted: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onGranted()
    }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
