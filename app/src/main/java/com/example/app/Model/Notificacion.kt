package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class EnviarReciboRequest(
    @SerializedName("destinatarioId")
    val destinatarioId: Long,
    @SerializedName(value = "tipoRecibo", alternate = ["tipo", "recibo", "empresa"])
    val tipoRecibo: String,
    @SerializedName("mensaje")
    val mensaje: String? = null
)

data class Notificacion(
    val id: Long? = null,

    @SerializedName("mensaje")
    val mensaje: String? = null,

    @SerializedName("fechaEnvio")
    val fechaEnvio: String? = null,  // Formato ISO 8601

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    @SerializedName(value = "destinatarioId", alternate = ["usuarioDestinoId", "idDestinatario", "residenteId"])
    val destinatarioId: Long? = null,

    @SerializedName(value = "destinatario", alternate = ["usuarioDestino", "residente"])
    val destinatario: Usuario? = null,

    @SerializedName(value = "tipoRecibo", alternate = ["tipo", "empresaRecibo"])
    val tipoRecibo: String? = null,

    @SerializedName(value = "tipoNotificacion", alternate = ["tipo", "categoria"])
    val tipoNotificacion: String? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,  // Ruta de la imagen adjunta

    @SerializedName("videoUrl")
    val videoUrl: String? = null,  // Ruta del video adjunto

    @SerializedName("usuariosEtiquetados")
    val usuariosEtiquetados: String? = null  // JSON array de IDs de usuarios etiquetados
) {
    // Función helper para obtener mensaje seguro (renombrada para evitar conflicto con getter automático)
    fun mensajeSeguro(): String {
        val original = mensaje ?: ""
        return original.replace("Pago PSE", "Pago en línea", ignoreCase = true)
    }
    
    // Función helper para verificar si la notificación es válida
    fun esValida(): Boolean = (id != null) || (mensaje?.isNotBlank() == true)
    
    // Función helper para obtener lista de IDs de usuarios etiquetados
    fun obtenerUsuariosEtiquetados(): List<Long> {
        return try {
            usuariosEtiquetados?.let {
                com.google.gson.Gson().fromJson(it, Array<Long>::class.java).toList()
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun esRecibo(): Boolean = esNotificacionRecibo(mensaje) || tipoNotificacion.equals("RECIBO", ignoreCase = true)

    fun esParaResidente(residenteId: Long): Boolean {
        if (destinatarioId == residenteId) return true
        if (destinatario?.id == residenteId) return true
        if (usuario?.id == residenteId) return true
        if (residenteId in obtenerUsuariosEtiquetados()) return true
        return false
    }
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
            n.destinatario?.id != null -> n.destinatario?.id == residenteId
            n.usuario?.id != null -> n.usuario?.id == residenteId
            residenteId in n.obtenerUsuariosEtiquetados() -> true
            else -> true
        }
    }
}