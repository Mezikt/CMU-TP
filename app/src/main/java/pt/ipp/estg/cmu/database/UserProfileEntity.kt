package pt.ipp.estg.cmu.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Represents a user profile, designed to be compatible with both Room and Firestore.
 *
 * The primary constructor contains only the fields persisted in the local Room database.
 * These fields are `var` to allow Room's code generator (KSP) to create the necessary setters,
 * resolving the "Cannot find setter for field" build error.
 *
 * Firestore-specific fields (e.g., `friends`) are declared outside the primary constructor and
 * marked with `@Ignore`. This prevents Room from trying to persist them, while Firestore can
 * still access and populate them as public properties.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var points: Long = 0L
) {
    @Ignore
    var friends: List<String> = emptyList()
    @Ignore
    var friendRequestsSent: List<String> = emptyList()
    @Ignore
    var friendRequestsReceived: List<String> = emptyList()
}
