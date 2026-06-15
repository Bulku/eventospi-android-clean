package com.leonvelez.eventospi.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.components.AuthScreenContainer
import kotlinx.coroutines.launch
import com.leonvelez.eventospi.data.model.UserLoginRequest

@Composable
fun LoginScreen(
    onGoToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    initialEmail: String,
    initialMessage: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf(initialMessage) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthScreenContainer(
        title = "Iniciar sesión",
        subtitle = "Accede para crear, gestionar e inscribirte en eventos"
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Correo"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Contraseña"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = "Mostrar u ocultar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    resultText = "Completa correo y contraseña"
                    return@Button
                }

                scope.launch {
                    try {
                        isLoading = true
                        resultText = "Iniciando sesión..."

                        val response = RetrofitInstance.api.login(
                            UserLoginRequest(
                                email = email,
                                password = password
                            )
                        )

                        if (response.isSuccessful) {
                            val loginResponse = response.body()

                            if (loginResponse?.token.isNullOrBlank()) {
                                resultText = "No se recibió token de autenticación"
                            } else {
                                tokenManager.saveToken(loginResponse!!.token)
                                resultText = "Inicio de sesión exitoso"
                                onLoginSuccess()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string().orEmpty()

                            resultText = when {
                                response.code() == 401 -> "Correo o contraseña incorrectos"
                                errorBody.contains("invalid", ignoreCase = true) -> "Correo o contraseña incorrectos"
                                errorBody.contains("unauthorized", ignoreCase = true) -> "Correo o contraseña incorrectos"
                                else -> "No se pudo iniciar sesión"
                            }
                        }
                    } catch (e: Exception) {
                        resultText = "No se pudo iniciar sesión. Verifica tu conexión."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Entrar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onGoToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resultText.isNotBlank()) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (
                    resultText.contains("exitoso", ignoreCase = true)
                ) Color(0xFF2E7D32) else Color.DarkGray
            )
        }
    }
}
