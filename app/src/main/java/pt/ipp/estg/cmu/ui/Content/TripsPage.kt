package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.data.model.TripEntity
import pt.ipp.estg.cmu.viewmodel.MobilityViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripsPage() {
    val viewModel: MobilityViewModel = viewModel()
    val trips by viewModel.trips.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.trips_title)) },
            backgroundColor = MaterialTheme.colors.primaryVariant,
            contentColor = MaterialTheme.colors.onPrimary
        )

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Ainda não há viagens registadas",
                    style = MaterialTheme.typography.body1
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trips) { trip ->
                    TripCard(trip)
                }
            }
        }
    }
}

@Composable
fun TripCard(trip: TripEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatDate(trip.startTime),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${stringResource(R.string.distance)}: ${String.format("%.2f", trip.distance)} km",
                        style = MaterialTheme.typography.body2
                    )
                    trip.endTime?.let {
                        val duration = (it - trip.startTime) / 60000 // minutos
                        Text(
                            text = "${stringResource(R.string.duration)}: $duration min",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }

                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = "${stringResource(R.string.points_earned)}:",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = "${trip.points} pts",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.secondary
                    )
                }
            }

            Text(
                text = "Modo: ${trip.transportMode}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
