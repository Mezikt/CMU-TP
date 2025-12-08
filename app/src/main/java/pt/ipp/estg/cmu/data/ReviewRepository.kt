package pt.ipp.estg.cmu.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val firestore: FirebaseFirestore) {

    /**
     * Saves a review to the Firestore database.
     *
     * @param review The Review object to be saved.
     * @return A Result object indicating success or failure.
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
     *
     * @param pointId The ID of the mobility point.
     * @return A list of Review objects.
     */
    suspend fun getReviewsForPoint(pointId: String): List<Review> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("pointId", pointId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<Review>() }
        } catch (e: Exception) {
            // In case of an error, return an empty list
            emptyList()
        }
    }
}
