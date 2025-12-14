package pt.ipp.estg.cmu

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import pt.ipp.estg.cmu.ui.Content.* // Import all content pages
import pt.ipp.estg.cmu.ui.Content.map.MapPage // FIX: Import from the correct path

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "home",
        route = "main"
    ) {
        composable("home") {
            HomePage(navController = navController)
        }
        composable("map"){
            MapPage(navController = navController)
        }
        composable("profile") {
            ProfilePage(
                onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("auth") { popUpTo("main") { inclusive = true } }
                },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToFriends = { navController.navigate("friends") },
                onNavigateToLeaderboard = { navController.navigate("leaderboard") } ,
                onNavigateToReviews = { navController.navigate("review") }
            )
        }
        composable("settings") {
            SettingsPage(toChangePassword = { navController.navigate("changePassword") })
        }
        composable("friends"){
            FriendsPage(onNavigateBack = { navController.navigateUp() })
        }
        composable("history"){
            HistoryPage(onNavigateBack = { navController.navigateUp() })
        }
        composable("changePassword"){
            ChangePasswordPage(onNavigateBack = { navController.navigateUp() })
        }
        composable("trip_recording") {
            TripRecordingPage(onNavigateBack = { navController.navigateUp() })
        }
        composable("leaderboard") { // Add new destination for the Leaderboard
            LeaderboardPage(onNavigateBack = { navController.navigateUp() })
        }

        composable("review") {
            ReviewAllPage(onNavigateBack = { navController.navigateUp() })
        }


        composable(
            route = "review/{pointId}",
            arguments = listOf(navArgument("pointId") { type = NavType.StringType })
        ) {
            val pointId = it.arguments?.getString("pointId") ?: ""
            ReviewPage(
                pointId = pointId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
