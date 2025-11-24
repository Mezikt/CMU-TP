package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import pt.ipp.estg.cmu.viewmodel.ProfileViewModel // Certifique-se que o import está correto

@Composable
fun PerfilPage(
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFriends: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {

    val userProfile by profileViewModel.userProfile.collectAsState(initial = null)


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (userProfile == null) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userProfile!!.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userProfile!!.email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))


                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileMenuItem(text = "Histórico", onClick = onNavigateToHistory)
                    Divider() // Linha separadora
                    ProfileMenuItem(text = "Amigos", onClick = onNavigateToFriends)
                }


                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Pontos: ${userProfile!!.points}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    text: String,
    onClick: () -> Unit
) {
    val navController = rememberNavController()
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Ir para $text",
                tint = Color.Gray
            )
        }
    }
}