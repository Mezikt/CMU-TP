package pt.ipp.estg.cmu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.Trip
import pt.ipp.estg.cmu.data.TripRepository

data class HistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HistoryViewModel(private val tripRepository: TripRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadUserTrips()
    }

    fun loadUserTrips() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val userId = Firebase.auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "User not authenticated. Cannot fetch history."
                )
                return@launch
            }

            try {
                val trips = tripRepository.getUserTrips(userId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    trips = trips.sortedByDescending { it.date } // Show most recent first
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load trip history: ${e.message}"
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: TripRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
