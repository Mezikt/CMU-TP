package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu.data.Trip
import pt.ipp.estg.cmu.data.TripRepository
import pt.ipp.estg.cmu.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    onNavigateBack: () -> Unit,
) {
    val repository = remember { TripRepository(Firebase.firestore) }
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(repository))
    val uiState by historyViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip History") },
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
            } else if (uiState.trips.isEmpty()) {
                Text("You have no recorded trips yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(uiState.trips) { trip ->
                        TripItem(trip = trip)
                    }
                }
            }
        }
    }
}

@Composable
private fun TripItem(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formattedDate = trip.date?.toDate()?.let {
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(it)
            } ?: "N/A"

            Text(text = "Date: $formattedDate", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Distance:", value = "%.2f km".format(trip.distance / 1000))
            InfoRow(label = "Duration:", value = formatDuration(trip.duration))
            InfoRow(label = "Points:", value = "${trip.points}")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
        Text(text = value)
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, secs)
        minutes > 0 -> String.format("%dm %02ds", minutes, secs)
        else -> String.format("%ds", secs)
    }
}
