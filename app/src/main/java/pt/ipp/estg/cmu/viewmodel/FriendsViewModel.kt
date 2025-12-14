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
            _uiState.value = _uiState.value.copy(isLoading = true) // Mantém o estado anterior mas mete loading
            try {
                // Assumo que o teu repositório sabe ler desta coleção "friendRequest"
                // Se isto estiver a devolver lista vazia, o problema está no repositório.
                val friends = repository.getCurrentUserFriends()
                val requests = repository.getFriendRequests()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    friends = friends,
                    friendRequests = requests,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load data: ${e.message}")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length > 2) {
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
                    // Verifica se já não existe um pedido igual antes de enviar
                    val query = db.collection("/friendRequest")
                        .whereEqualTo("from", user)
                        .whereEqualTo("receive", friendToAdd)
                        .get()
                        .await()

                    if (query.isEmpty) {
                        val doc = mapOf(
                            "from" to user,
                            "receive" to friendToAdd,
                            "status" to "pending",
                        )
                        db.collection("/friendRequest").add(doc).await()
                        _uiState.value = _uiState.value.copy(errorMessage = "Friend request sent!")
                    } else {
                        _uiState.value = _uiState.value.copy(errorMessage = "Request already sent.")
                    }
                } else {
                    throw Exception("User not authenticated")
                }
            } catch (e : Exception){
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    // --- CORREÇÃO AQUI ---
    fun acceptFriendRequest(userSentEmail: String) {
        Log.w(TAG, "Attempting to accept request from: $userSentEmail")
        viewModelScope.launch {
            try {
                val userRecieve = auth.currentUser?.email

                if(userRecieve != null){

                    // Procura o pedido na base de dados
                    val snapshot = db.collection("/friendRequest")
                        .whereEqualTo("from", userSentEmail)
                        .whereEqualTo("receive", userRecieve)
                        .whereEqualTo("status", "pending") // Garante que só aceita se estiver pendente
                        .limit(1)
                        .get()
                        .await()

                    // --- AQUI ESTAVA O ERRO ---
                    if (!snapshot.isEmpty) {
                        // Se encontrou, atualiza
                        val doc = snapshot.documents[0].reference
                        doc.update("status", "accepted").await()

                        // Atualiza a lista na UI
                        loadInitialData()
                    } else {
                        // Se não encontrou, avisa (pode ser problema de emails não baterem certo)
                        Log.e(TAG, "Document not found for $userSentEmail -> $userRecieve")
                        _uiState.value = _uiState.value.copy(errorMessage = "Request not found (Check emails)")
                    }

                } else {
                    throw Exception("User not authenticated")
                }
            } catch (e : Exception){
                Log.e(TAG, "Error accepting", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    // --- CORREÇÃO AQUI TAMBÉM ---
    // O teu código antigo usava repository com ID. Como estamos a usar emails,
    // temos de fazer a lógica aqui tal como no accept.
    fun declineFriendRequest(userSentEmail: String) {
        viewModelScope.launch {
            try {
                val userRecieve = auth.currentUser?.email
                if (userRecieve != null) {
                    val snapshot = db.collection("/friendRequest")
                        .whereEqualTo("from", userSentEmail)
                        .whereEqualTo("receive", userRecieve)
                        .limit(1)
                        .get()
                        .await()

                    if (!snapshot.isEmpty) {
                        // Ao recusar, normalmente apagamos o pedido ou mudamos para "declined"
                        snapshot.documents[0].reference.delete().await()
                        loadInitialData()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
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