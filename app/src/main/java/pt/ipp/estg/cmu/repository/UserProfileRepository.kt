package pt.ipp.estg.cmu.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.database.UserProfileDao
import pt.ipp.estg.cmu.database.UserProfileEntity

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    val userProfileFlow: Flow<UserProfileEntity?> = userProfileDao.observeUserProfile()

    /**
     * Fetches the latest user profile from Firestore and updates the local database.
     * FIX: Now returns a Result to propagate errors to the ViewModel.
     */
    suspend fun refreshUserProfile(): Result<Unit> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return Result.failure(Exception("User not authenticated"))
        }

        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                document.toObject<UserProfileEntity>()?.let {
                    userProfileDao.upsertUserProfile(it)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("User document does not exist in Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e) // Propagate any exception
        }
    }

    /**
     * Adds points to the current user's profile in Firestore and refreshes local data.
     */
    suspend fun addPointsToCurrentUser(points: Long): Result<Unit> {
        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated."))

        return try {
            val userDocRef = firestore.collection("users").document(currentUserId)
            userDocRef.update("points", FieldValue.increment(points)).await()
            refreshUserProfile() // Refresh local data after updating points
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a list of users for the leaderboard, sorted by points.
     */
    suspend fun getLeaderboardUsers(limit: Long = 100): List<UserProfileEntity> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.toObjects(UserProfileEntity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Deletes user data from the local database upon logout.
     */
    suspend fun clearLocalData() {
        userProfileDao.deleteUserProfile()
    }
}
