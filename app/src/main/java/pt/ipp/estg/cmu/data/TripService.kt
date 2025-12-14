package pt.ipp.estg.cmu.data

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ipp.estg.cmu.R

class TripService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        private val _isTracking = MutableStateFlow(false)
        val isTracking = _isTracking.asStateFlow()

        private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
        val pathPoints = _pathPoints.asStateFlow()

        private val _distance = MutableStateFlow(0.0)
        val distance = _distance.asStateFlow()

        private val _elapsedTime = MutableStateFlow(0L)
        val elapsedTime = _elapsedTime.asStateFlow()

        fun resetTripData() {
            _pathPoints.value = emptyList()
            _distance.value = 0.0
            _elapsedTime.value = 0L
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { location ->
                    val newPoint = LatLng(location.latitude, location.longitude)


                    val currentList = _pathPoints.value.toMutableList()

                    if (currentList.isNotEmpty()) {
                        val lastPoint = currentList.last()
                        val dist = SphericalUtil.computeDistanceBetween(lastPoint, newPoint)
                        _distance.value += dist
                    }

                    currentList.add(newPoint)
                    _pathPoints.value = currentList
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TRACKING" -> startForegroundService()
            "STOP_TRACKING" -> stopForegroundService()
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundService() {
        resetTripData()
        _isTracking.value = true


        createNotificationChannel()
        val notification = buildNotification()

        startForeground(1, notification)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        startTimer()
    }

    private fun stopForegroundService() {
        _isTracking.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        serviceScope.launch {
            while (_isTracking.value) {
                delay(1000)
                _elapsedTime.value += 1
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "trip_channel",
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW // Low para não fazer barulho constante
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "trip_channel")
            .setContentTitle("SoftMobility")
            .setContentText("A gravar a tua viagem... 🚲")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}