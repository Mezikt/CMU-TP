package pt.ipp.estg.cmu.ui.Content

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // --- MELHORIA DE DESIGN ---
    // Usar um Box para garantir que o conteúdo fica perfeitamente centrado
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp), // Aumentar o padding para dar mais margem
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Adiciona um espaçamento uniforme de 16.dp entre cada elemento na Column
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Título principal da aplicação
            Text(
                text = "Mobilidade Suave",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary // Usar a cor primária do tema para destaque
            )

            // Subtítulo
            Text(
                text = "Bem-vindo de volta!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Cor mais suave para o subtítulo
            )

            // Espaço maior após os títulos para separar do formulário
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para o Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Campo de texto para a Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Botão de Login
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        scope.launch {
                            try {
                                Firebase.auth.signInWithEmailAndPassword(email, password).await()
                                isLoading = false
                                onLoginSuccess()
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(context, "Falha no login: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp), // Altura standard para melhor toque
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium // Cantos ligeiramente arredondados
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }

            // Botão para navegar para o ecrã de Registo
            TextButton(onClick = onNavigateToRegister) {
                Text("Não tem conta? Registe-se")
            }
        }
    }
}