package pt.ipp.estg.cmu

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pt.ipp.estg.cmu.ui.Content.*

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Place, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_map)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "map" } == true,
                    onClick = {
                        navController.navigate("map") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_trips)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "trips" } == true,
                    onClick = {
                        navController.navigate("trips") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_profile)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomePage()
            }
            composable("map") {
                MapPage()
            }
            composable("trips") {
                TripsPage()
            }
            composable("profile") {
                ProfilePage()
            }
        }
    }
}
