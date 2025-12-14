package pt.ipp.estg.cmu.ui.Content

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun RegisterPage(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val auth = Firebase.auth

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = stringResource(R.string.title_create_account),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.subtitle_quick_easy),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.label_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.label_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.label_confirm_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_fill_all_fields),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_passwords_mismatch),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                            val user = authResult.user
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }
                            user?.updateProfile(profileUpdates)?.await()
                            if (user != null) {
                                val db = Firebase.firestore
                                val userDocument = mapOf(
                                    "name" to name,
                                    "email" to email,
                                    "points" to 0
                                )
                                db.collection("users").document(user.uid).set(userDocument).await()
                            }
                            isLoading = false
                            onRegisterSuccess()
                        } catch (e: Exception) {
                            isLoading = false
                            val errorMsg = context.getString(R.string.error_register_failed) + "${e.message}"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.btn_register))
                }
            }

            TextButton(onClick = onBackToLogin) {
                Text(stringResource(R.string.btn_login_prompt))
            }
        }
    }
}