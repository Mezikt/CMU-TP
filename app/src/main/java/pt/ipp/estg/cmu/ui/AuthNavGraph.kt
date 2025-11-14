package pt.ipp.estg.cmu

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import pt.ipp.estg.cmu.ui.Content.LoginPage
import pt.ipp.estg.cmu.ui.Content.RegisterPage

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {

    navigation(
        startDestination = "login",
        route = "auth"
    ) {
        composable("login") {

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