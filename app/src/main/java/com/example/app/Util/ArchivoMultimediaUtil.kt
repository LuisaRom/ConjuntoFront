package com.example.app.Util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
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

fun prepararImagenPublicacion(
    context: Context,
    rutaLocal: String?,
    uriString: String?,
    maxBytes: Long = 900_000L
): File? {
    // Prioriza archivo local (cámara/galería copiada) y si no, usa el URI.
    val desdeRuta = rutaLocal?.let { comprimirImagenDesdeRuta(context, it, maxBytes) }
    if (desdeRuta != null) return desdeRuta
    return uriString?.let { copiarImagenComoArchivoTemporal(context, it)?.let { archivo ->
        comprimirImagenDesdeRuta(context, archivo.absolutePath, maxBytes) ?: archivo
    } }
}

private fun comprimirImagenDesdeRuta(context: Context, ruta: String, maxBytes: Long): File? {
    return try {
        val origen = File(ruta)
        if (!origen.exists() || !origen.canRead()) return null

        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(origen.absolutePath, opciones)
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return origen

        val maxDimension = 1600
        var sampleSize = 1
        while ((opciones.outWidth / sampleSize) > maxDimension || (opciones.outHeight / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeFile(origen.absolutePath, decodeOpts) ?: return origen
        comprimirBitmapEnArchivo(context, bitmap, maxBytes)
    } catch (_: Exception) {
        null
    }
}

private fun comprimirBitmapEnArchivo(context: Context, bitmap: Bitmap, maxBytes: Long): File? {
    return try {
        var calidad = 85
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, stream)

        while (stream.size() > maxBytes && calidad > 35) {
            stream.reset()
            calidad -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, stream)
        }

        val destino = File.createTempFile("publicacion_comp_", ".jpg", context.cacheDir)
        destino.outputStream().use { it.write(stream.toByteArray()) }
        destino
    } catch (_: Exception) {
        null
    }
}

fun archivoMultimediaValido(path: String?): File? {
    if (path.isNullOrBlank()) return null
    val file = File(path)
    return file.takeIf { it.exists() && it.canRead() && it.length() > 0L }
}
