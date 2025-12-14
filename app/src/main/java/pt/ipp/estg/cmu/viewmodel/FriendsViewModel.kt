package pt.ipp.estg.cmu.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    val auth = Firebase.auth
    val db = Firebase.firestore

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = FriendsUiState(isLoading = true)
            try {
                val friends = repository.getCurrentUserFriends()
                val requests = repository.getFriendRequests()
//                Log.d("FriendsViewModel", "Successfully loaded friends and requests.")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    friends = friends,
                    friendRequests = requests
                )
            } catch (e: Exception) {
//                Log.e("FriendsViewModel", "Error loading initial data", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load data: ${e.message}")
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

    fun sendFriendRequest(friendToAdd: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser?.email

                if(user != null){
                    val db = Firebase.firestore
                    val doc = mapOf(
                        "from" to user,
                        "receive" to friendToAdd,
                        "status" to "pending",
                    )

                    db.collection("/friendRequest").add(doc).await()
                }else{
                    throw Exception("User not authenticated")
                }
            }catch (e : Exception){
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun acceptFriendRequest(userSentEmail: String) {
        Log.w(TAG, "Entrou no ViewModel acceptFriendRequest")
        viewModelScope.launch {
            try {
                val userRecieve = auth.currentUser?.email

                if(userRecieve != null){
                    val db = Firebase.firestore

                    val acceptSnapshot = db.collection("/friendRequest")
                        .whereEqualTo("from", userSentEmail)
                        .whereEqualTo("receive", userRecieve)
                        .limit(1)

                    var doc = acceptSnapshot.get().await().documents[0].reference;

                    doc.update("status", "accepted")

                }else{
                    throw Exception("User not authenticated")
                }
            }catch (e : Exception){
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
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