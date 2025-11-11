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
    // CoroutineScope para lançar a chamada de autenticação
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Variáveis de estado para guardar o email e a password inseridos pelo utilizador
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Para mostrar um indicador de progresso

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Adiciona espaçamento à volta
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Bem-vindo!",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de texto para o Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para a Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), // Esconde a password
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão de Login
        Button(
            onClick = {
                // Validação simples para não fazer chamadas à API desnecessariamente
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    // Lançar a coroutine para fazer o login
                    scope.launch {
                        try {
                            // Usar signIn, não createUser. E usar as variáveis!
                            Firebase.auth.signInWithEmailAndPassword(email, password).await()

                            // Se a linha de cima não der erro, o login foi bem-sucedido
                            isLoading = false
                            onLoginSuccess() // Navega para o ecrã principal

                        } catch (e: Exception) {
                            // Se houver um erro (ex: password errada), mostra uma mensagem
                            isLoading = false
                            Toast.makeText(context, "Falha no login: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Desativa o botão enquanto está a carregar
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

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para navegar para o ecrã de Registo
        TextButton(onClick = onNavigateToRegister) {
            Text("Não tem conta? Registe-se")
        }
    }
}