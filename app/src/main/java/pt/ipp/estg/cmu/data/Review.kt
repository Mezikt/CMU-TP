package pt.ipp.estg.cmu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a review for a mobility point.
 *
 * @property pointId The ID of the mobility point being reviewed.
 * @property userId The ID of the user who submitted the review.
 * @property rating The star rating given by the user (e.g., 1 to 5).
 * @property comment An optional text comment from the user.
 * @property date The timestamp when the review was submitted.
 */
data class Review(
    val pointId: String = "",
    val userId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    @ServerTimestamp
    val date: Timestamp? = null // Firestore will automatically set this
)
