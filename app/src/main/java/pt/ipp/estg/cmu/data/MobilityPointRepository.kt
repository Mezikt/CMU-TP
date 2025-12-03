package pt.ipp.estg.cmu.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.ui.Content.MobilityPoint // Importa sua data class MobilityPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobilityPointRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val _mobilityPoints = MutableStateFlow<List<MobilityPoint>>(emptyList())
    val mobilityPoints: StateFlow<List<MobilityPoint>> = _mobilityPoints.asStateFlow()

    suspend fun fetchMobilityPoints() {
        try {
            val result = firestore.collection("mobility_points").get().await()
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
            _mobilityPoints.value = points
        } catch (e: Exception) {
            // Lidar com erros, talvez logar ou emitir um erro para o ViewModel
            e.printStackTrace()
            // Você pode querer emitir um erro aqui ou retornar um Result<List<MobilityPoint>>
        }
    }
}