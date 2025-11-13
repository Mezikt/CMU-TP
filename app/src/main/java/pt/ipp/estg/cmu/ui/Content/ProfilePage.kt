package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.util.AndroidIntents
import pt.ipp.estg.cmu.viewmodel.MobilityViewModel

@Composable
fun ProfilePage() {
    val context = LocalContext.current
    val viewModel: MobilityViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.profile_title)) },
            backgroundColor = MaterialTheme.colors.primaryVariant,
            contentColor = MaterialTheme.colors.onPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colors.primaryVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userProfile?.name?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.h3,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }

            // Nome e email
            userProfile?.let { profile ->
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.total_points),
                    value = userProfile?.totalPoints?.toString() ?: "0"
                )
                StatItem(
                    label = stringResource(R.string.total_trips),
                    value = userProfile?.totalTrips?.toString() ?: "0"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ações
            Button(
                onClick = {
                    AndroidIntents.shareText(
                        context,
                        "Tenho ${userProfile?.totalPoints ?: 0} pontos em Mobilidade Suave!"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Partilhar Resultados")
            }

            Button(
                onClick = { viewModel.syncPendingTrips() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sincronizar Dados")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Card(
        modifier = Modifier.width(140.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}
