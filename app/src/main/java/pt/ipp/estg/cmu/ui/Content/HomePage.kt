package pt.ipp.estg.cmu.ui.Content

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
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
fun HomePage() {
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

    // Temos dois LaunchedEffect a correr com a mesma chave (Unit).
    // É melhor prática juntá-los para evitar comportamentos inesperados.
    LaunchedEffect(Unit) {
        // Pedir permissão de localização
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f)
    }

    var mobilityPoints by remember { mutableStateOf<List<MobilityPoint>>(emptyList()) }

    // Efeito para ir buscar os dados à Firestore uma vez
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

    // Este efeito agora depende da permissão para obter a localização
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
                // --- INÍCIO DA ALTERAÇÃO ---
                mobilityPoints.forEach { point ->
                    // 1. Determina qual o ID do recurso do ícone a usar
                    val iconResourceId = when (point.type) {
                        "scooter" -> R.drawable.ic_scooter
                        "bike" -> R.drawable.ic_bike
                        else -> null // Não mostra ícone se o tipo for desconhecido
                    }

                    // 2. Converte o ID do recurso num BitmapDescriptor que o mapa entende
                    val iconBitmap = iconResourceId?.let {
                        bitmapDescriptorFromVector(context, it)
                    }

                    // 3. Usa o ícone personalizado no Marcador
                    Marker(
                        state = MarkerState(position = point.location),
                        title = point.name,
                        snippet = "Tipo: ${point.type}",
                        icon = iconBitmap // Passa o nosso ícone aqui
                    )
                }
                // --- FIM DA ALTERAÇÃO ---
            }
        } else {
            Text(
                text = "Permissão de localização necessária para mostrar o mapa.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}