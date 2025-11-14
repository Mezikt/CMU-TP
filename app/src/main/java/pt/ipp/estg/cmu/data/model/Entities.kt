package pt.ipp.estg.cmu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long?,
    val distance: Double,
    val points: Int,
    val transportMode: String, // bicycle, scooter, bus, walking
    val startLat: Double,
    val startLon: Double,
    val endLat: Double?,
    val endLon: Double?,
    val synced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // scooter, bike, bus
    val latitude: Double,
    val longitude: Double,
    val available: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val totalPoints: Int,
    val totalTrips: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

