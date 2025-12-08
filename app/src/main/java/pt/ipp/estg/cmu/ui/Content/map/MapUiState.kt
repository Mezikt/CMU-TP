package pt.ipp.estg.cmu.ui.Content.map // FIX: Corrected package name to match folder structure

import com.google.android.gms.maps.model.LatLng
import pt.ipp.estg.cmu.data.MobilityPoint

/**
 * Represents the state of the Map screen. This is the single source of truth.
 */
data class MapUiState(
    val mobilityPoints: List<MobilityPoint> = emptyList(),
    val selectedFilter: String? = MobilityTypeFilter.ALL.type,
    val searchQuery: String = "", // Added to hold the search query
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userLocation: LatLng? = null
)

/**
 * Enum representing the available filters for mobility points. This is the single source of truth.
 */
enum class MobilityTypeFilter(val type: String?) {
    ALL(null), // A null type represents all points
    SCOOTER("scooter"),
    BIKE("bike");

    companion object {
        fun fromType(type: String?): MobilityTypeFilter {
            return entries.find { it.type == type } ?: ALL
        }
    }
}
