package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.viewmodel.ProfileViewModel

@Composable
fun ProfilePage(
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFriends: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState(initial = null)
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    LaunchedEffect(Firebase.auth.currentUser) {
        profileViewModel.refreshProfile()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            // 1. First, check for an error message and display it
            errorMessage != null -> {
                ErrorContent(
                    message = errorMessage!!,
                    onRetry = { profileViewModel.refreshProfile() }
                )
            }
            // 2. Then, check if the user is authenticated
            Firebase.auth.currentUser == null -> {
                NotAuthenticatedContent(onNavigateToLogin = onLogout)
            }
            // 3. If no user profile data is available yet, show loading
            userProfile == null -> {
                CircularProgressIndicator()
            }
            // 4. Finally, if everything is fine, show the profile
            else -> {
                ProfileContent(
                    name = userProfile!!.name,
                    email = userProfile!!.email,
                    points = userProfile!!.points,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToFriends = onNavigateToFriends,
                    onLogout = {
                        profileViewModel.onLogout()
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text("An Error Occurred", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
private fun ProfileContent(
    name: String,
    email: String,
    points: Long,
    onNavigateToHistory: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = name, style = MaterialTheme.typography.headlineLarge)
        Text(text = email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Estatísticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pontos: $points", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuItem(icon = Icons.Filled.History, text = "Histórico de Viagens", onClick = onNavigateToHistory)
        Divider()
        ProfileMenuItem(icon = Icons.Filled.People, text = "Amigos", onClick = onNavigateToFriends)

        Spacer(modifier = Modifier.weight(1f)) 

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Logout, contentDescription = "Logout Icon", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
private fun NotAuthenticatedContent(onNavigateToLogin: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text("You are not logged in.", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please log in to see your profile.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToLogin) {
            Text("Go to Login")
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, modifier = Modifier.weight(1f), fontSize = 16.sp)
        }
    }
}
