package pt.ipp.estg.cmu.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.database.UserProfileDao
import pt.ipp.estg.cmu.database.UserProfileEntity

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    private val auth = Firebase.auth

    // Expõe um Flow com os dados do perfil a partir da base de dados local (Room)
    val userProfileFlow: Flow<UserProfileEntity?> = auth.currentUser?.uid?.let { userId ->
        userProfileDao.getUserProfile(userId)
    } ?: emptyFlow() // Se não houver utilizador, retorna um Flow vazio.


    // Função para forçar a atualização dos dados a partir da Firestore
    suspend fun refreshUserProfile() {
        // --- INÍCIO DA CORREÇÃO ---
        // Guarda o uid numa variável local para garantir o smart cast.
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            // --- FIM DA CORREÇÃO ---
            try {
                // Usa a variável local 'currentUserId' em vez da propriedade
                val document = Firebase.firestore.collection("users").document(currentUserId).get().await()
                if (document.exists()) {
                    val profile = UserProfileEntity(
                        uid = currentUserId, // Usa a variável local
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        points = document.getLong("points") ?: 0L
                    )
                    // Guarda os dados frescos na base de dados local
                    userProfileDao.upsertUserProfile(profile)
                }
            } catch (e: Exception) {
                // Lidar com erros de rede (ex: logar o erro)
                e.printStackTrace()
            }
        }
    }
}