package pt.ipp.estg.cmu.ui.Content

import com.google.firebase.firestore.firestore
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
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun RegisterPage(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados para os campos de input
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Crie a sua conta",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de texto para o Nome
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para confirmar a Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão de Registo
        Button(
            onClick = {
                // Validações antes de chamar o Firebase
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, "As passwords não coincidem.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        // 1. Criar o utilizador com email e password
                        val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()

                        // 2. Atualizar o perfil do utilizador para adicionar o nome
                        val user = authResult.user
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }
                        user?.updateProfile(profileUpdates)?.await()

                        // 3. Criar um documento para o utilizador na Firestore (CÓDIGO NOVO)
                        if (user != null) {
                            // Acede à instância da Firestore através do objeto Firebase
                            val db = Firebase.firestore
                            // O resto do código mantém-se igual
                            val userDocument = mapOf(
                                "name" to name,
                                "email" to email,
                                "points" to 0
                            )
                            db.collection("users").document(user.uid).set(userDocument).await()
                        }

                        isLoading = false
                        onRegisterSuccess() // Navega para a app principal

                    } catch (e: Exception) {
                        isLoading = false
                        Toast.makeText(context, "Falha no registo: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para voltar para o ecrã de Login
        TextButton(onClick = onBackToLogin) {
            Text("Já tem uma conta? Faça login")
        }
    }
}