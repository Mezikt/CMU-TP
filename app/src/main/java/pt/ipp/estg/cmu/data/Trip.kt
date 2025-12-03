package pt.ipp.estg.cmu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a recorded trip.
 *
 * @property userId The ID of the user who made the trip.
 * @property distance The total distance of the trip in meters.
 * @property duration The total duration of the trip in seconds.
 * @property path A list of geographical coordinates representing the trip's path.
 *              Each point is stored as a map to be compatible with Firestore.
 * @property date The timestamp when the trip was completed.
 * @property points The number of points awarded for the trip.
 */
data class Trip(
    val userId: String = "",
    val distance: Double = 0.0,
    val duration: Long = 0L,
    val path: List<Map<String, Double>> = emptyList(),
    @ServerTimestamp
    val date: Timestamp? = null, // Firestore will automatically set this on the server
    val points: Int = 0
)
