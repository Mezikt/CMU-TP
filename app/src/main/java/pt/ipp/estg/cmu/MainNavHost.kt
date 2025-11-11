package pt.ipp.estg.cmu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding

@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Verifica se a rota atual pertence ao grafo "main" para mostrar a barra
    val showBottomBar = navBackStackEntry?.destination?.parent?.route == "main"

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                // O teu código da BottomAppBar fica aqui
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "auth", // Começa sempre no fluxo de autenticação
            modifier = Modifier.padding(innerPadding)
        ) {
            // Chama a função de extensão que definiste no outro ficheiro
            authNavGraph(navController)

            // Chama a função para o grafo principal
            mainNavGraph(navController)
        }
    }
}