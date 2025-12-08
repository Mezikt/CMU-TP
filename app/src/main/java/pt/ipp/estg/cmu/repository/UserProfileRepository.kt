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
    private val usersCollection = firestore.collection("users")

    val userProfileFlow: Flow<UserProfileEntity?> = userProfileDao.observeUserProfile()

    suspend fun refreshUserProfile(): Result<Unit> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject<UserProfileEntity>()?.let {
                    userProfileDao.upsertUserProfile(it)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("User document does not exist in Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPointsToCurrentUser(points: Long): Result<Unit> {
        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated."))

        return try {
            val userDocRef = usersCollection.document(currentUserId)
            userDocRef.update("points", FieldValue.increment(points)).await()
            refreshUserProfile()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboardUsers(limit: Long = 100): List<UserProfileEntity> {
        return try {
            val snapshot = usersCollection
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

    suspend fun clearLocalData() {
        userProfileDao.deleteUserProfile()
    }

    // --- Friends Management --- //

    suspend fun searchUsers(query: String): List<UserProfileEntity> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        // Search by name (you can expand this to email)
        val nameQuery = usersCollection
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .get().await()

        return nameQuery.toObjects(UserProfileEntity::class.java)
            .filter { it.uid != currentUserId } // Exclude current user
    }

    suspend fun sendFriendRequest(friendId: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))

        return try {
            usersCollection.document(currentUserId).update("friendRequestsSent", FieldValue.arrayUnion(friendId)).await()
            usersCollection.document(friendId).update("friendRequestsReceived", FieldValue.arrayUnion(currentUserId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(friendId: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))

        return try {
            // Add to friends list for both users
            usersCollection.document(currentUserId).update("friends", FieldValue.arrayUnion(friendId)).await()
            usersCollection.document(friendId).update("friends", FieldValue.arrayUnion(currentUserId)).await()

            // Remove from requests
            usersCollection.document(currentUserId).update("friendRequestsReceived", FieldValue.arrayRemove(friendId)).await()
            usersCollection.document(friendId).update("friendRequestsSent", FieldValue.arrayRemove(currentUserId)).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(friendId: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))

        return try {
            usersCollection.document(currentUserId).update("friendRequestsReceived", FieldValue.arrayRemove(friendId)).await()
            usersCollection.document(friendId).update("friendRequestsSent", FieldValue.arrayRemove(currentUserId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserFriends(): List<UserProfileEntity> {
        val currentUser = getCurrentUserDocument() ?: return emptyList()
        val friendIds = currentUser.friends

        return if (friendIds.isNotEmpty()) {
            usersCollection.whereIn("uid", friendIds).get().await().toObjects(UserProfileEntity::class.java)
        } else {
            emptyList()
        }
    }

    suspend fun getFriendRequests(): List<UserProfileEntity> {
        val currentUser = getCurrentUserDocument() ?: return emptyList()
        val requestIds = currentUser.friendRequestsReceived

        return if (requestIds.isNotEmpty()) {
            usersCollection.whereIn("uid", requestIds).get().await().toObjects(UserProfileEntity::class.java)
        } else {
            emptyList()
        }
    }

    private suspend fun getCurrentUserDocument(): UserProfileEntity? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            usersCollection.document(userId).get().await().toObject(UserProfileEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
