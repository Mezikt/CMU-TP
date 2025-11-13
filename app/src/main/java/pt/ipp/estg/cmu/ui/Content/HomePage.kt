package pt.ipp.estg.cmu.ui.Content

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.service.TrackingService
import pt.ipp.estg.cmu.util.AndroidIntents
import pt.ipp.estg.cmu.viewmodel.MobilityViewModel

@Composable
fun HomePage(
    // windowSizeClass: WindowSizeClass? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isCompact = screenWidthDp < 600

    val viewModel: MobilityViewModel = viewModel()
    val isTracking by viewModel.isTracking.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    if (isCompact) {
        HomeScreenCompact(
            context = context,
            isTracking = isTracking,
            totalPoints = userProfile?.totalPoints ?: 0,
            totalTrips = userProfile?.totalTrips ?: 0,
            onStartTracking = {
                viewModel.startTracking()
                TrackingService.startService(context)
            },
            onStopTracking = {
                viewModel.stopTracking()
                TrackingService.stopService(context)
            }
        )
    } else {
        HomeScreenExpanded(
            context = context,
            isTracking = isTracking,
            totalPoints = userProfile?.totalPoints ?: 0,
            totalTrips = userProfile?.totalTrips ?: 0,
            onStartTracking = {
                viewModel.startTracking()
                TrackingService.startService(context)
            },
            onStopTracking = {
                viewModel.stopTracking()
                TrackingService.stopService(context)
            }
        )
    }
}

@Composable
fun HomeScreenCompact(
    context: Context,
    isTracking: Boolean,
    totalPoints: Int,
    totalTrips: Int,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.primary
            )

            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            StatsCard(totalPoints, totalTrips)

            Spacer(modifier = Modifier.height(16.dp))

            // Tracking Button
            TrackingButton(isTracking, onStartTracking, onStopTracking)

            Spacer(modifier = Modifier.height(16.dp))

            // Android Intents
            AndroidIntentButtons(context)
        }
    }
}

@Composable
fun HomeScreenExpanded(
    context: Context,
    isTracking: Boolean,
    totalPoints: Int,
    totalTrips: Int,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Coluna esquerda
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary
                )

                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.body1
                )

                StatsCard(totalPoints, totalTrips)
            }

            // Coluna direita
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrackingButton(isTracking, onStartTracking, onStopTracking)
                AndroidIntentButtons(context)
            }
        }
    }
}

@Composable
fun StatsCard(totalPoints: Int, totalTrips: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${stringResource(R.string.total_points)}: $totalPoints",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stringResource(R.string.total_trips)}: $totalTrips",
                style = MaterialTheme.typography.subtitle1
            )
        }
    }
}

@Composable
fun TrackingButton(
    isTracking: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Button(
        onClick = { if (isTracking) onStopTracking() else onStartTracking() },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isTracking) MaterialTheme.colors.error else MaterialTheme.colors.primary
        )
    ) {
        Text(
            text = stringResource(if (isTracking) R.string.stop_tracking else R.string.start_tracking)
        )
    }
}

@Composable
fun AndroidIntentButtons(context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { AndroidIntents.openContacts(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.share_contacts))
        }

        Button(
            onClick = { AndroidIntents.openDialer(context, "112") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.call_support))
        }

        Button(
            onClick = {
                AndroidIntents.sendSMS(
                    context,
                    "",
                    "Partilho os meus resultados de mobilidade suave!"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.send_sms))
        }
    }
}

// Componente Reutilizável de exemplo
@Composable
fun ReusableTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        color = MaterialTheme.colors.primary
    )
}
