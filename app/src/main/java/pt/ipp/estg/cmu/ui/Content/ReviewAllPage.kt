package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.data.ReviewRepository
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.database.UserProfileEntity
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.viewmodel.ReviewViewModel
import pt.ipp.estg.cmu.data.Review


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAllPage(    onNavigateBack: () -> Unit) {
    val repository = remember { ReviewRepository(Firebase.firestore) }
    val reviewViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory(repository))
    val uiState by reviewViewModel.uiState.collectAsState()

    reviewViewModel.loadReviews();



    val currentUserId = Firebase.auth.currentUser?.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(pt.ipp.estg.cmu.R.string.profile_reviews)) },
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
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage!!)
                }
            } else if (uiState.reviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val message =stringResource(R.string.msg_no_reviews)
                    Text(message)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(uiState.reviews) { index, reviews ->
                        ReviewItem(rank = index + 1, reviews = reviews)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(rank: Int, reviews: Review) {
    val cardColors = CardDefaults.cardColors()

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
                if(reviews.comment.isEmpty()){
                    Text(stringResource(R.string.no_comment), style = MaterialTheme.typography.bodyLarge)
                }else{
                Text(reviews.comment, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Text(
                text = "${reviews.rating} stars",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}