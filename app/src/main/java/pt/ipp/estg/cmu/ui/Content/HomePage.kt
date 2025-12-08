package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomePage(navController: NavHostController) { // Receive NavController
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium
        )

        // Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Search locations or transport...") },
            modifier = Modifier.fillMaxWidth()
        )

        // Quick Action Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { navController.navigate("map") }) { 
                Text("Find a Ride")
            }
            Button(onClick = { navController.navigate("trip_recording") }) { 
                Text("Record a Trip")
            }
        }

        // Leaderboard Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth() // FIX: Make the Column fill the Card's width
            ) {
                Text(
                    text = "Leaderboard",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("See how you stack up against other users.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("leaderboard") },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("View Leaderboard")
                }
            }
        }
    }
}
