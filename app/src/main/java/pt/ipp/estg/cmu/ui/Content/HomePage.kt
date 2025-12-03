package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomePage(navController: NavHostController) { // Receive NavController
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Search locations or transport...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { navController.navigate("map") }) { // Navigate to map
                Text("Find a Ride")
            }
            Button(onClick = { navController.navigate("trip_recording") }) { // Navigate to trip recording
                Text("Record a Trip")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Leaderboard Snippet
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        // TODO: Add a list or preview of the leaderboard here
        Text("Top users will be displayed here.")
    }
}