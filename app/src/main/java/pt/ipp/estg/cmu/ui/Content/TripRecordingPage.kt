package pt.ipp.estg.cmu.ui.Content

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import pt.ipp.estg.cmu.data.TripRepository
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.viewmodel.TripRecordingViewModel
import com.google.firebase.ktx.Firebase // FIX: Added import
import com.google.firebase.firestore.ktx.firestore // FIX: Added import

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun TripRecordingPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    // --- ViewModel Instantiation ---
    val tripRepository = remember { TripRepository(Firebase.firestore) } // FIX: Corrected instantiation
    val userProfileRepository = remember { UserProfileRepository(AppDatabase.getDatabase(context).userProfileDao())}
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val viewModel: TripRecordingViewModel = viewModel(
        factory = TripRecordingViewModel.Factory(tripRepository, userProfileRepository, fusedLocationClient)
    )
    val uiState by viewModel.uiState.collectAsState()

    // --- Location Permission State ---
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // --- Map State ---
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f) // Default to Porto
    }
    
    // Update camera when path points change
    LaunchedEffect(uiState.pathPoints) {
        uiState.pathPoints.lastOrNull()?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
        }
    }

    // --- UI Feedback Logic ---
    LaunchedEffect(uiState) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Trip saved successfully!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record a Trip") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Google Map View ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aspectRatio(1f),
            ) {
                if (hasLocationPermission) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true)
                    ) {
                        if (uiState.pathPoints.isNotEmpty()) {
                            Polyline(
                                points = uiState.pathPoints,
                                color = Color.Blue,
                                width = 10f
                            )
                        }
                    }
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Location permission needed to record a trip.")
                    }
                }
            }

            // --- Trip Information ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoText(label = "Distance", value = "%.2f km".format(uiState.distance / 1000))
                InfoText(label = "Time", value = formatElapsedTime(uiState.elapsedTime))
            }

            // --- Start/Stop Button ---
            Button(
                onClick = {
                    if (uiState.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.primary
                ),
                enabled = hasLocationPermission && !uiState.isSaving
            ) {
                when {
                    uiState.isSaving -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    uiState.isRecording -> Text("Stop Recording")
                    else -> Text("Start Recording")
                }
            }
        }
    }
}

@Composable
private fun InfoText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
    }
}

private fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
