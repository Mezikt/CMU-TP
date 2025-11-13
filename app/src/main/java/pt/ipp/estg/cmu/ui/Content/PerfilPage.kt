package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmu.viewmodel.ProfileViewModel // Certifique-se que o import está correto

@Composable
fun PerfilPage(
    onLogout: () -> Unit,
    // Obtém uma instância do nosso ViewModel. O Compose trata de a manter viva.
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Observa o Flow de dados que vem do ViewModel.
    // `collectAsState` converte o Flow num State que o Compose consegue observar.
    // O valor inicial é 'null', o que nos permite mostrar um indicador de progresso.
    val userProfile by profileViewModel.userProfile.collectAsState(initial = null)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Se userProfile ainda for nulo, significa que os dados ainda estão a ser carregados.
        if (userProfile == null) {
            CircularProgressIndicator()
        } else {
            // Assim que os dados chegam, mostra a informação do perfil.
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userProfile!!.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userProfile!!.email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Mostra a pontuação lida da base de dados local (que é atualizada pela Firestore)
                Text(
                    text = "Pontos: ${userProfile!!.points}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }
        }
    }
}