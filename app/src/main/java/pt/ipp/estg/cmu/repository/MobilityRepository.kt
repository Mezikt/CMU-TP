package pt.ipp.estg.cmu.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.local.AppDatabase
import pt.ipp.estg.cmu.data.model.OperatorEntity
import pt.ipp.estg.cmu.data.model.TripEntity
import pt.ipp.estg.cmu.data.model.UserProfileEntity
import pt.ipp.estg.cmu.data.remote.FirebaseDataSource
import pt.ipp.estg.cmu.network.MobilityApiService
import pt.ipp.estg.cmu.network.buildOverpassQuery

/**
 * Repository que combina:
 * - Room (cache local)
 * - Firebase (sincronização cloud com pub/sub)
 * - Retrofit (API REST externa)
 */
class MobilityRepository(
    private val database: AppDatabase,
    private val firebaseDataSource: FirebaseDataSource,
    private val apiService: MobilityApiService
) {
    private val tripDao = database.tripDao()
    private val operatorDao = database.operatorDao()
    private val userProfileDao = database.userProfileDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // ========== TRIPS (Room + Firebase sync) ==========

    fun getUserTrips(userId: String): Flow<List<TripEntity>> {
        // Inicia sincronização em background
        scope.launch {
            syncTripsFromFirebase(userId)
        }
        return tripDao.getAllTripsForUser(userId)
    }

    suspend fun saveTrip(trip: TripEntity) {
        // Guarda localmente
        tripDao.insert(trip)
        // Tenta sincronizar com Firebase
        try {
            firebaseDataSource.syncTrip(trip)
            tripDao.update(trip.copy(synced = true))
        } catch (e: Exception) {
            Log.e("MobilityRepository", "Erro ao sincronizar viagem", e)
        }
    }

    private suspend fun syncTripsFromFirebase(userId: String) {
        try {
            firebaseDataSource.observeUserTrips(userId).first().forEach { remoteTrip ->
                tripDao.insert(remoteTrip)
            }
        } catch (e: Exception) {
            Log.e("MobilityRepository", "Erro ao buscar viagens do Firebase", e)
        }
    }

    suspend fun syncPendingTrips() {
        val unsynced = tripDao.getUnsyncedTrips()
        unsynced.forEach { trip ->
            try {
                firebaseDataSource.syncTrip(trip)
                tripDao.update(trip.copy(synced = true))
            } catch (e: Exception) {
                Log.e("MobilityRepository", "Erro ao sincronizar viagem pendente", e)
            }
        }
    }

    // ========== OPERATORS (Room cache + Firebase pub/sub + Retrofit API) ==========

    fun getAvailableOperators(): Flow<List<OperatorEntity>> {
        // Subscreve updates do Firebase (dados públicos)
        scope.launch {
            firebaseDataSource.observePublicOperators().collect { remoteOperators ->
                operatorDao.insertAll(remoteOperators)
            }
        }
        return operatorDao.getAllAvailableOperators()
    }

    suspend fun fetchOperatorsFromApi(lat: Double, lon: Double) {
        try {
            val query = buildOverpassQuery(lat, lon)
            val response = apiService.getNearbyMobilityPoints(query)

            if (response.isSuccessful) {
                val operators = response.body()?.elements?.mapNotNull { element ->
                    if (element.lat != null && element.lon != null) {
                        OperatorEntity(
                            id = "api_${element.id}",
                            name = element.tags?.get("name") ?: "Unknown",
                            type = when (element.tags?.get("amenity")) {
                                "bicycle_rental" -> "bike"
                                "bicycle_parking" -> "bike"
                                else -> "other"
                            },
                            latitude = element.lat,
                            longitude = element.lon,
                            available = true
                        )
                    } else null
                } ?: emptyList()

                operatorDao.insertAll(operators)
            }
        } catch (e: Exception) {
            Log.e("MobilityRepository", "Erro ao buscar operadores da API", e)
        }
    }

    // ========== USER PROFILE (Room + Firebase) ==========

    fun getUserProfile(userId: String): Flow<UserProfileEntity?> {
        // Subscreve updates do Firebase
        scope.launch {
            firebaseDataSource.observeUserProfile(userId).collect { remoteProfile ->
                remoteProfile?.let { userProfileDao.insert(it) }
            }
        }
        return userProfileDao.getProfile(userId)
    }

    suspend fun updateUserStats(userId: String, points: Int) {
        userProfileDao.updateStats(userId, points)
        // Sincroniza com Firebase
        val profile = userProfileDao.getProfile(userId).first()
        profile?.let {
            firebaseDataSource.updateUserProfile(it)
        }
    }
}

