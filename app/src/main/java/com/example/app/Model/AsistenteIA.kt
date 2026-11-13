package com.example.app.Model

data class ChatIARequest(
    val mensaje: String,
    val conversacionId: Long? = null
)

data class ChatIAResponse(
    val conversacionId: Long? = null,
    val mensajeId: Long? = null,
    val respuesta: String? = null,
    val herramientasUsadas: List<String>? = null,
    val iaDisponible: Boolean? = null
)

data class ConversacionIA(
    val id: Long = 0,
    val titulo: String? = null,
    val fechaActualizacion: String? = null
)

data class MensajeIA(
    val id: Long? = null,
    val rol: String? = null,
    val contenido: String? = null,
    val util: Boolean? = null,
    val fecha: String? = null,
    val herramientasUsadas: String? = null
) {
    fun esUsuario(): Boolean = rol.equals("USUARIO", ignoreCase = true)
}

data class FeedbackIARequest(val util: Boolean)

data class ConocimientoIA(
    val id: Long? = null,
    val titulo: String? = null,
    val contenido: String? = null,
    val categoria: String? = null,
    val estado: String? = null,
    val origen: String? = null,
    val usos: Int? = null,
    val fechaCreacion: String? = null,
    val creadoPorNombre: String? = null
)

data class ConocimientoIARequest(
    val titulo: String,
    val contenido: String,
    val categoria: String
)

data class ResumenGestionIA(
    val metricas: Map<String, Any?>? = null,
    val analisis: String? = null,
    val generadoPorIA: Boolean? = null,
    val fecha: String? = null
)

data class ComunicadoIARequest(
    val tema: String,
    val tono: String? = null,
    val detalles: String? = null
)

data class ComunicadoIAResponse(
    val texto: String? = null,
    val generadoPorIA: Boolean? = null
)

data class RespuestaMalCalificada(
    val mensajeId: Long? = null,
    val pregunta: String? = null,
    val respuesta: String? = null,
    val fecha: String? = null
)

data class EstadoIA(
    val disponible: Boolean? = null,
    val proveedor: String? = null,
    val modelo: String? = null,
    val nombreAsistente: String? = null,
    val respuestasUtiles: Long? = null,
    val respuestasNoUtiles: Long? = null,
    val conocimiento: Map<String, Long>? = null
)
