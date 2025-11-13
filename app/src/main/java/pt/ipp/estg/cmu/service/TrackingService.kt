package pt.ipp.estg.cmu.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.data.local.AppDatabase
import pt.ipp.estg.cmu.data.model.TripEntity
import pt.ipp.estg.cmu.data.remote.FirebaseDataSource
import pt.ipp.estg.cmu.network.MobilityApiService
import pt.ipp.estg.cmu.repository.MobilityRepository
import java.util.*
import kotlin.math.sqrt

/**
 * Serviço foreground que:
 * - Rastreia localização GPS
 * - Usa acelerómetro para detetar movimento
 * - Mostra notificação persistente
 * - Guarda viagens em Room e sincroniza com Firebase
 */
class TrackingService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var repository: MobilityRepository

    private var currentTripId: String? = null
    private var tripStartTime: Long = 0
    private var tripStartLocation: Location? = null
    private var totalDistance: Double = 0.0
    private var lastLocation: Location? = null

    // Acelerómetro
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var isMoving = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleLocationUpdate(location)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TrackingService criado")

        // Inicializar Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val firebaseDataSource = FirebaseDataSource()
        val apiService = MobilityApiService.create()
        repository = MobilityRepository(database, firebaseDataSource, apiService)

        // Localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        startLocationTracking()
        startAccelerometerTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTrip()
            ACTION_STOP_TRACKING -> stopTrip()
        }
        return START_STICKY
    }

    private fun startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permissão de localização não concedida")
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 segundos
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setMaxUpdateDelayMillis(20000L)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startAccelerometerTracking() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } ?: Log.w(TAG, "Acelerómetro não disponível")
    }

    private fun startTrip() {
        currentTripId = UUID.randomUUID().toString()
        tripStartTime = System.currentTimeMillis()
        totalDistance = 0.0
        tripStartLocation = null
        Log.d(TAG, "Viagem iniciada: $currentTripId")
    }

    private fun stopTrip() {
        currentTripId?.let { tripId ->
            val endTime = System.currentTimeMillis()
            val points = calculatePoints(totalDistance)

            val trip = TripEntity(
                id = tripId,
                userId = "user_demo", // TODO: usar Firebase Auth
                startTime = tripStartTime,
                endTime = endTime,
                distance = totalDistance,
                points = points,
                transportMode = "unknown", // TODO: inferir do acelerómetro
                startLat = tripStartLocation?.latitude ?: 0.0,
                startLon = tripStartLocation?.longitude ?: 0.0,
                endLat = lastLocation?.latitude,
                endLon = lastLocation?.longitude,
                synced = false
            )

            serviceScope.launch {
                repository.saveTrip(trip)
                repository.updateUserStats("user_demo", points)
            }

            Log.d(TAG, "Viagem terminada: $totalDistance km, $points pontos")
        }

        currentTripId = null
        stopSelf()
    }

    private fun handleLocationUpdate(location: Location) {
        if (currentTripId == null) return

        if (tripStartLocation == null) {
            tripStartLocation = location
        }

        lastLocation?.let { previous ->
            val distance = previous.distanceTo(location) / 1000.0 // km
            totalDistance += distance
        }

        lastLocation = location
        Log.d(TAG, "Localização: ${location.latitude}, ${location.longitude}, distância total: $totalDistance km")
    }

    private fun calculatePoints(distanceKm: Double): Int {
        // Lógica de pontos: dobrar para viagens < 5km
        return if (distanceKm < 5.0) {
            (distanceKm * 20).toInt() // 20 pontos/km * 2
        } else {
            (distanceKm * 10).toInt() // 10 pontos/km
        }
    }

    // ========== Sensor (Acelerómetro) ==========

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                lastAcceleration = currentAcceleration
                currentAcceleration = sqrt(x * x + y * y + z * z)
                val delta = currentAcceleration - lastAcceleration

                // Threshold para detetar movimento
                isMoving = delta > 2.0f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não necessário
    }

    // ========== Notificação ==========

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_tracking_title))
            .setContentText(getString(R.string.notification_tracking_text))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        Log.d(TAG, "TrackingService destruído")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "TrackingService"
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_START_TRACKING = "pt.ipp.estg.cmu.START_TRACKING"
        const val ACTION_STOP_TRACKING = "pt.ipp.estg.cmu.STOP_TRACKING"

        fun startService(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_START_TRACKING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }
}

