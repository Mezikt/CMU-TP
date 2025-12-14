package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.viewmodel.ProfileViewModel

@Composable
fun ProfilePage(
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState(initial = null)
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    LaunchedEffect(Firebase.auth.currentUser) {
        profileViewModel.refreshProfile()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            errorMessage != null -> {
                ErrorContent(
                    message = errorMessage!!,
                    onRetry = { profileViewModel.refreshProfile() }
                )
            }
            Firebase.auth.currentUser == null -> {
                NotAuthenticatedContent(onNavigateToLogin = onLogout)
            }
            userProfile == null -> {
                CircularProgressIndicator()
            }
            else -> {
                ProfileContent(
                    name = userProfile!!.name,
                    email = userProfile!!.email,
                    points = userProfile!!.points,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToFriends = onNavigateToFriends,
                    onNavigateToLeaderboard = onNavigateToLeaderboard,
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
        Text(
            text = stringResource(R.string.title_error),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.btn_try_again))
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
    onNavigateToLeaderboard: () -> Unit,
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
                Text(
                    text = stringResource(R.string.title_statistics),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${stringResource(R.string.label_points_prefix)} $points",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        ProfileMenuItem(
            icon = Icons.Filled.History,
            text = stringResource(R.string.title_trip_history),
            onClick = onNavigateToHistory
        )
        Divider()
        ProfileMenuItem(
            icon = Icons.Filled.People,
            text = stringResource(R.string.title_friends),
            onClick = onNavigateToFriends
        )
        Divider()
        ProfileMenuItem(
            icon = Icons.Filled.Leaderboard,
            text = stringResource(R.string.title_leaderboard),
            onClick = onNavigateToLeaderboard
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = stringResource(R.string.desc_logout_icon),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_logout))
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
        Text(
            text = stringResource(R.string.title_not_logged_in),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.msg_login_profile_prompt),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToLogin) {
            Text(stringResource(R.string.btn_go_to_login))
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