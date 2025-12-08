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
import pt.ipp.estg.cmu.viewmodel.LeaderboardFilter
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterButton(
                    text = "Global",
                    isSelected = uiState.selectedFilter == LeaderboardFilter.GLOBAL,
                    onClick = { leaderboardViewModel.onFilterChanged(LeaderboardFilter.GLOBAL) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterButton(
                    text = "Friends",
                    isSelected = uiState.selectedFilter == LeaderboardFilter.FRIENDS,
                    onClick = { leaderboardViewModel.onFilterChanged(LeaderboardFilter.FRIENDS) }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Text(text = uiState.errorMessage!!)
                }
            } else if (uiState.users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val message = if (uiState.selectedFilter == LeaderboardFilter.FRIENDS) {
                        "You have no friends on the leaderboard yet!"
                    } else {
                        "The leaderboard is currently empty."
                    }
                    Text(message)
                }
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
private fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = if (isSelected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    val border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder

    Button(
        onClick = onClick,
        colors = colors,
        border = border
    ) {
        Text(text)
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
