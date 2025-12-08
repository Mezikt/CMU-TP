package pt.ipp.estg.cmu.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Long = 0L
)
