package pt.ipp.estg.cmu.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.Trip
import pt.ipp.estg.cmu.data.TripRepository
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.data.TripService

data class TripRecordingUiState(
    val isRecording: Boolean = false,
    val distance: Double = 0.0,
    val elapsedTime: Long = 0L,
    val pathPoints: List<LatLng> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class TripRecordingViewModel(
    private val application: Application,
    private val tripRepository: TripRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripRecordingUiState())
    val uiState = _uiState.asStateFlow()

    private val portoCenterBounds = object {
        private val sw = LatLng(41.14, -8.63)
        private val ne = LatLng(41.16, -8.60)
        fun contains(point: LatLng): Boolean {
            return point.latitude >= sw.latitude && point.latitude <= ne.latitude &&
                    point.longitude >= sw.longitude && point.longitude <= ne.longitude
        }
    }

    init {
        viewModelScope.launch {
            launch {
                TripService.distance.collect { dist ->
                    _uiState.value = _uiState.value.copy(distance = dist)
                }
            }
            launch {
                TripService.elapsedTime.collect { time ->
                    _uiState.value = _uiState.value.copy(elapsedTime = time)
                }
            }
            launch {
                TripService.pathPoints.collect { points ->
                    _uiState.value = _uiState.value.copy(pathPoints = points)
                }
            }
            launch {
                TripService.isTracking.collect { tracking ->
                    _uiState.value = _uiState.value.copy(isRecording = tracking)
                }
            }
        }
    }

    fun startRecording() {
        Intent(application, TripService::class.java).also { intent ->
            intent.action = "START_TRACKING"
            application.startService(intent)
        }
    }

    fun stopRecording() {
        Intent(application, TripService::class.java).also { intent ->
            intent.action = "STOP_TRACKING"
            application.startService(intent)
        }

        saveTripData()
    }

    private fun saveTripData() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isSaving = true)

        viewModelScope.launch {
            if (currentState.pathPoints.size <= 1) {
                _uiState.value = currentState.copy(isSaving = false, errorMessage = "Not enough data to save.")
                return@launch
            }

            val userId = Firebase.auth.currentUser?.uid ?: return@launch

            var points = (currentState.distance / 100).toLong()
            if (currentState.distance < 5000) { // Regra < 5km
                points *= 2
            }
            if (currentState.pathPoints.any { portoCenterBounds.contains(it) }) {
                points += 50
            }


            val pathForDb = currentState.pathPoints.map { mapOf("latitude" to it.latitude, "longitude" to it.longitude) }

            val trip = Trip(
                userId = userId,
                distance = currentState.distance,
                duration = currentState.elapsedTime,
                path = pathForDb,
                points = points.toInt()
            )


            val tripResult = tripRepository.saveTrip(trip)
            if (tripResult.isSuccess) {
                userProfileRepository.addPointsToCurrentUser(points)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = tripResult.exceptionOrNull()?.message)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val tripRepository: TripRepository,
        private val userProfileRepository: UserProfileRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripRecordingViewModel(application, tripRepository, userProfileRepository) as T
        }
    }
}