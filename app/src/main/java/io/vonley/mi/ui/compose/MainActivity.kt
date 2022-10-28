package io.vonley.mi.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.ui.compose.nav.BottomNavigationBar
import io.vonley.mi.ui.compose.nav.TopBar
import io.vonley.mi.ui.compose.screens.*
import io.vonley.mi.ui.compose.screens.consoles.presentation.ConsolesView
import io.vonley.mi.ui.compose.screens.ftp.presentation.FTPView
import io.vonley.mi.ui.compose.screens.packages.presentation.RepositoryView
import io.vonley.mi.ui.compose.screens.settings.SettingsView

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) },
        content = { padding -> // We have to pass the scaffold inner padding to our content. That's why we use Box.
            Box(modifier = Modifier.padding(padding)) {
                Navigation(navController = navController)
            }
        },
        backgroundColor = Color.White // Set background color to avoid the white flashing when you switch between screens
    )
}


@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = TabItem.Home.route) {
        composable(TabItem.Home.route) {
            HomeView()
        }
        composable(TabItem.Consoles.route) {
            ConsolesView()
        }
        composable(TabItem.Packages.route) {
            RepositoryView()
        }
        composable(TabItem.Ftp.route) {
            FTPView()
        }
        composable(TabItem.Settings.route) {
            SettingsView()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}