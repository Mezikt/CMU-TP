package pt.ipp.estg.cmu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.database.UserProfileEntity

// UI State for the Friends Screen
data class FriendsUiState(
    val friends: List<UserProfileEntity> = emptyList(),
    val searchResults: List<UserProfileEntity> = emptyList(),
    val friendRequests: List<UserProfileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
)

class FriendsViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = FriendsUiState(isLoading = true)
            try {
                val friends = repository.getCurrentUserFriends()
                val requests = repository.getFriendRequests()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    friends = friends,
                    friendRequests = requests
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length > 2) { // Start searching after 3 characters
            searchUsers(query)
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val results = repository.searchUsers(query)
                _uiState.value = _uiState.value.copy(isLoading = false, searchResults = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            val result = repository.sendFriendRequest(userId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun acceptFriendRequest(userId: String) {
        viewModelScope.launch {
            val result = repository.acceptFriendRequest(userId)
            if (result.isSuccess) {
                loadInitialData() // Refresh data after accepting
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun declineFriendRequest(userId: String) {
        viewModelScope.launch {
            val result = repository.declineFriendRequest(userId)
            if (result.isSuccess) {
                loadInitialData() // Refresh data after declining
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: UserProfileRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
                return FriendsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}