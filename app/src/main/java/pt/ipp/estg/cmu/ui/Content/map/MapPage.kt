package pt.ipp.estg.cmu.ui.Content.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import pt.ipp.estg.cmu.data.MobilityPointRepository
import pt.ipp.estg.cmu.data.ReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapPage(navController: NavController) {
    val context = LocalContext.current

    // --- Repositories and ViewModel --- 
    val mobilityPointRepository = remember { MobilityPointRepository(Firebase.firestore) }
    val reviewRepository = remember { ReviewRepository(Firebase.firestore) }
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModel.Factory(mobilityPointRepository, reviewRepository)
    )
    val uiState by mapViewModel.uiState.collectAsState()

    // --- Location Permission --- 
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

    // --- Map and Camera State --- 
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f)
    }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val userLatLng = location?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(41.1579, -8.6291)
                mapViewModel.setUserLocation(userLatLng)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
            }
        }
    }

    // --- UI --- 
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            uiState.mobilityPoints.forEach { point ->
                val icon = bitmapDescriptorFromVector(context, when (point.type) {
                    "scooter" -> R.drawable.ic_scooter
                    else -> R.drawable.ic_bike
                })
                Marker(
                    state = MarkerState(position = point.location),
                    title = point.name,
                    icon = icon,
                    onClick = { mapViewModel.onPointSelected(point); false }
                )
            }
        }

        // --- Bottom Sheet for Selected Point --- 
        uiState.selectedPointInfo?.let {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { mapViewModel.onBottomSheetDismissed() },
                sheetState = sheetState
            ) {
                PointDetailsSheet(
                    pointInfo = it,
                    onReviewClick = { navController.navigate("review/${it.point.id}") }
                )
            }
        }

        // --- Search and Filter UI --- 
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { mapViewModel.onSearchQueryChange(it) },
                label = { Text("Search by name...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterButtons(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { mapViewModel.setSelectedFilter(it) }
            )
        }

        // --- Loading/Error Indicators --- 
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.errorMessage?.let {
            Text(it, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun PointDetailsSheet(pointInfo: SelectedPointInfo, onReviewClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(pointInfo.point.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Type: ${pointInfo.point.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(8.dp))

        // Rating Display
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingBar(rating = pointInfo.averageRating)
            Spacer(modifier = Modifier.width(8.dp))
            Text("(${pointInfo.reviewCount} reviews)", style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onReviewClick, modifier = Modifier.fillMaxWidth()) {
            Text("Leave a Review")
        }
    }
}

@Composable
private fun RatingBar(rating: Float, maxRating: Int = 5) {
    Row {
        for (i in 1..maxRating) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Filled.StarBorder
            val tint = if (isSelected) Color(0xFFFFD700) else Color.Gray
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp) // Smaller stars for display
            )
        }
    }
}

@Composable
private fun FilterButtons(selectedFilter: String?, onFilterSelected: (MobilityTypeFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        MobilityTypeFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter.type
            Button(
                onClick = { onFilterSelected(filter) },
                colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                border = if(isSelected) null else ButtonDefaults.outlinedButtonBorder
            ) {
                Text(filter.name.lowercase().replaceFirstChar { it.titlecase() })
            }
        }
    }
}
