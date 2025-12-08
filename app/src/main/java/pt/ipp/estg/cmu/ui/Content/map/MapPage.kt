package pt.ipp.estg.cmu.ui.Content.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.bitmapDescriptorFromVector
import pt.ipp.estg.cmu.data.MobilityPoint // Correct model
import pt.ipp.estg.cmu.data.MobilityPointRepository

// FIX: Explicit imports to resolve reference errors
import pt.ipp.estg.cmu.ui.Content.map.MapViewModel
import pt.ipp.estg.cmu.ui.Content.map.MobilityTypeFilter

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapPage(navController: NavController) {
    val context = LocalContext.current

    val repository = remember { MobilityPointRepository(Firebase.firestore) }
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModel.Factory(repository))
    val uiState by mapViewModel.uiState.collectAsState()

    // --- State for Bottom Sheet ---
    var selectedPoint by remember { mutableStateOf<MobilityPoint?>(null) }
    val sheetState = rememberModalBottomSheetState()

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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mapViewModel.setUserLocation(userLatLng)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            uiState.mobilityPoints.forEach { point ->
                val iconResourceId = when (point.type) {
                    "scooter" -> R.drawable.ic_scooter
                    "bike" -> R.drawable.ic_bike
                    else -> R.drawable.ic_scooter
                }
                val iconBitmap = bitmapDescriptorFromVector(context, iconResourceId)

                Marker(
                    state = MarkerState(position = point.location),
                    title = point.name,
                    snippet = "Type: ${point.type}",
                    icon = iconBitmap,
                    onClick = { // FIX: Changed from onInfoWindowClick to onClick
                        selectedPoint = point
                        false // Return false to also show the info window
                    }
                )
            }
        }

        // Show the Bottom Sheet when a point is selected
        if (selectedPoint != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedPoint = null },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(selectedPoint!!.name, style = MaterialTheme.typography.headlineSmall)
                    Text("Type: ${selectedPoint!!.type}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            navController.navigate("review/${selectedPoint!!.id}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Avaliar Local")
                    }
                }
            }
        }

        // Filter Buttons UI
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            FilterButtons(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { filter -> mapViewModel.setSelectedFilter(filter) }
            )
        }

        if (!hasLocationPermission) {
            Text(
                text = "Location permission is required to show the map.",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.errorMessage?.let {
            Text(
                text = it,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun FilterButtons(
    selectedFilter: String?,
    onFilterSelected: (MobilityTypeFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onFilterSelected(MobilityTypeFilter.ALL) },
            enabled = selectedFilter != MobilityTypeFilter.ALL.type
        ) {
            Text("All")
        }
        Button(
            onClick = { onFilterSelected(MobilityTypeFilter.SCOOTER) },
            enabled = selectedFilter != MobilityTypeFilter.SCOOTER.type
        ) {
            Text("Scooters")
        }
        Button(
            onClick = { onFilterSelected(MobilityTypeFilter.BIKE) },
            enabled = selectedFilter != MobilityTypeFilter.BIKE.type
        ) {
            Text("Bikes")
        }
    }
}
