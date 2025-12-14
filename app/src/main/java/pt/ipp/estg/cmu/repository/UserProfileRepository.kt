package pt.ipp.estg.cmu.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
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
                val profile = documentToUserProfile(document)
                userProfileDao.upsertUserProfile(profile)
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
            // FIX: Removed .orderBy("points") to avoid needing a specific Firestore index.
            // The sorting is now handled client-side in the ViewModel, which is more robust.
            val snapshot = usersCollection
                .limit(limit)
                .get()
                .await()
            snapshot.documents.map { documentToUserProfile(it) }
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

        val nameQuery = usersCollection
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .get().await()

        return nameQuery.documents
            .map { documentToUserProfile(it) }
            .filter { it.uid != currentUserId } // Exclude current user
    }

    suspend fun getUserByEmail(email: String): UserProfileEntity? {
        return try {
            val querySnapshot = usersCollection.whereEqualTo("email", email).limit(1).get().await()
            if (querySnapshot.isEmpty) {
                null
            } else {
                documentToUserProfile(querySnapshot.documents.first())
            }
        } catch (e: Exception) {
            null
        }
    }


    suspend fun acceptFriendRequest(doc: DocumentSnapshot): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))

        return try {


            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(friendId: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not signed in"))

        return try {
            // Similar to accept, you need to find and delete/update the specific request document.
            // Placeholder for now.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserFriends(): List<UserProfileEntity> {
        val currentUser = getCurrentUserDocument() ?: return emptyList()
        val friendIds = currentUser.friends

        if (friendIds.isEmpty()) return emptyList()

        val friendsQuery = usersCollection.whereIn("uid", friendIds).get().await()
        return friendsQuery.documents.map { documentToUserProfile(it) }
    }

    suspend fun getFriendRequests(): List<UserProfileEntity> {
        val currentUserEmail = auth.currentUser?.email ?: return emptyList()
        return try {
            val requestsSnapshot = firestore.collection("friendRequest")
                .whereEqualTo("receive", currentUserEmail)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            if (requestsSnapshot.isEmpty) return emptyList()

            val senderEmails = requestsSnapshot.documents.mapNotNull { it.getString("from") }
            if (senderEmails.isEmpty()) return emptyList()

            // Chunk the list of emails into sublists of 10
            val emailChunks = senderEmails.chunked(10)
            val users = mutableListOf<UserProfileEntity>()

            for (chunk in emailChunks) {
                val usersSnapshot = usersCollection.whereIn("email", chunk).get().await()
                users.addAll(usersSnapshot.documents.map { documentToUserProfile(it) })
            }
            users
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getCurrentUserDocument(): UserProfileEntity? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            usersCollection.document(userId).get().await().let { document ->
                if (document.exists()) documentToUserProfile(document) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun documentToUserProfile(document: DocumentSnapshot): UserProfileEntity {
        return UserProfileEntity().apply {
            uid = document.id
            name = document.getString("name") ?: ""
            email = document.getString("email") ?: ""
            points = document.getLong("points") ?: 0L
            friends = document.get("friends") as? List<String> ?: emptyList()
            friendRequestsReceived = document.get("friendRequestsReceived") as? List<String> ?: emptyList()
            friendRequestsSent = document.get("friendRequestsSent") as? List<String> ?: emptyList()
        }
    }
}