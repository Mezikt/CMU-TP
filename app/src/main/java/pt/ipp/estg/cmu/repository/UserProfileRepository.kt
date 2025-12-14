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
    private val friendRequestCollection = firestore.collection("friendRequest")

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

    suspend fun getCurrentUserProfile(): UserProfileEntity? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) documentToUserProfile(doc) else null
        } catch (e: Exception) { null }
    }

    suspend fun clearLocalData() {
        userProfileDao.deleteUserProfile()
    }

    suspend fun searchUsers(queryText: String): List<UserProfileEntity> {
        return try {
            val querySnapshot = usersCollection
                .whereGreaterThanOrEqualTo("name", queryText)
                .whereLessThanOrEqualTo("name", queryText + "\uf8ff")
                .get()
                .await()

            querySnapshot.documents.map { documentToUserProfile(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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

    suspend fun sendFriendRequest(friendEmail: String): Result<Unit> {
        val currentUserEmail = auth.currentUser?.email ?: return Result.failure(Exception("No Email"))

        return try {
            val exists = friendRequestCollection
                .whereEqualTo("from", currentUserEmail)
                .whereEqualTo("receive", friendEmail)
                .get().await()

            if (!exists.isEmpty) return Result.failure(Exception("Request already sent"))

            val requestMap = mapOf(
                "from" to currentUserEmail,
                "receive" to friendEmail,
                "status" to "pending"
            )
            friendRequestCollection.add(requestMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(friendUid: String): Result<Unit> {
        val currentUserEmail = auth.currentUser?.email ?: return Result.failure(Exception("Not signed in"))

        return try {
            val friendDoc = usersCollection.document(friendUid).get().await()
            val friendEmail = friendDoc.getString("email") ?: return Result.failure(Exception("Friend email not found"))

            val snapshot = friendRequestCollection
                .whereEqualTo("from", friendEmail)
                .whereEqualTo("receive", currentUserEmail)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            if (snapshot.isEmpty) return Result.failure(Exception("Request not found"))

            for (doc in snapshot.documents) {
                doc.reference.update("status", "accepted").await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(friendUid: String): Result<Unit> {
        val currentUserEmail = auth.currentUser?.email ?: return Result.failure(Exception("Not signed in"))

        return try {
            val friendDoc = usersCollection.document(friendUid).get().await()
            val friendEmail = friendDoc.getString("email") ?: return Result.failure(Exception("Friend email not found"))

            val snapshot = friendRequestCollection
                .whereEqualTo("from", friendEmail)
                .whereEqualTo("receive", currentUserEmail)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserFriends(): List<UserProfileEntity> {
        val currentUserEmail = auth.currentUser?.email ?: return emptyList()
        val friendEmails = mutableSetOf<String>()

        try {
            val sentQuery = friendRequestCollection
                .whereEqualTo("from", currentUserEmail)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            for (doc in sentQuery) {
                doc.getString("receive")?.let { friendEmails.add(it) }
            }

            val receivedQuery = friendRequestCollection
                .whereEqualTo("receive", currentUserEmail)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            for (doc in receivedQuery) {
                doc.getString("from")?.let { friendEmails.add(it) }
            }

            val friendsList = mutableListOf<UserProfileEntity>()
            for (email in friendEmails) {
                val friendProfile = getUserByEmail(email)
                if (friendProfile != null) {
                    friendsList.add(friendProfile)
                }
            }
            return friendsList

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getFriendRequests(): List<UserProfileEntity> {
        val currentUserEmail = auth.currentUser?.email ?: return emptyList()
        return try {
            val requestsSnapshot = friendRequestCollection
                .whereEqualTo("receive", currentUserEmail)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            if (requestsSnapshot.isEmpty) return emptyList()

            val senderEmails = requestsSnapshot.documents.mapNotNull { it.getString("from") }
            val users = mutableListOf<UserProfileEntity>()
            for (email in senderEmails) {
                val user = getUserByEmail(email)
                if (user != null) users.add(user)
            }
            users
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun documentToUserProfile(document: DocumentSnapshot): UserProfileEntity {
        return UserProfileEntity().apply {
            uid = document.id
            name = document.getString("name") ?: "Unknown"
            email = document.getString("email") ?: ""
            points = document.getLong("points") ?: 0L
            friends = emptyList()
            friendRequestsReceived = emptyList()
            friendRequestsSent = emptyList()
        }
    }
}