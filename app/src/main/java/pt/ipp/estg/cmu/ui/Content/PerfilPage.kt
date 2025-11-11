package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun PerfilPage(
    onLogout: () -> Unit // Parâmetro para lidar com o evento de logout
) {
    val user = Firebase.auth.currentUser

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TODO: Adicionar a vossa UI de imagem de perfil aqui

        Spacer(modifier = Modifier.height(16.dp))

        // Mostra os dados do utilizador que fez login
        Text(
            text = user?.displayName ?: "Utilizador",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user?.email ?: "email@desconhecido.com",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onLogout) { // O botão agora chama a função recebida
            Text("Logout")
        }
    }
}