package pt.ipp.estg.cmu.data

import com.google.android.gms.maps.model.LatLng

/**
 * Data class representing a single mobility point for the UI.
 *
 * @property id The unique ID from Firestore.
 * @property name The name of the location.
 * @property type The type of mobility (e.g., "scooter", "bike").
 * @property location The geographical coordinates as a LatLng object for map usage.
 */
data class MobilityPoint(
    val id: String,
    val name: String,
    val type: String,
    val location: LatLng
)
