package pt.ipp.estg.cmu.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    // Upsert = (UP)date + in(SERT). Insere um novo perfil se não existir, ou atualiza um existente.
    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    // Query para ler o perfil de um utilizador específico pelo seu ID.
    // Retorna um Flow, que permite à UI observar alterações nos dados em tempo real.
    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    fun getUserProfile(uid: String): Flow<UserProfileEntity?>
}