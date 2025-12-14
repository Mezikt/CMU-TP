package pt.ipp.estg.cmu.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository responsible for fetching mobility point data from Firestore.
 */
class MobilityPointRepository(private val firestore: FirebaseFirestore) {

    private val _mobilityPoints = MutableStateFlow<List<MobilityPoint>>(emptyList())
    val mobilityPoints: StateFlow<List<MobilityPoint>> = _mobilityPoints.asStateFlow()

    /**
     * Fetches mobility points from the "mobility_points" collection in Firestore
     * and updates the state flow.
     */
    suspend fun fetchMobilityPoints() {
        try {
            val snapshot = firestore.collection("mobility_points").get().await()
            val points = snapshot.documents.mapNotNull { doc ->
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
            e.printStackTrace()
        }
    }
}
