package pt.ipp.estg.cmu.ui.Content

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.Trip
import pt.ipp.estg.cmu.data.TripRepository

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun TripRecordingPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Repositories ---
    val tripRepository = remember { TripRepository(Firebase.firestore) }

    // --- State Variables ---
    var isRecording by remember { mutableStateOf(false) }
    var distance by remember { mutableStateOf(0.0) }
    var elapsedTime by remember { mutableStateOf(0L) }
    val pathPoints = remember { mutableStateListOf<LatLng>() }

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

    // --- Location & Timer Logic ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Timer for elapsed time
    LaunchedEffect(key1 = isRecording) {
        if (isRecording) {
            while (true) {
                delay(1000)
                elapsedTime++
            }
        }
    }

    // Location callback to handle updates
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    pathPoints.add(newLatLng)

                    if (pathPoints.size > 1) {
                        distance += SphericalUtil.computeDistanceBetween(
                            pathPoints[pathPoints.size - 2],
                            newLatLng
                        )
                    }
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLng, 17f)
                }
            }
        }
    }

    // Effect to start/stop location updates
    DisposableEffect(isRecording, hasLocationPermission) {
        if (isRecording && hasLocationPermission) {
            pathPoints.clear()
            distance = 0.0
            elapsedTime = 0L

            val locationRequest = LocationRequest.create().apply {
                interval = 5000 // 5 seconds
                fastestInterval = 2000 // 2 seconds
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
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
                        if (pathPoints.isNotEmpty()) {
                            Polyline(
                                points = pathPoints,
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
                InfoText(label = "Distance", value = "%.2f km".format(distance / 1000))
                InfoText(label = "Time", value = formatElapsedTime(elapsedTime))
            }

            // --- Start/Stop Button ---
            Button(
                onClick = {
                    val wasRecording = isRecording
                    isRecording = !isRecording

                    // If we just stopped recording, save the trip
                    if (wasRecording && !isRecording) {
                        if (pathPoints.size > 1) { // Only save if there's a path
                            scope.launch {
                                val userId = Firebase.auth.currentUser?.uid
                                if (userId == null) {
                                    Toast.makeText(context, "Error: User not authenticated.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                // Convert List<LatLng> to a format compatible with Firestore
                                val pathForDb = pathPoints.map { mapOf("latitude" to it.latitude, "longitude" to it.longitude) }
                                
                                val trip = Trip(
                                    userId = userId,
                                    distance = distance,
                                    duration = elapsedTime,
                                    path = pathForDb,
                                    points = (distance / 100).toInt() // Example: 1 point per 100 meters
                                )

                                tripRepository.saveTrip(trip)
                                    .onSuccess {
                                        Toast.makeText(context, "Trip saved successfully!", Toast.LENGTH_SHORT).show()
                                        onNavigateBack() // Navigate back after saving
                                    }
                                    .onFailure { error ->
                                        Toast.makeText(context, "Error saving trip: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                             Toast.makeText(context, "Not enough data to save the trip.", Toast.LENGTH_SHORT).show()
                             onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                ),
                enabled = hasLocationPermission
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
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
