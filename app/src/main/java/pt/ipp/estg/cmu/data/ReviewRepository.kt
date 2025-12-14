package pt.ipp.estg.cmu.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.database.UserProfileEntity

class ReviewRepository(private val firestore: FirebaseFirestore) {

    private val auth = Firebase.auth
    private val reviewsCollection = firestore.collection("reviews")

    /**
     * Saves a review to the Firestore database.
     */
    suspend fun saveReview(review: Review): Result<Unit> {
        return try {
            firestore.collection("reviews").add(review).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all reviews for a specific mobility point.
     */
    suspend fun getReviewsForPoint(pointId: String): List<Review> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("pointId", pointId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<Review>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Calculates the average rating and review count for a specific mobility point.
     */
    suspend fun getPointRatingSummary(pointId: String): Pair<Float, Int> {
        return try {
            val reviews = getReviewsForPoint(pointId)
            if (reviews.isEmpty()) {
                return 0f to 0
            }
            val average = reviews.map { it.rating }.average().toFloat()
            val count = reviews.size
            average to count
        } catch (e: Exception) {
            0f to 0
        }
    }

    suspend fun getReviewsFromUser(): List<Review>{
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        val reviewsSnapshot = firestore.collection("reviews")
            .whereEqualTo("userId", currentUserId)
            .get()
            .await()

        if (reviewsSnapshot.isEmpty) return emptyList()

        return reviewsSnapshot.documents.map { documentToReview(it) }

    }
    private fun documentToReview(document: DocumentSnapshot): Review {
        return Review().apply {
            pointId = document.getString("pointId") ?: ""
            userId = document.getString("userId") ?: ""
            rating = document.getLong("rating")?.toInt() ?: 0
            comment = document.getString("comment") ?: ""
            @ServerTimestamp
            date = document.getTimestamp("date")
        }
    }
}