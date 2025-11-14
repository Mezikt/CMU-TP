package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.viewmodel.MobilityViewModel

@Composable
fun MapPage() {
    val viewModel: MobilityViewModel = viewModel()
    val operators by viewModel.operators.collectAsState()

    // Localização padrão (Porto, Portugal)
    val defaultLocation = LatLng(41.1579, -8.6291)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Título
        TopAppBar(
            title = { Text(stringResource(R.string.map_title)) },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        )

        // Mapa
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false // Ativar quando tiver permissões
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                )
            ) {
                // Marcadores de operadores
                operators.forEach { operator ->
                    Marker(
                        state = MarkerState(position = LatLng(operator.latitude, operator.longitude)),
                        title = operator.name,
                        snippet = "${operator.type} - ${if (operator.available) "Disponível" else "Indisponível"}"
                    )
                }
            }

            // FAB para atualizar operadores
            FloatingActionButton(
                onClick = {
                    val currentPos = cameraPositionState.position.target
                    viewModel.fetchOperatorsNearby(currentPos.latitude, currentPos.longitude)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("🔄")
            }
        }
    }
}
