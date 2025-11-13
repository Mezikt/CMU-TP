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

// Classe de dados para organizar os itens da barra de navegação
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "perfil", "definições") // Lógica simplificada

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Só mostra a barra de navegação se a showBottomBar for verdadeira
            if (showBottomBar) {

                // 1. Lista de Itens de Navegação
                val items = listOf(
                    BottomNavItem("Início", "home", Icons.Default.Home),
                    BottomNavItem("Perfil", "perfil", Icons.Default.Person),
                    BottomNavItem("Definições", "definições", Icons.Default.Settings)
                )

                // 2. Componente NavigationBar
                NavigationBar {
                    // 3. Loop para criar cada item
                    items.forEach { item ->
                        NavigationBarItem(
                            // 4. Lógica para saber qual item está selecionado
                            selected = currentRoute == item.route,
                            // 5. Ação ao clicar: navegar para a rota do item
                            onClick = {
                                navController.navigate(item.route) {
                                    // Limpa a pilha de navegação para evitar acumular ecrãs
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
            startDestination = "auth", // Continua a começar no fluxo de autenticação
            modifier = Modifier.padding(innerPadding)
        ) {
            // Os vossos grafos de navegação
            authNavGraph(navController)
            mainNavGraph(navController)
        }
    }
}