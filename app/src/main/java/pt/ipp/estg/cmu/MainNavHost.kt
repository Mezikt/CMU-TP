package pt.ipp.estg.cmu

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pt.ipp.estg.cmu.ui.Content.BottomNav

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("home", "map", "profile", "settings")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNav(navController = navController)
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(it)
        ) {
            authNavGraph(navController)
            mainNavGraph(navController)
        }
    }
}
