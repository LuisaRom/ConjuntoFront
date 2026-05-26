package com.example.app.Util

private const val BASE_URL = "https://conjuntoback.onrender.com"

fun resolverUrlMedio(path: String?): String? {
    if (path.isNullOrBlank()) return null
    val limpio = path
        .trim()
        .removePrefix("\"")
        .removeSuffix("\"")
        .replace("\\", "/")

    // Soporta QR/imagenes embebidas como data URL.
    if (limpio.startsWith("data:image", ignoreCase = true)) {
        return limpio
    }

    if (limpio.startsWith("http://") || limpio.startsWith("https://") || limpio.startsWith("content://")) {
        return limpio
    }
    if (limpio.startsWith("/storage/") || limpio.startsWith("file://") || limpio.startsWith("/data/")) {
        return null
    }
    return "$BASE_URL/${limpio.removePrefix("/")}"
}

fun resolverUrlImagenMascota(path: String?): String? = resolverUrlMedio(path)

fun resolverUrlImagenNotificacion(path: String?): String? = resolverUrlMedio(path)

fun resolverUrlVideoNotificacion(path: String?): String? = resolverUrlMedio(path)
