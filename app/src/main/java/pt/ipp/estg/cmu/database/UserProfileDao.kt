package pt.ipp.estg.cmu.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    // FIX: Renamed to observeUserProfile and simplified to get the single user profile
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    // FIX: Added method to delete the user profile on logout
    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()
}
