package pt.ipp.estg.cmu.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String, // ID do utilizador, será a nossa chave primária
    val name: String,
    val email: String,
    val points: Long
)