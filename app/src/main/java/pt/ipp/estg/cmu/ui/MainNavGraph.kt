package pt.ipp.estg.cmu


import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import pt.ipp.estg.cmu.ui.Content.HomePage
import pt.ipp.estg.cmu.ui.Content.PerfilPage
import pt.ipp.estg.cmu.ui.Content.SettingsPage

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "home",
        route = "main"
    ) {
        composable("home") {
            HomePage()
        }
        composable("perfil") {

            PerfilPage(
                onLogout = {
                    // Termina a sessão do utilizador no Firebase
                    Firebase.auth.signOut()

                    //  o utilizador volta para o ecrã de autenticação
                    navController.navigate("auth") {
                        // Limpa todo o histórico de navegação para que o utilizador não possa voltar atrás
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable("definições") {
            SettingsPage()
        }
    }
}