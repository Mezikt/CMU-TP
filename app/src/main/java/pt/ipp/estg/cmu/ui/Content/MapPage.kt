package pt.ipp.estg.cmu.ui.Content

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.R // Import para aceder aos recursos (drawables)
import pt.ipp.estg.cmu.bitmapDescriptorFromVector // Import da nossa função auxiliar

@SuppressLint("MissingPermission")
@Composable
fun MapPage() {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )


    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f)
    }

    var mobilityPoints by remember { mutableStateOf<List<MobilityPoint>>(emptyList()) }


    LaunchedEffect(Unit) {
        val db = Firebase.firestore
        try {
            val result = db.collection("mobility_points").get().await()
            val points = result.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: ""
                val type = doc.getString("type") ?: ""
                val location = doc.getGeoPoint("location")
                if (location != null) {
                    MobilityPoint(
                        id = doc.id,
                        name = name,
                        type = type,
                        location = LatLng(location.latitude, location.longitude)
                    )
                } else {
                    null
                }
            }
            mobilityPoints = points
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {

                mobilityPoints.forEach { point ->

                    val iconResourceId = when (point.type) {
                        "scooter" -> R.drawable.ic_scooter
                        "bike" -> R.drawable.ic_bike
                        else -> null
                    }

                    val iconBitmap = iconResourceId?.let {
                        bitmapDescriptorFromVector(context, it)
                    }

                    Marker(
                        state = MarkerState(position = point.location),
                        title = point.name,
                        snippet = "Tipo: ${point.type}",
                        icon = iconBitmap
                    )
                }
            }
        } else {
            Text(
                text = "Permissão de localização necessária para mostrar o mapa.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}