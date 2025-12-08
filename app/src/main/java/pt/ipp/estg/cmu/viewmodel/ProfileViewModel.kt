package pt.ipp.estg.cmu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.repository.UserProfileRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserProfileRepository

    init {
        val userProfileDao = AppDatabase.getDatabase(application).userProfileDao()
        repository = UserProfileRepository(userProfileDao)
    }

    // A UI irá simplesmente observar este Flow.
    // O repositório agora atualiza-se automaticamente com base na autenticação.
    val userProfile = repository.userProfileFlow

    // FIX: O bloco init que chamava refreshUserProfile() foi removido.
    // A lógica de atualização agora é gerida internamente pelo UserProfileRepository,
    // que ouve as mudanças no estado de autenticação.
}
