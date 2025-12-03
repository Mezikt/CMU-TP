package pt.ipp.estg.cmu.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class TripRepository(private val firestore: FirebaseFirestore) {

    /**
     * Saves a trip to the Firestore database.
     *
     * @param trip The Trip object to be saved.
     * @return A Result object indicating success or failure.
     */
    suspend fun saveTrip(trip: Trip): Result<Unit> {
        return try {
            // "trips" is the name of our collection
            firestore.collection("trips").add(trip).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all trips for a specific user.
     *
     * @param userId The ID of the user whose trips are to be fetched.
     * @return A list of Trip objects.
     */
    suspend fun getUserTrips(userId: String): List<Trip> {
        return try {
            val snapshot = firestore.collection("trips")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<Trip>() }
        } catch (e: Exception) {
            emptyList() // Return an empty list in case of an error
        }
    }
}
