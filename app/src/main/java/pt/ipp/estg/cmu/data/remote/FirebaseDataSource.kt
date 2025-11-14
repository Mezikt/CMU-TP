package pt.ipp.estg.cmu.data.remote

import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.data.model.OperatorEntity
import pt.ipp.estg.cmu.data.model.TripEntity
import pt.ipp.estg.cmu.data.model.UserProfileEntity

/**
 * Firebase Remote Data Source
 * - Dados PÚBLICOS: operadores de mobilidade (Realtime Database)
 * - Dados PRIVADOS: viagens e perfis dos utilizadores (Firestore com regras de segurança)
 */
class FirebaseDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

    // ========== DADOS PÚBLICOS (Realtime Database - Pub/Sub) ==========

    /**
     * Observa operadores em tempo real (público para todos os utilizadores)
     * Pattern Publish/Subscribe
     */
    fun observePublicOperators(): Flow<List<OperatorEntity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val operators = snapshot.children.mapNotNull { child ->
                    try {
                        OperatorEntity(
                            id = child.key ?: return@mapNotNull null,
                            name = child.child("name").getValue(String::class.java) ?: "",
                            type = child.child("type").getValue(String::class.java) ?: "",
                            latitude = child.child("latitude").getValue(Double::class.java) ?: 0.0,
                            longitude = child.child("longitude").getValue(Double::class.java) ?: 0.0,
                            available = child.child("available").getValue(Boolean::class.java) ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(operators)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        realtimeDb.child("public_operators").addValueEventListener(listener)

        awaitClose {
            realtimeDb.child("public_operators").removeEventListener(listener)
        }
    }

    /**
     * Publica novos operadores (simulação - normalmente seria admin)
     */
    suspend fun publishOperator(operator: OperatorEntity) {
        realtimeDb.child("public_operators").child(operator.id).setValue(
            mapOf(
                "name" to operator.name,
                "type" to operator.type,
                "latitude" to operator.latitude,
                "longitude" to operator.longitude,
                "available" to operator.available,
                "updatedAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    // ========== DADOS PRIVADOS (Firestore com segurança) ==========

    /**
     * Observa viagens do utilizador (privado - apenas o próprio vê)
     * Pattern Publish/Subscribe com Firestore
     */
    fun observeUserTrips(userId: String): Flow<List<TripEntity>> = callbackFlow {
        val listener: ListenerRegistration = firestore
            .collection("users")
            .document(userId)
            .collection("trips")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val trips = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        TripEntity(
                            id = doc.id,
                            userId = userId,
                            startTime = doc.getLong("startTime") ?: 0L,
                            endTime = doc.getLong("endTime"),
                            distance = doc.getDouble("distance") ?: 0.0,
                            points = doc.getLong("points")?.toInt() ?: 0,
                            transportMode = doc.getString("transportMode") ?: "",
                            startLat = doc.getDouble("startLat") ?: 0.0,
                            startLon = doc.getDouble("startLon") ?: 0.0,
                            endLat = doc.getDouble("endLat"),
                            endLon = doc.getDouble("endLon"),
                            synced = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(trips)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Sincroniza viagem para Firebase (privado)
     */
    suspend fun syncTrip(trip: TripEntity) {
        firestore
            .collection("users")
            .document(trip.userId)
            .collection("trips")
            .document(trip.id)
            .set(
                mapOf(
                    "startTime" to trip.startTime,
                    "endTime" to trip.endTime,
                    "distance" to trip.distance,
                    "points" to trip.points,
                    "transportMode" to trip.transportMode,
                    "startLat" to trip.startLat,
                    "startLon" to trip.startLon,
                    "endLat" to trip.endLat,
                    "endLon" to trip.endLon,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    /**
     * Observa perfil do utilizador (privado)
     */
    fun observeUserProfile(userId: String): Flow<UserProfileEntity?> = callbackFlow {
        val listener = firestore
            .collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val profile = snapshot?.let { doc ->
                    try {
                        UserProfileEntity(
                            userId = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            totalPoints = doc.getLong("totalPoints")?.toInt() ?: 0,
                            totalTrips = doc.getLong("totalTrips")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                trySend(profile)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Atualiza perfil do utilizador
     */
    suspend fun updateUserProfile(profile: UserProfileEntity) {
        firestore
            .collection("users")
            .document(profile.userId)
            .set(
                mapOf(
                    "name" to profile.name,
                    "email" to profile.email,
                    "totalPoints" to profile.totalPoints,
                    "totalTrips" to profile.totalTrips,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}

