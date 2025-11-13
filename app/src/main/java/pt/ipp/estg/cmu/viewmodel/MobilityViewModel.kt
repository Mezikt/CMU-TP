package pt.ipp.estg.cmu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.local.AppDatabase
import pt.ipp.estg.cmu.data.model.OperatorEntity
import pt.ipp.estg.cmu.data.model.TripEntity
import pt.ipp.estg.cmu.data.model.UserProfileEntity
import pt.ipp.estg.cmu.data.remote.FirebaseDataSource
import pt.ipp.estg.cmu.network.MobilityApiService
import pt.ipp.estg.cmu.repository.MobilityRepository

class MobilityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MobilityRepository

    private val _trips = MutableStateFlow<List<TripEntity>>(emptyList())
    val trips: StateFlow<List<TripEntity>> = _trips.asStateFlow()

    private val _operators = MutableStateFlow<List<OperatorEntity>>(emptyList())
    val operators: StateFlow<List<OperatorEntity>> = _operators.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val firebaseDataSource = FirebaseDataSource()
        val apiService = MobilityApiService.create()
        repository = MobilityRepository(database, firebaseDataSource, apiService)

        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Carregar viagens
            repository.getUserTrips("user_demo").collect { tripList ->
                _trips.value = tripList
            }
        }

        viewModelScope.launch {
            // Carregar operadores
            repository.getAvailableOperators().collect { operatorList ->
                _operators.value = operatorList
            }
        }

        viewModelScope.launch {
            // Carregar perfil
            repository.getUserProfile("user_demo").collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    fun startTracking() {
        _isTracking.value = true
    }

    fun stopTracking() {
        _isTracking.value = false
    }

    fun fetchOperatorsNearby(lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.fetchOperatorsFromApi(lat, lon)
        }
    }

    fun syncPendingTrips() {
        viewModelScope.launch {
            repository.syncPendingTrips()
        }
    }
}

