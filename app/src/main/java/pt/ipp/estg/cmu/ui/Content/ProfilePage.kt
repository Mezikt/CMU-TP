package pt.ipp.estg.cmu.ui.Content

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
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
    onNavigateToReviews: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState(initial = null)
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val currentUser = Firebase.auth.currentUser // Obter o utilizador atual do Firebase

    LaunchedEffect(currentUser) {
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
            currentUser == null -> {
                NotAuthenticatedContent(onNavigateToLogin = onLogout)
            }
            userProfile == null -> {
                CircularProgressIndicator()
            }
            else -> {
                ProfileContent(
                    uid = currentUser.uid,
                    name = userProfile!!.name,
                    email = userProfile!!.email,
                    points = userProfile!!.points,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToFriends = onNavigateToFriends,
                    onNavigateToLeaderboard = onNavigateToLeaderboard,
                    onNavigateToReviews = onNavigateToReviews,
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
private fun ProfileContent(
    uid: String,
    name: String,
    email: String,
    points: Long,
    onNavigateToHistory: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current


    val sharedPreferences = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
    val savedUriString = sharedPreferences.getString("photo_uri_$uid", null)

    var selectedImageUri by remember {
        mutableStateOf(if (savedUriString != null) Uri.parse(savedUriString) else null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            sharedPreferences.edit()
                .putString("photo_uri_$uid", uri.toString())
                .apply()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))


        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .size(120.dp)
                .clickable { galleryLauncher.launch("image/*") }
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Alterar foto", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))


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
        Divider()
        ProfileMenuItem(
            icon = Icons.Filled.Announcement,
            text = stringResource(R.string.profile_reviews),
            onClick = onNavigateToReviews
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
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