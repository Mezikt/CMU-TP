package pt.ipp.estg.cmu.ui.Content.map

import com.google.android.gms.maps.model.LatLng
import pt.ipp.estg.cmu.data.MobilityPoint

/**
 * Represents the state of the Map screen.
 */
data class MapUiState(
    val mobilityPoints: List<MobilityPoint> = emptyList(),
    val selectedFilter: String? = MobilityTypeFilter.ALL.type,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userLocation: LatLng? = null,
    val selectedPointInfo: SelectedPointInfo? = null // Holds details for the bottom sheet
)

/**
 * Represents detailed information about a selected mobility point, including its rating.
 */
data class SelectedPointInfo(
    val point: MobilityPoint,
    val averageRating: Float = 0f,
    val reviewCount: Int = 0
)

/**
 * Enum representing the available filters for mobility points.
 */
enum class MobilityTypeFilter(val type: String?) {
    ALL(null),
    SCOOTER("scooter"),
    BIKE("bike");

    companion object {
        fun fromType(type: String?): MobilityTypeFilter {
            return entries.find { it.type == type } ?: ALL
        }
    }
}
