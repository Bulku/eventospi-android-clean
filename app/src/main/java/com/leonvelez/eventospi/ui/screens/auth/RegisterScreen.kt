package com.leonvelez.eventospi.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.FormSectionTitle
import com.leonvelez.eventospi.ui.components.ValidationErrorText
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var userNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    FormScreenContainer(
        title = "Crear cuenta",
        subtitle = "Regístrate para publicar, gestionar e inscribirte en eventos"
    ) {
        FormSectionTitle("Datos personales")

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = null
            },
            label = { Text("Nombre") },
            singleLine = true,
            isError = firstNameError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Nombre"
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(firstNameError)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = null
            },
            label = { Text("Apellido") },
            singleLine = true,
            isError = lastNameError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Apellido"
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(lastNameError)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                userNameError = null
            },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            isError = userNameError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(userNameError)

        Spacer(modifier = Modifier.height(14.dp))

        FormSectionTitle("Acceso")

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Correo") },
            singleLine = true,
            isError = emailError != null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Correo"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        ValidationErrorText(emailError)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Contraseña") },
            singleLine = true,
            isError = passwordError != null,
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
        ValidationErrorText(passwordError)

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                firstNameError = if (firstName.isBlank()) "Ingresa tu nombre" else null
                lastNameError = if (lastName.isBlank()) "Ingresa tu apellido" else null
                userNameError = if (userName.isBlank()) "Ingresa un nombre de usuario" else null
                emailError = if (email.isBlank()) "Ingresa tu correo" else null
                passwordError = if (password.isBlank()) "Ingresa una contraseña" else null

                if (
                    firstNameError != null ||
                    lastNameError != null ||
                    userNameError != null ||
                    emailError != null ||
                    passwordError != null
                ) {
                    resultText = "Corrige los campos marcados en rojo"
                    return@Button
                }

                scope.launch {
                    try {
                        isLoading = true
                        resultText = "Creando cuenta..."

                        val response = RetrofitInstance.api.register(
                            firstName = firstName,
                            lastName = lastName,
                            userName = userName,
                            email = email,
                            password = password,
                            confirmPassword = password
                        )

                        if (response.isSuccessful) {
                            resultText = "Cuenta creada correctamente"
                            onRegisterSuccess(email)
                        } else {
                            val errorBody = response.errorBody()?.string().orEmpty()
                            resultText = "Error al registrarse: ${response.code()} - $errorBody"
                        }
                    } catch (e: Exception) {
                        resultText = "Excepción al registrarse: ${e.message}"
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
                Text("Crear cuenta")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ya tengo cuenta")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resultText.isNotBlank()) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (
                    resultText.contains("correctamente", ignoreCase = true)
                ) Color(0xFF2E7D32) else Color.DarkGray
            )
        }
    }
}
