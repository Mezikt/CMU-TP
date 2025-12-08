package pt.ipp.estg.cmu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.database.UserProfileEntity
import pt.ipp.estg.cmu.repository.UserProfileRepository

data class LeaderboardUiState(
    val users: List<UserProfileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LeaderboardViewModel(private val userRepository: UserProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        fetchLeaderboard()
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _uiState.value = LeaderboardUiState(isLoading = true)
            try {
                val users = userRepository.getLeaderboardUsers()
                _uiState.value = LeaderboardUiState(users = users)
            } catch (e: Exception) {
                _uiState.value = LeaderboardUiState(errorMessage = "Failed to load leaderboard: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: UserProfileRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
                return LeaderboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
