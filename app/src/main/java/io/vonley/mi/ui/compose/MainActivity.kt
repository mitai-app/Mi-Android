package io.vonley.mi.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.common.templates.nav.BottomNavigationBar
import io.vonley.mi.common.templates.nav.TopBar
import io.vonley.mi.ui.MainViewModel
import io.vonley.mi.ui.TabItem
import io.vonley.mi.ui.screens.consoles.presentation.ConsolesView
import io.vonley.mi.ui.screens.ftp.presentation.FTPView
import io.vonley.mi.ui.screens.home.HomeView
import io.vonley.mi.ui.screens.packages.presentation.RepositoryView
import io.vonley.mi.ui.screens.settings.SettingsView

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Root()
        }
    }

}


@Composable
fun Root() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        composable(route = "about") {

        }
        navigation(
            startDestination = "login",
            route = "auth"
        ) {
            composable(route = "login") {
                val viewModel =
                    it.sharedViewModel<MainViewModel>(navController = navController)
                Button(
                    modifier = Modifier.padding(16.dp)
                        .fillMaxWidth(),
                    onClick = {
                    navController.navigate(route = "overview") {
                        popUpTo(route = "auth") {
                            //A, B[Our Nav Graph Here], C, D[Our Screen Is Here]  -> E (overview)
                            //A -> E
                            inclusive =
                                true // it would also pop this current view out the backstack
                        }
                    }
                }) {

                    Text(text = "Example", modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight(), fontSize=22.sp)
                }
            }

            composable(route = "register") {
                val viewModel =
                    it.sharedViewModel<MainViewModel>(navController = navController)

            }

            composable(route = "forgot_password") {
                val viewModel =
                    it.sharedViewModel<MainViewModel>(navController = navController)

            }
        }
        navigation(
            startDestination = "main",
            route = "overview"
        ) {
            composable(route = "main") {
                MainScreen(navController)
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavHostController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(viewModelStoreOwner = parentEntry)
}

@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
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
    Root()
}