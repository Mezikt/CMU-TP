package pt.ipp.estg.cmu.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase // FIX: Correct import for the Firebase object
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.database.UserProfileDao
import pt.ipp.estg.cmu.database.UserProfileEntity

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    // Este flow observa diretamente a base de dados local.
    val userProfileFlow: Flow<UserProfileEntity?> = userProfileDao.observeUserProfile()

    init {
        // Ouve ativamente as mudanças de autenticação usando um listener.
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    refreshUserProfile(firebaseUser.uid)
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    userProfileDao.deleteUserProfile()
                }
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    private suspend fun refreshUserProfile(userId: String) {
        try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val profile = document.toObject<UserProfileEntity>()
                if (profile != null) {
                    userProfileDao.upsertUserProfile(profile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addPointsToCurrentUser(points: Long): Result<Unit> {
        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated."))

        return try {
            val userDocRef = firestore.collection("users").document(currentUserId)
            userDocRef.update("points", FieldValue.increment(points)).await()
            refreshUserProfile(currentUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
}
