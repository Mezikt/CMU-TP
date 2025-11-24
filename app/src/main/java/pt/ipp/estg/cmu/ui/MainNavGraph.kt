package pt.ipp.estg.cmu


import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import pt.ipp.estg.cmu.ui.Content.FriendsPage
import pt.ipp.estg.cmu.ui.Content.HistoryPage
import pt.ipp.estg.cmu.ui.Content.HomePage
import pt.ipp.estg.cmu.ui.Content.PerfilPage
import pt.ipp.estg.cmu.ui.Content.SettingsPage
import pt.ipp.estg.cmu.ui.Content.MapPage


fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "home",
        route = "main"
    ) {
        composable("home") {
            HomePage()
        }
        composable("map"){
            MapPage()
        }
        composable("perfil") {

            PerfilPage(
                onLogout = {
                    Firebase.auth.signOut()

                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToFriends = { navController.navigate("friends") }

            )
        }
        composable("definições") {
            SettingsPage()
        }

        composable("friends"){
            FriendsPage(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("history"){
            HistoryPage(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
