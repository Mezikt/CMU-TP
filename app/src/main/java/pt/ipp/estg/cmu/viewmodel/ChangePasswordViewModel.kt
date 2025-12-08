package pt.ipp.estg.cmu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Represents the state of the change password screen
data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class ChangePasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.value = ChangePasswordUiState(error = "New passwords do not match.")
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = ChangePasswordUiState(error = "New password must be at least 6 characters long.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState(isLoading = true)
            try {
                val user = auth.currentUser
                if (user != null && user.email != null) {
                    // Re-authenticate the user with their current password
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    user.reauthenticate(credential).await()

                    // If re-authentication is successful, update the password
                    user.updatePassword(newPassword).await()
                    _uiState.value = ChangePasswordUiState(success = true)
                } else {
                    _uiState.value = ChangePasswordUiState(error = "User not authenticated.")
                }
            } catch (e: Exception) {
                // Handle exceptions, e.g., wrong password
                _uiState.value = ChangePasswordUiState(error = "Failed to change password: ${e.message}")
            }
        }
    }
    
    // Function to reset the error state, so the error message is only shown once
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
