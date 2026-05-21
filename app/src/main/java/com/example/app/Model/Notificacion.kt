package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class EnviarReciboRequest(
    @SerializedName("destinatarioId")
    val destinatarioId: Long,
    @SerializedName("tipoRecibo")
    val tipoRecibo: String,
    @SerializedName("mensaje")
    val mensaje: String? = null
)

data class Notificacion(
    val id: Long? = null,

    @SerializedName("mensaje")
    val mensaje: String? = null,

    @SerializedName("fechaEnvio")
    val fechaEnvio: String? = null,

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    /** Solo lectura: el backend puede devolverlo en notificaciones de recibo. */
    @SerializedName("destinatarioId")
    val destinatarioId: Long? = null,

    @SerializedName("tipoRecibo")
    val tipoRecibo: String? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,

    @SerializedName("videoUrl")
    val videoUrl: String? = null,

    @SerializedName("usuariosEtiquetados")
    val usuariosEtiquetados: String? = null
) {
    fun mensajeSeguro(): String {
        val original = mensaje ?: ""
        return original.replace("Pago PSE", "Pago en línea", ignoreCase = true)
    }

    fun esValida(): Boolean = (id != null) || (mensaje?.isNotBlank() == true)

    fun obtenerUsuariosEtiquetados(): List<Long> {
        return try {
            usuariosEtiquetados?.let {
                com.google.gson.Gson().fromJson(it, Array<Long>::class.java).toList()
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun esRecibo(): Boolean = esNotificacionRecibo(mensaje) || !tipoRecibo.isNullOrBlank()
}

fun mensajeRecibo(tipoRecibo: String): String =
    "Tienes un recibo de $tipoRecibo disponible en la portería. Por favor pasa a recogerlo."

fun esNotificacionRecibo(mensaje: String?): Boolean {
    if (mensaje.isNullOrBlank()) return false
    val texto = mensaje.lowercase()
    val tieneRecibo = texto.contains("recibo")
    val tieneTipo = texto.contains("enel") || texto.contains("vanti") || texto.contains("epz")
    return tieneRecibo && tieneTipo
}

fun filtrarRecibosResidente(notificaciones: List<Notificacion>, residenteId: Long?): List<Notificacion> {
    if (residenteId == null) return emptyList()
    return notificaciones.filter { n ->
        if (!n.esRecibo()) return@filter false
        when {
            n.destinatarioId != null -> n.destinatarioId == residenteId
            n.usuario?.id != null -> n.usuario?.id == residenteId
            residenteId in n.obtenerUsuariosEtiquetados() -> true
            else -> true
        }
    }
}

/** Payload seguro para POST/PUT: solo campos que el backend documenta en OpenAPI. */
fun Notificacion.paraApi(): Notificacion = Notificacion(
    id = id,
    mensaje = mensaje?.trim(),
    fechaEnvio = fechaEnvio,
    usuario = usuario?.id?.let { Usuario(id = it) },
    imagenUrl = imagenUrl,
    videoUrl = videoUrl,
    usuariosEtiquetados = usuariosEtiquetados
)
