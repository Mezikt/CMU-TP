package pt.ipp.estg.cmu.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()
}
