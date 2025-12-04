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
import pt.ipp.estg.cmu.ui.Content.ChangePasswordPage
import pt.ipp.estg.cmu.ui.Content.TripRecordingPage


fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "home",
        route = "main"
    ) {
        composable("home") {
            HomePage(navController = navController) // Pass navController
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
            SettingsPage(toChangePassword = { navController.navigate("changePassword") })
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

        composable("changePassword"){
            ChangePasswordPage(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable("trip_recording") { // Add new destination
            TripRecordingPage(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
