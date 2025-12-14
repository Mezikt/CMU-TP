package pt.ipp.estg.cmu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.repository.UserProfileRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserProfileRepository

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        val userProfileDao = AppDatabase.getDatabase(application).userProfileDao()
        repository = UserProfileRepository(userProfileDao)
        refreshProfile()
    }

    val userProfile = repository.userProfileFlow

    fun refreshProfile() {
        viewModelScope.launch {
            val result = repository.refreshUserProfile()
            if (result.isFailure) {
                _errorMessage.update { result.exceptionOrNull()?.message }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            repository.clearLocalData()
        }
    }
}
