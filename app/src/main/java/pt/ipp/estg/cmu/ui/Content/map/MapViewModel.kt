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

// FIX: Imports for the centralized data classes from MapState.kt
import pt.ipp.estg.cmu.ui.Content.map.MapUiState
import pt.ipp.estg.cmu.ui.Content.map.MobilityTypeFilter

class MapViewModel(
    private val repository: MobilityPointRepository
) : ViewModel() {

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    private val _selectedFilter = MutableStateFlow(MobilityTypeFilter.ALL)
    private val _searchQuery = MutableStateFlow("") // Added for search functionality
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // FIX: Restructure combine to handle more than 5 flows
    val uiState: StateFlow<MapUiState> = combine(
        repository.mobilityPoints,
        _selectedFilter,
        _searchQuery,
        _isLoading,
        _errorMessage
    ) { mobilityPoints, selectedFilter, searchQuery, isLoading, errorMessage ->
        val filteredPointsByType = if (selectedFilter == MobilityTypeFilter.ALL) {
            mobilityPoints
        } else {
            mobilityPoints.filter { it.type == selectedFilter.type }
        }

        val finalFilteredPoints = if (searchQuery.isBlank()) {
            filteredPointsByType
        } else {
            filteredPointsByType.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        // Use a tuple to pass the processed data to the next combine
        Triple(finalFilteredPoints, selectedFilter, searchQuery) to (isLoading to errorMessage)

    }.combine(_userLocation) { (processedData, loadingStatus), userLocation ->
        val (finalFilteredPoints, selectedFilter, searchQuery) = processedData
        val (isLoading, errorMessage) = loadingStatus

        // Construct the final MapUiState
        MapUiState(
            mobilityPoints = finalFilteredPoints,
            selectedFilter = selectedFilter.type,
            searchQuery = searchQuery,
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

    fun onSearchQueryChange(query: String) { // Function to update search query
        _searchQuery.value = query
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