package pt.ipp.estg.cmu.viewmodel

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.Trip
import pt.ipp.estg.cmu.data.TripRepository
import pt.ipp.estg.cmu.repository.UserProfileRepository

// UI State for the Trip Recording Screen
data class TripRecordingUiState(
    val isRecording: Boolean = false,
    val distance: Double = 0.0,
    val elapsedTime: Long = 0L,
    val pathPoints: List<LatLng> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@SuppressLint("MissingPermission")
class TripRecordingViewModel(
    private val tripRepository: TripRepository,
    private val userProfileRepository: UserProfileRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripRecordingUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val newLatLng = LatLng(location.latitude, location.longitude)
                val currentPath = _uiState.value.pathPoints.toMutableList()
                var currentDistance = _uiState.value.distance

                if (currentPath.isNotEmpty()) {
                    currentDistance += SphericalUtil.computeDistanceBetween(
                        currentPath.last(),
                        newLatLng
                    )
                }
                currentPath.add(newLatLng)

                _uiState.value = _uiState.value.copy(
                    pathPoints = currentPath,
                    distance = currentDistance
                )
            }
        }
    }

    fun startRecording() {
        // Reset state for a new trip
        _uiState.value = TripRecordingUiState(isRecording = true)

        // Start location updates
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // Start timer
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(elapsedTime = _uiState.value.elapsedTime + 1)
            }
        }
    }

    fun stopRecording() {
        // Stop timer and location updates
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val currentState = _uiState.value
        _uiState.value = currentState.copy(isRecording = false, isSaving = true)

        viewModelScope.launch {
            // Ensure there is enough data to save
            if (currentState.pathPoints.size <= 1) {
                _uiState.value = currentState.copy(isSaving = false, errorMessage = "Not enough data to save the trip.")
                return@launch
            }

            val userId = Firebase.auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = currentState.copy(isSaving = false, errorMessage = "User not authenticated.")
                return@launch
            }

            // --- Point Calculation ---
            var points = (currentState.distance / 100).toLong()
            if (currentState.distance < 5000) { // less than 5km
                points *= 2 // Double points
            }

            val pathForDb = currentState.pathPoints.map { mapOf("latitude" to it.latitude, "longitude" to it.longitude) }

            val trip = Trip(
                userId = userId,
                distance = currentState.distance,
                duration = currentState.elapsedTime,
                path = pathForDb,
                points = points.toInt()
            )

            // --- Save Trip and Update Points ---
            val tripResult = tripRepository.saveTrip(trip)
            if (tripResult.isSuccess) {
                val pointsResult = userProfileRepository.addPointsToCurrentUser(points)
                if (pointsResult.isSuccess) {
                    // FIX: Removed redundant refresh call. The repository now handles this automatically.
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Trip saved, but failed to update points.")
                }
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = tripResult.exceptionOrNull()?.message)
            }
        }
    }

    // Factory to create the ViewModel with its dependencies
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val tripRepository: TripRepository,
        private val userProfileRepository: UserProfileRepository,
        private val fusedLocationClient: FusedLocationProviderClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripRecordingViewModel::class.java)) {
                return TripRecordingViewModel(tripRepository, userProfileRepository, fusedLocationClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
