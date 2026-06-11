package com.leonvelez.eventospi.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.components.PickerLikeField
import com.leonvelez.eventospi.utils.createProfileImagePartFromUri
import kotlinx.coroutines.launch

@Composable
fun ProfileImageScreen(
    onBackToHome: () -> Unit,
    onImageUploaded: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resultText by remember { mutableStateOf("Selecciona una imagen para tu perfil") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Imagen de perfil",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        PickerLikeField(
            label = "Imagen de perfil",
            value = if (selectedImageUri == null) "" else "Imagen seleccionada",
            placeholder = "Seleccionar imagen",
            onClick = {
                imagePickerLauncher.launch("image/*")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedImageUri == null) {
                    resultText = "Selecciona una imagen primero"
                    return@Button
                }

                scope.launch {
                    try {
                        val savedToken = tokenManager.getToken()

                        if (savedToken.isNullOrBlank()) {
                            resultText = "No hay sesión activa"
                            return@launch
                        }

                        val imagePart = createProfileImagePartFromUri(
                            context = context,
                            uri = selectedImageUri!!
                        )

                        val response = RetrofitInstance.api.uploadProfileImage(
                            token = "Bearer $savedToken",
                            file = imagePart
                        )

                        if (response.isSuccessful) {
                            onImageUploaded(selectedImageUri!!)
                            resultText = "Imagen de perfil cargada correctamente"
                            onBackToHome()
                        } else {
                            val errorText = response.errorBody()?.string().orEmpty()
                            resultText = "Error subiendo imagen: ${response.code()} - $errorText"
                        }
                    } catch (e: Exception) {
                        resultText = "Excepción subiendo imagen: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Subir imagen")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onBackToHome) {
            Text("Volver al mapa")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = resultText)
    }
}
