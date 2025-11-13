package pt.ipp.estg.cmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ipp.estg.cmu.data.model.OperatorEntity
import pt.ipp.estg.cmu.data.model.TripEntity
import pt.ipp.estg.cmu.data.model.UserProfileEntity

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity)

    @Update
    suspend fun update(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllTripsForUser(userId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE synced = 0")
    suspend fun getUnsyncedTrips(): List<TripEntity>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: String): TripEntity?

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: String)
}

@Dao
interface OperatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(operators: List<OperatorEntity>)

    @Query("SELECT * FROM operators WHERE available = 1")
    fun getAllAvailableOperators(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators WHERE type = :type AND available = 1")
    fun getOperatorsByType(type: String): Flow<List<OperatorEntity>>

    @Query("DELETE FROM operators")
    suspend fun deleteAll()
}

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getProfile(userId: String): Flow<UserProfileEntity?>

    @Query("UPDATE user_profile SET totalPoints = totalPoints + :points, totalTrips = totalTrips + 1 WHERE userId = :userId")
    suspend fun updateStats(userId: String, points: Int)
}

