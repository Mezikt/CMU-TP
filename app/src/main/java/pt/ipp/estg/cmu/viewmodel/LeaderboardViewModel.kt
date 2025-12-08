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

enum class LeaderboardFilter { GLOBAL, FRIENDS }

data class LeaderboardUiState(
    val users: List<UserProfileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: LeaderboardFilter = LeaderboardFilter.GLOBAL
)

class LeaderboardViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun onFilterChanged(filter: LeaderboardFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val users = when (_uiState.value.selectedFilter) {
                    LeaderboardFilter.GLOBAL -> repository.getLeaderboardUsers()
                    LeaderboardFilter.FRIENDS -> repository.getCurrentUserFriends()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = users.sortedByDescending { it.points } // Always sort by points
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
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