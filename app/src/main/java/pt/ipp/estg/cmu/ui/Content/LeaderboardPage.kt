package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.database.UserProfileEntity
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardPage(onNavigateBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { UserProfileRepository(AppDatabase.getDatabase(context).userProfileDao()) }
    val leaderboardViewModel: LeaderboardViewModel = viewModel(factory = LeaderboardViewModel.Factory(repository))
    val uiState by leaderboardViewModel.uiState.collectAsState()
    
    val currentUserId = Firebase.auth.currentUser?.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage!!)
            } else if (uiState.users.isEmpty()) {
                Text("The leaderboard is currently empty.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(uiState.users) { index, user ->
                        val isCurrentUser = user.uid == currentUserId
                        UserRankItem(rank = index + 1, user = user, isCurrentUser = isCurrentUser)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRankItem(rank: Int, user: UserProfileEntity, isCurrentUser: Boolean) {
    val cardColors = if (isCurrentUser) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#$rank", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))
                Text(user.name, style = MaterialTheme.typography.bodyLarge)
            }
            Text("${user.points} pts", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
