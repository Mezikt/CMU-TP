package pt.ipp.estg.cmu

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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

// Classe de dados para organizar os itens da barra de navegação
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // --- INÍCIO DA ALTERAÇÃO ---
    // 1. Verifica se já existe um utilizador autenticado no Firebase no momento da composição.
    val currentUser = Firebase.auth.currentUser
    // 2. Define o ponto de partida da navegação com base no resultado da verificação.
    val startDestination = if (currentUser != null) "main" else "auth"
    // --- FIM DA ALTERAÇÃO ---

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "perfil", "definições")

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                val items = listOf(
                    BottomNavItem("Início", "home", Icons.Default.Home),
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
            startDestination = startDestination, // <-- 3. USA A NOVA VARIÁVEL AQUI
            modifier = Modifier.padding(innerPadding)
        ) {
            authNavGraph(navController)
            mainNavGraph(navController)
        }
    }
}