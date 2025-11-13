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

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f)
    }

    // --- CÓDIGO NOVO ---
    // 1. Estado para guardar a lista de pontos de mobilidade
    var mobilityPoints by remember { mutableStateOf<List<MobilityPoint>>(emptyList()) }

    // 2. Efeito para ir buscar os dados à Firestore uma vez quando o Composable é criado
    LaunchedEffect(Unit) {
        val db = Firebase.firestore
        try {
            // Vai à coleção "mobility_points" e obtém todos os documentos
            val result = db.collection("mobility_points").get().await()
            // Mapeia cada documento para o nosso objeto de dados MobilityPoint
            val points = result.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: ""
                val type = doc.getString("type") ?: ""
                val location = doc.getGeoPoint("location")
                // Só cria o objeto se a localização existir
                if (location != null) {
                    MobilityPoint(
                        id = doc.id,
                        name = name,
                        type = type,
                        location = LatLng(location.latitude, location.longitude)
                    )
                } else {
                    null // Ignora documentos sem localização
                }
            }
            // Atualiza o estado com a lista de pontos
            mobilityPoints = points
        } catch (e: Exception) {
            // Pode adicionar um Toast ou Log para lidar com erros de rede
            e.printStackTrace()
        }
    }
    // --- FIM DO CÓDIGO NOVO ---

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
                // --- CÓDIGO NOVO ---
                // 3. Loop que desenha um marcador no mapa para cada ponto na nossa lista de estado
                mobilityPoints.forEach { point ->
                    Marker(
                        state = MarkerState(position = point.location),
                        title = point.name,
                        snippet = "Tipo: ${point.type}" // Texto que aparece ao clicar no marcador
                    )
                }
                // --- FIM DO CÓDIGO NOVO ---
            }
        } else {
            Text(
                text = "Permissão de localização necessária para mostrar o mapa.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}