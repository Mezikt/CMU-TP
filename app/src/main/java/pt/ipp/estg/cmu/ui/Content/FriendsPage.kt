package pt.ipp.estg.cmu.ui.Content

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.database.UserProfileEntity
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.viewmodel.FriendsViewModel
import pt.ipp.estg.cmu.viewmodel.FriendsUiState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserProfileRepository(AppDatabase.getDatabase(context).userProfileDao()) }
    val viewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsState()

    var friendAdd by remember { mutableStateOf("") }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf(false) }

    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Friend by Email") },
            text = {
                OutlinedTextField(
                    value = friendAdd,
                    onValueChange = { friendAdd = it },
                    label = { Text("Email") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendFriendRequest(friendAdd)
                        showAddFriendDialog = false
                        friendAdd = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddFriendDialog = false
                        friendAdd = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRequestsDialog) {
        AlertDialog(
            onDismissRequest = { showRequestsDialog = false },
            title = { Text("Friend Requests") },
            text = {
                if (uiState.friendRequests.isEmpty()) {
                    Text("You have no pending friend requests.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.friendRequests) { user ->
                            FriendRequestItem(
                                user = user,
                                onAccept = { viewModel.acceptFriendRequest(user.email) },
                                onDecline = { viewModel.declineFriendRequest(user.email) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRequestsDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Add new friend")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search for friends by name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            LazyColumn {
                if (uiState.searchResults.isNotEmpty()) {
                    item { SectionTitle(title = "Search Results") }
                    items(uiState.searchResults) { user ->
                        UserSearchResultItem(user = user, onAddFriend = { viewModel.sendFriendRequest(user.uid) })
                    }
                }

                if (uiState.friendRequests.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical=8.dp), contentAlignment = Alignment.Center) {
                            Button(onClick = { showRequestsDialog = true }) {
                                Text("View Friend Requests (${uiState.friendRequests.size})")
                            }
                        }
                    }
                }

                item { SectionTitle(title = "Your Friends") }
                if (uiState.friends.isEmpty()) {
                    item { Text("You have no friends yet. Add some!") }
                } else {
                    items(uiState.friends) { user ->
                        FriendItem(user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun UserSearchResultItem(user: UserProfileEntity, onAddFriend: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAddFriend) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add friend")
            }
        }
    }
}

@Composable
private fun FriendRequestItem(user: UserProfileEntity, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.email, fontWeight = FontWeight.Bold)
                Text("Wants to be your friend", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick =  onAccept){
                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color(0xFF4CAF50))
                }
                IconButton(onClick = onDecline) {
                    Icon(Icons.Default.Clear, contentDescription = "Decline", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FriendItem(user: UserProfileEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(user.name, fontWeight = FontWeight.Bold)
        }
    }
}
