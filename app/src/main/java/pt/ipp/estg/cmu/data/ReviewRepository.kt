package pt.ipp.estg.cmu.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val firestore: FirebaseFirestore) {

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
}