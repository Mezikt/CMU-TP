package pt.ipp.estg.cmu

// Adiciona estes imports se estiverem em falta
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
            // CORREÇÃO: A função PerfilPage agora precisa de receber a lógica para o logout.
            PerfilPage(
                onLogout = {
                    // 1. Termina a sessão do utilizador no Firebase
                    Firebase.auth.signOut()

                    // 2. Navega o utilizador de volta para o ecrã de autenticação
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