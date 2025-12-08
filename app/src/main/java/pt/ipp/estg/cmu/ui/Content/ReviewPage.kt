package pt.ipp.estg.cmu.ui.Content

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.data.ReviewRepository
import pt.ipp.estg.cmu.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewPage(
    pointId: String, // The ID of the mobility point being reviewed
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // --- ViewModel Instantiation ---
    val repository = remember { ReviewRepository(Firebase.firestore) }
    val reviewViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory(repository))
    val uiState by reviewViewModel.uiState.collectAsState()

    // --- Local State ---
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    // --- UI Feedback Logic ---
    LaunchedEffect(uiState) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Reviewing point: $pointId", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                // Star rating
                Text("Your Rating", style = MaterialTheme.typography.titleLarge)
                RatingBar(rating = rating, onRatingChanged = { rating = it })

                Spacer(modifier = Modifier.height(16.dp))

                // Comment field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Leave a comment (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // Submit button
                Button(
                    onClick = { 
                        reviewViewModel.submitReview(pointId, rating, comment) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading // Disable button while loading
                ) {
                    Text("Submit Review")
                }
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Star $i",
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChanged(i) }
            )
        }
    }
}
