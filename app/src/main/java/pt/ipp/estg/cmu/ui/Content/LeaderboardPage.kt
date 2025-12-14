package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.database.UserProfileEntity
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.viewmodel.LeaderboardFilter
import pt.ipp.estg.cmu.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserProfileRepository(AppDatabase.getDatabase(context).userProfileDao()) }
    val leaderboardViewModel: LeaderboardViewModel = viewModel(factory = LeaderboardViewModel.Factory(repository))
    val uiState by leaderboardViewModel.uiState.collectAsState()

    val currentUserId = Firebase.auth.currentUser?.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_leaderboard)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back)
                        )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterButton(
                    text = stringResource(R.string.filter_global),
                    isSelected = uiState.selectedFilter == LeaderboardFilter.GLOBAL,
                    onClick = { leaderboardViewModel.onFilterChanged(LeaderboardFilter.GLOBAL) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterButton(
                    text = stringResource(R.string.filter_friends_btn),
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
                        stringResource(R.string.msg_no_friends_leaderboard)
                    } else {
                        stringResource(R.string.msg_empty_leaderboard)
                    }
                    Text(message)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.users) { index, user ->
                        UserRankItem(
                            rank = index + 1,
                            user = user,
                            isCurrentUser = user.uid == currentUserId
                        )
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
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )

            if (user.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Foto de ${user.name}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                )
            }

            Text(
                text = "${user.points} pts",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}