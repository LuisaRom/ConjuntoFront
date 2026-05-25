package com.example.app.Util

import android.content.Context
import android.net.Uri
import java.io.File

fun copiarImagenComoArchivoTemporal(context: Context, uriString: String): File? {
    return try {
        val uri = Uri.parse(uriString)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val file = File.createTempFile("publicacion_", ".jpg", context.cacheDir)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap != null) {
            file.outputStream().use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 72, outputStream)
            }
        } else {
            file.outputStream().use { outputStream ->
                outputStream.write(bytes)
            }
        }
        file
    } catch (_: Exception) {
        null
    }
}

fun archivoMultimediaValido(path: String?): File? {
    if (path.isNullOrBlank()) return null
    val file = File(path)
    return file.takeIf { it.exists() && it.canRead() && it.length() > 0L }
}
