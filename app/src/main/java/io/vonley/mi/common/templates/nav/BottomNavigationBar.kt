package io.vonley.mi.common.templates.nav

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.vonley.mi.Mi
import io.vonley.mi.ui.compose.TabItem

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        TabItem.Home,
        TabItem.Consoles,
        TabItem.Packages,
        TabItem.Ftp,
        TabItem.Settings
    )
    BottomNavigation(
        backgroundColor = Mi.Color.QUATERNARY,
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(painterResource(id = item.icon),
                    contentDescription = item.title)
                       },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White.copy(0.4f),
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {

    val navController = rememberNavController()
    BottomNavigationBar(navController)
}