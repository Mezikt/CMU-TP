package pt.ipp.estg.cmu.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Represents a user profile as a regular class to resolve KSP build issues with data classes.
 * Using a plain class with `var` properties provides explicit setters that Room's code generator
 * can reliably work with, fixing the "Cannot find setter for field" error.
 */
@Entity(tableName = "user_profile")
class UserProfileEntity {
    @PrimaryKey
    var uid: String = ""
    var name: String = ""
    var email: String = ""
    var points: Long = 0L

    var photoUrl: String = ""

    @Ignore
    var friends: List<String> = emptyList()
    @Ignore
    var friendRequestsSent: List<String> = emptyList()
    @Ignore
    var friendRequestsReceived: List<String> = emptyList()
}