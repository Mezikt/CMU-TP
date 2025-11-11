package pt.ipp.estg.cmu

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import pt.ipp.estg.cmu.ui.Content.LoginPage
import pt.ipp.estg.cmu.ui.Content.RegisterPage

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    // A função `navigation` cria um grafo de navegação aninhado
    navigation(
        startDestination = "login", // O ecrã inicial deste fluxo
        route = "auth"             // O nome do grafo
    ) {
        composable("login") {
            // Chamada Corrigida: Usar LoginPage
            LoginPage(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            // Bloco de Registo Ativado e Corrigido
            RegisterPage(
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack() // Volta para o ecrã de login
                }
            )
        }
    }
}