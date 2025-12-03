package pt.ipp.estg.cmu.ui.Content.map

import com.google.android.gms.maps.model.LatLng
import pt.ipp.estg.cmu.ui.Content.MobilityPoint // Importa sua data class MobilityPoint

data class MapUiState(
    val mobilityPoints: List<MobilityPoint> = emptyList(),
    val selectedFilter: String? = null, // "scooter", "bike", "bus", null (para todos)
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userLocation: LatLng? = null // Para centralizar o mapa na localização do usuário
)

// Você pode criar uma enum para os tipos de filtro, o que é mais seguro
enum class MobilityTypeFilter(val type: String?) {
    ALL(null),
    SCOOTER("scooter"),
    BIKE("bike"),
    BUS("bus"); // Exemplo, se você adicionar autocarros

    companion object {
        fun fromType(type: String?): MobilityTypeFilter {
            return entries.find { it.type == type } ?: ALL
        }
    }
}