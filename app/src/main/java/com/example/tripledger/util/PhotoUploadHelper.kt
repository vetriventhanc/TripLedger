package com.example.tripledger.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun uriToMultipart(
    context: Context,
    uri: Uri
): MultipartBody.Part? {

    val contentResolver = context.contentResolver

    val mimeType =
        contentResolver.getType(uri)
            ?: return null

    val extension = when (mimeType) {
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        else -> return null
    }

    val tempFile = File.createTempFile(
        "trip_photo_",
        extension,
        context.cacheDir
    )

    contentResolver.openInputStream(uri)?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: return null

    val requestBody = tempFile.asRequestBody(
        mimeType.toMediaTypeOrNull()
    )

    return MultipartBody.Part.createFormData(
        name = "photo",
        filename = tempFile.name,
        body = requestBody
    )
}