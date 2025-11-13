package pt.ipp.estg.cmu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.repository.UserProfileRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Inicializa o Repositório, passando-lhe o DAO da nossa base de dados
    private val repository: UserProfileRepository

    init {
        val userProfileDao = AppDatabase.getDatabase(application).userProfileDao()
        repository = UserProfileRepository(userProfileDao)
    }

    // Expõe o Flow de dados do Repositório para a UI observar
    val userProfile = repository.userProfileFlow

    // Quando o ViewModel é criado, ele pede ao repositório para ir buscar os dados mais frescos
    init {
        viewModelScope.launch {
            repository.refreshUserProfile()
        }
    }
}