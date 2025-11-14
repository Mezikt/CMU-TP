package pt.ipp.estg.cmu

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase // Adicionar este import
import com.google.firebase.auth.auth   // Adicionar este import

data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()


    val currentUser = Firebase.auth.currentUser

    val startDestination = if (currentUser != null) "main" else "auth"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home","map", "perfil", "definições")

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                val items = listOf(
                    BottomNavItem("Início", "home", Icons.Default.Home),
                    BottomNavItem("Mapa", "map", Icons.Default.Map),
                    BottomNavItem("Perfil", "perfil", Icons.Default.Person),
                    BottomNavItem("Definições", "definições", Icons.Default.Settings)
                )
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(item.title) },
                            icon = { Icon(item.icon, contentDescription = item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            authNavGraph(navController)
            mainNavGraph(navController)
        }
    }
}