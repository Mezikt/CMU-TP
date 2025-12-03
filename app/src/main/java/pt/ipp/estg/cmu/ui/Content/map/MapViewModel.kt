package pt.ipp.estg.cmu.ui.Content.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.MobilityPointRepository

// As classes de estado e filtro foram removidas para evitar redeclaração.
// Presume-se que estão definidas num ficheiro de modelo de dados (ex: /data/MapModels.kt)

class MapViewModel(
    private val repository: MobilityPointRepository
) : ViewModel() {

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    private val _selectedFilter = MutableStateFlow(MobilityTypeFilter.ALL)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        repository.mobilityPoints,
        _selectedFilter,
        _isLoading,
        _errorMessage,
        _userLocation
    ) { mobilityPoints, selectedFilter, isLoading, errorMessage, userLocation ->
        val filteredPoints = if (selectedFilter == MobilityTypeFilter.ALL) {
            mobilityPoints
        } else {
            mobilityPoints.filter { it.type == selectedFilter.type }
        }
        MapUiState(
            mobilityPoints = filteredPoints,
            selectedFilter = selectedFilter.type,
            isLoading = isLoading,
            errorMessage = errorMessage,
            userLocation = userLocation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState(isLoading = true)
    )

    init {
        loadMobilityPoints()
    }

    fun loadMobilityPoints() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.fetchMobilityPoints()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load mobility points: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedFilter(filter: MobilityTypeFilter) {
        _selectedFilter.value = filter
    }

    fun setUserLocation(location: LatLng) {
        _userLocation.value = location
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: MobilityPointRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}