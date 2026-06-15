package com.leonvelez.eventospi.ui.screens.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.ValidationErrorText
import kotlinx.coroutines.launch
import com.leonvelez.eventospi.data.model.UserChangePasswordRequest

@Composable
fun ChangePasswordScreen(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    FormScreenContainer(
        title = "Cambiar contraseña",
        subtitle = "Actualiza tu acceso de forma segura"
    ) {
        OutlinedTextField(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                currentPasswordError = null
            },
            label = { Text("Contraseña actual") },
            singleLine = true,
            isError = currentPasswordError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Contraseña actual"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { currentPasswordVisible = !currentPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (currentPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = "Mostrar u ocultar contraseña actual"
                    )
                }
            },
            visualTransformation = if (currentPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(currentPasswordError)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                newPasswordError = null
            },
            label = { Text("Nueva contraseña") },
            singleLine = true,
            isError = newPasswordError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Nueva contraseña"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { newPasswordVisible = !newPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (newPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = "Mostrar u ocultar nueva contraseña"
                    )
                }
            },
            visualTransformation = if (newPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(newPasswordError)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null
            },
            label = { Text("Confirmar nueva contraseña") },
            singleLine = true,
            isError = confirmPasswordError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirmar contraseña"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = "Mostrar u ocultar confirmación"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(confirmPasswordError)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                currentPasswordError = if (currentPassword.isBlank()) "Ingresa tu contraseña actual" else null
                newPasswordError = if (newPassword.isBlank()) "Ingresa una nueva contraseña" else null
                confirmPasswordError = when {
                    confirmPassword.isBlank() -> "Confirma la nueva contraseña"
                    confirmPassword != newPassword -> "Las contraseñas no coinciden"
                    else -> null
                }

                if (
                    currentPasswordError != null ||
                    newPasswordError != null ||
                    confirmPasswordError != null
                ) {
                    resultText = "Corrige los campos marcados en rojo"
                    return@Button
                }

                scope.launch {
                    try {
                        isLoading = true
                        resultText = "Cambiando contraseña..."

                        val savedToken = tokenManager.getToken()

                        if (savedToken.isNullOrBlank()) {
                            resultText = "No hay sesión activa"
                            return@launch
                        }

                        val response = RetrofitInstance.api.changePassword(
                            token = "Bearer $savedToken",
                            request = UserChangePasswordRequest(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmNewPassword = confirmPassword
                            )
                        )

                        if (response.isSuccessful) {
                            resultText = "Contraseña actualizada correctamente"
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        } else {
                            val errorText = response.errorBody()?.string().orEmpty()

                            resultText = when {
                                response.code() == 400 -> "La contraseña actual es incorrecta"
                                errorText.contains("incorrect", ignoreCase = true) -> "La contraseña actual es incorrecta"
                                else -> "No se pudo cambiar la contraseña"
                            }
                        }
                    } catch (e: Exception) {
                        resultText = "No se pudo cambiar la contraseña. Verifica tu conexión."
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
                Text("Guardar nueva contraseña")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resultText.isNotBlank()) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (resultText.contains("correctamente", ignoreCase = true)) {
                    Color(0xFF2E7D32)
                } else {
                    Color.DarkGray
                }
            )
        }
    }
}
