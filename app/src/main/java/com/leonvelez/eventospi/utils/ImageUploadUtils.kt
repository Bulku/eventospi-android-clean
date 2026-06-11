package com.leonvelez.eventospi.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.http.Part

fun getFileNameFromUri(
    context: android.content.Context,
    uri: Uri
): String {
    var result = "imagen.jpg"

    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            result = it.getString(nameIndex)
        }
    }

    return result
}

fun createImagePartFromUri(
    context: android.content.Context,
    uri: Uri
): MultipartBody.Part {
    val fileName = getFileNameFromUri(context, uri)
    val mimeType = context.contentResolver.getType(uri) ?: "image/*"

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("No se pudo abrir la imagen seleccionada")

    val tempFile = File.createTempFile("upload_", fileName, context.cacheDir)
    tempFile.outputStream().use { output ->
        inputStream.copyTo(output)
    }

    val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        "FormFile",
        fileName,
        requestFile
    )
}

fun createProfileImagePartFromUri(
    context: android.content.Context,
    uri: Uri
): MultipartBody.Part {
    val fileName = getFileNameFromUri(context, uri)
    val mimeType = context.contentResolver.getType(uri) ?: "image/*"

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("No se pudo abrir la imagen seleccionada")

    val tempFile = File.createTempFile("profile_", fileName, context.cacheDir)
    tempFile.outputStream().use { output ->
        inputStream.copyTo(output)
    }

    val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        "file",
        fileName,
        requestFile
    )
}
