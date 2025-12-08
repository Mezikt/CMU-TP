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
import pt.ipp.estg.cmu.data.MobilityPoint
import pt.ipp.estg.cmu.data.MobilityPointRepository
import pt.ipp.estg.cmu.data.ReviewRepository

class MapViewModel(
    private val mobilityPointRepository: MobilityPointRepository,
    private val reviewRepository: ReviewRepository // Injected repository
) : ViewModel() {

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    private val _selectedFilter = MutableStateFlow(MobilityTypeFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _selectedPointInfo = MutableStateFlow<SelectedPointInfo?>(null)

    // FIX: Restructured combine to handle 7 flows by chaining them.
    // The combine function can only take a maximum of 6 flows at a time.
    val uiState: StateFlow<MapUiState> = combine(
        mobilityPointRepository.mobilityPoints,
        _selectedFilter,
        _searchQuery,
        _isLoading,
        _errorMessage
    ) { points, filter, query, loading, error ->
        // Create an intermediate object to hold the first 5 values
        object {
            val points = points
            val filter = filter
            val query = query
            val loading = loading
            val error = error
        }
    }.combine(_userLocation) { intermediate, location ->
        object { // Create another intermediate object
            val intermediate = intermediate
            val location = location
        }
    }.combine(_selectedPointInfo) { intermediateWithLocation, selectedInfo -> // Combine with the 7th flow
        val intermediate = intermediateWithLocation.intermediate
        val location = intermediateWithLocation.location

        val filteredPoints = intermediate.points.filter {
            (intermediate.filter == MobilityTypeFilter.ALL || it.type == intermediate.filter.type) &&
            (intermediate.query.isBlank() || it.name.contains(intermediate.query, ignoreCase = true))
        }
        MapUiState(
            mobilityPoints = filteredPoints,
            selectedFilter = intermediate.filter.type,
            searchQuery = intermediate.query,
            isLoading = intermediate.loading,
            errorMessage = intermediate.error,
            userLocation = location,
            selectedPointInfo = selectedInfo
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
                mobilityPointRepository.fetchMobilityPoints()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load points: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: MobilityTypeFilter) {
        _selectedFilter.value = filter
    }

    fun setUserLocation(location: LatLng) {
        _userLocation.value = location
    }

    fun onPointSelected(point: MobilityPoint) {
        viewModelScope.launch {
            _selectedPointInfo.value = SelectedPointInfo(point, 0f, 0) // Show basic info immediately
            val (avgRating, reviewCount) = reviewRepository.getPointRatingSummary(point.id)
            _selectedPointInfo.value = SelectedPointInfo(point, avgRating, reviewCount) // Update with rating
        }
    }

    fun onBottomSheetDismissed() {
        _selectedPointInfo.value = null
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val mobilityPointRepository: MobilityPointRepository,
        private val reviewRepository: ReviewRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(mobilityPointRepository, reviewRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
