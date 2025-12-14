package pt.ipp.estg.cmu.ui.Content

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.data.TripRepository
import pt.ipp.estg.cmu.database.AppDatabase
import pt.ipp.estg.cmu.repository.UserProfileRepository
import pt.ipp.estg.cmu.viewmodel.TripRecordingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun TripRecordingPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application 

    val tripRepository = remember { TripRepository(Firebase.firestore) }
    val userProfileRepository = remember { UserProfileRepository(AppDatabase.getDatabase(context).userProfileDao())}

    val viewModel: TripRecordingViewModel = viewModel(
        factory = TripRecordingViewModel.Factory(application, tripRepository, userProfileRepository)
    )
    val uiState by viewModel.uiState.collectAsState()


    var isDark by remember { mutableStateOf(false) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val luxValue = it.values[0]
                    isDark = luxValue < 10f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (lightSensor != null) {
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }


    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.1579, -8.6291), 12f) // Default to Porto
    }

    LaunchedEffect(uiState.pathPoints) {
        uiState.pathPoints.lastOrNull()?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState.saveSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.msg_trip_saved),
                Toast.LENGTH_SHORT
            ).show()
            onNavigateBack()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_record_trip)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back)
                        )
                    }
                },
                colors = if (isDark) {
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                } else {
                    TopAppBarDefaults.topAppBarColors()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aspectRatio(1f),
            ) {
                if (hasLocationPermission) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true)
                    ) {
                        if (uiState.pathPoints.isNotEmpty()) {
                            Polyline(
                                points = uiState.pathPoints,
                                color = Color.Blue,
                                width = 10f
                            )
                        }
                    }
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(stringResource(R.string.msg_location_permission_needed))
                    }
                }

                DarkModeWarning(
                    isVisible = isDark,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoText(
                    label = stringResource(R.string.label_distance),
                    value = "%.2f km".format(uiState.distance / 1000)
                )
                InfoText(
                    label = stringResource(R.string.label_time),
                    value = formatElapsedTime(uiState.elapsedTime)
                )
            }

            Button(
                onClick = {
                    if (uiState.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.primary
                ),
                enabled = hasLocationPermission && !uiState.isSaving
            ) {
                when {
                    uiState.isSaving -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    uiState.isRecording -> Text(stringResource(R.string.btn_stop_recording))
                    else -> Text(stringResource(R.string.btn_start_recording))
                }
            }
        }
    }
}

@Composable
private fun DarkModeWarning(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.Yellow)
                Text(
                    text = stringResource(R.string.warn_dark_mode),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
    }
}

private fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}