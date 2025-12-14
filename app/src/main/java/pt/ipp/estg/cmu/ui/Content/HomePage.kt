package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.data.WeatherApi
import pt.ipp.estg.cmu.data.WeatherResponse
import java.util.Locale

@Composable
fun HomePage(navController: NavHostController) {
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    val apiKey = "e0202c7e47881e77d0db9de64334eb63"

    LaunchedEffect(Unit) {
        try {
            val api = WeatherApi.create()
            weatherData = api.getCurrentWeather(
                lat = 41.1579,
                lon = -8.6291,
                apiKey = apiKey,
                lang = Locale.getDefault().language
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.title_welcome),
            style = MaterialTheme.typography.headlineMedium
        )


        if (weatherData != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = weatherData!!.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${weatherData!!.main.temp.toInt()}°C",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = weatherData!!.weather.firstOrNull()?.description
                                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = stringResource(R.string.desc_weather_icon),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.msg_loading_weather),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text(stringResource(R.string.label_search_transport)) },
            modifier = Modifier.fillMaxWidth()
        )


        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { navController.navigate("map") }) {
                Text(stringResource(R.string.btn_find_ride))
            }
            Button(onClick = { navController.navigate("trip_recording") }) {
                Text(stringResource(R.string.btn_record_trip))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.title_leaderboard),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.desc_leaderboard_promo))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("leaderboard") },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.btn_view_leaderboard))
                }
            }
        }
    }
}