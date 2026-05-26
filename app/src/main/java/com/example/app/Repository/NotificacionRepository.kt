package com.example.app.Repository

import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Model.ChatMensajeRequest
import com.example.app.Model.EnviarReciboRequest
import com.example.app.Model.Notificacion
import com.example.app.Model.Usuario
import com.example.app.Model.mensajeRecibo
import com.example.app.Model.paraApi
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NotificacionRepository @Inject constructor(
    private val api: NotificacionApiService
) {

    /** Feed de novedades (muro); alineado con GET /api/notificaciones/novedades del backend. */
    suspend fun obtenerNovedades(search: String? = null): List<Notificacion> {
        return try {
            api.obtenerNovedades(search?.takeIf { it.isNotBlank() })
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para obtener novedades")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al obtener novedades")
        } catch (e: HttpException) {
            when (e.code()) {
                500 -> throw Exception(
                    extraerMensajeHttp(e).ifBlank {
                        "Error del servidor al obtener novedades. Verifica el esquema de la tabla notificacion."
                    }
                )
                else -> throw Exception(extraerMensajeHttp(e))
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener novedades: ${e.message}")
        }
    }

    /** Listado general (incluye recibos); para el muro usar [obtenerNovedades]. */
    suspend fun obtenerTodos(): List<Notificacion> {
        return try {
            api.obtenerNotificaciones()
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para obtener notificaciones")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al obtener notificaciones")
        } catch (e: HttpException) {
            throw Exception(extraerMensajeHttp(e))
        } catch (e: Exception) {
            throw Exception("Error al obtener notificaciones: ${e.message}")
        }
    }

    suspend fun obtenerPorId(id: Long): Notificacion {
        return try {
            api.obtenerNotificacion(id)
        } catch (e: Exception) {
            throw Exception("Error al obtener notificación: ${e.message}")
        }
    }

    suspend fun obtenerHistorialChat(): List<Notificacion> {
        return try {
            api.obtenerHistorialChat()
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para obtener mensajes")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al obtener mensajes")
        } catch (e: retrofit2.HttpException) {
            throw Exception("Error HTTP ${e.code()} al obtener mensajes: ${e.message()}")
        } catch (e: Exception) {
            throw Exception("Error al obtener mensajes: ${e.message}")
        }
    }

    suspend fun enviarMensajeChat(destinatarioId: Long, mensaje: String): Notificacion {
        return try {
            api.enviarMensajeChat(ChatMensajeRequest(destinatarioId = destinatarioId, mensaje = mensaje))
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para enviar el mensaje")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al enviar el mensaje")
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string().orEmpty()
            throw Exception(errorBody.ifBlank { "Error HTTP ${e.code()} al enviar mensaje" })
        } catch (e: Exception) {
            throw Exception("Error al enviar mensaje: ${e.message}")
        }
    }

    suspend fun enviarRecibo(
        destinatarioId: Long,
        tipoRecibo: String,
        mensaje: String = mensajeRecibo(tipoRecibo)
    ): Notificacion {
        val request = EnviarReciboRequest(
            destinatarioId = destinatarioId,
            tipoRecibo = tipoRecibo.uppercase(),
            mensaje = mensaje
        )
        return try {
            api.enviarRecibo(request)
        } catch (error: HttpException) {
            if (error.code() in listOf(400, 404, 405)) {
                guardar(
                    Notificacion(
                        mensaje = mensaje,
                        fechaEnvio = fechaActualIso(),
                        usuario = Usuario(id = destinatarioId)
                    )
                )
            } else {
                throw Exception(extraerMensajeHttp(error))
            }
        } catch (error: Exception) {
            throw Exception("Error al enviar recibo: ${error.message}")
        }
    }

    suspend fun guardar(notificacion: Notificacion): Notificacion {
        val payloadPrincipal = notificacion.toPayloadGuardar()
        return try {
            api.guardarNotificacion(payloadPrincipal)
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para guardar la publicación")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al guardar la publicación")
        } catch (e: HttpException) {
            when (e.code()) {
                400 -> {
                    val payloadJwt = payloadPrincipal.copy(
                        imagenUrl = null,
                        videoUrl = null,
                        usuariosEtiquetados = null,
                        destinatarioId = null,
                        tipoRecibo = null,
                        usuario = null
                    )
                    runCatching { api.guardarNotificacion(payloadJwt) }.getOrElse {
                        throw Exception(extraerMensajeHttp(e))
                    }
                }
                else -> throw Exception(extraerMensajeHttp(e))
            }
        } catch (e: Exception) {
            throw Exception("Error al guardar publicación: ${e.message}")
        }
    }

    suspend fun guardarConMultimedia(
        notificacion: Notificacion,
        imagenFile: File?,
        videoFile: File?
    ): Notificacion {
        val mensaje = notificacion.mensaje?.trim().orEmpty()
        if (mensaje.isBlank()) {
            throw Exception("La descripción es obligatoria")
        }

        if (imagenFile == null && videoFile == null) {
            return guardar(notificacion)
        }

        fun crearPartes(): Triple<MultipartBody.Part?, MultipartBody.Part?, MultipartBody.Part?> {
            val imagenPart = imagenFile?.let { file ->
                val mime = when (file.extension.lowercase(Locale.getDefault())) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                MultipartBody.Part.createFormData(
                    "imagen",
                    file.name,
                    file.asRequestBody(mime.toMediaTypeOrNull())
                )
            }
            val fotoPart = imagenFile?.let { file ->
                val mime = when (file.extension.lowercase(Locale.getDefault())) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                MultipartBody.Part.createFormData(
                    "foto",
                    file.name,
                    file.asRequestBody(mime.toMediaTypeOrNull())
                )
            }
            val videoPart = videoFile?.let { file ->
                val mime = when (file.extension.lowercase(Locale.getDefault())) {
                    "mov" -> "video/quicktime"
                    "webm" -> "video/webm"
                    else -> "video/mp4"
                }
                MultipartBody.Part.createFormData(
                    "video",
                    file.name,
                    file.asRequestBody(mime.toMediaTypeOrNull())
                )
            }
            return Triple(imagenPart, fotoPart, videoPart)
        }

        fun textoPart(valor: String?): RequestBody? {
            return valor
                ?.takeIf { it.isNotBlank() }
                ?.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        val mensajeBody = mensaje.toRequestBody("text/plain".toMediaTypeOrNull())

        val intentos: List<suspend () -> Notificacion> = listOf(
            {
                val (imagenPart, _, videoPart) = crearPartes()
                api.guardarNotificacionMultipartForm(
                    mensaje = mensajeBody,
                    fechaEnvio = textoPart(notificacion.fechaEnvio),
                    usuariosEtiquetados = textoPart(notificacion.usuariosEtiquetados),
                    imagen = imagenPart,
                    video = videoPart
                )
            },
            {
                val (_, fotoPart, videoPart) = crearPartes()
                api.guardarNotificacionMultipartForm(
                    mensaje = mensajeBody,
                    fechaEnvio = textoPart(notificacion.fechaEnvio),
                    usuariosEtiquetados = textoPart(notificacion.usuariosEtiquetados),
                    foto = fotoPart,
                    video = videoPart
                )
            },
            {
                val (imagenPart, _, videoPart) = crearPartes()
                api.guardarNotificacionMultipart(
                    mensaje = mensaje,
                    fechaEnvio = notificacion.fechaEnvio,
                    usuariosEtiquetados = notificacion.usuariosEtiquetados,
                    imagen = imagenPart,
                    video = videoPart
                )
            },
            {
                guardar(notificacion)
            }
        )

        var ultimoError: Exception? = null
        for (intento in intentos) {
            try {
                return intento()
            } catch (e: HttpException) {
                ultimoError = Exception(extraerMensajeHttp(e))
                if (e.code() !in listOf(400, 404, 405, 415, 500)) {
                    throw ultimoError
                }
            } catch (e: Exception) {
                ultimoError = Exception("Error al guardar publicación con multimedia: ${e.message}")
            }
        }

        throw ultimoError ?: Exception("No se pudo guardar la publicación con imagen o video")
    }

    suspend fun actualizar(id: Long, notificacion: Notificacion): Notificacion {
        return try {
            api.actualizarNotificacion(id, notificacion)
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para actualizar la publicación")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al actualizar la publicación")
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                400 -> throw Exception("Datos inválidos. Verifica que todos los campos estén correctos.")
                404 -> throw Exception("Publicación no encontrada")
                500 -> throw Exception("Error del servidor al actualizar. Verifica que la base de datos esté configurada correctamente.")
                else -> throw Exception("Error HTTP ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Error al actualizar publicación: ${e.message}")
        }
    }

    suspend fun eliminar(id: Long) {
        try {
            api.eliminarNotificacion(id)
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para eliminar la publicación")
        } catch (e: Exception) {
            throw Exception("Error al eliminar publicación: ${e.message}")
        }
    }

    private fun Notificacion.toPayloadGuardar(): Notificacion = paraApi()

    private fun fechaActualIso(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun extraerMensajeHttp(error: HttpException): String {
        val cuerpo = error.response()?.errorBody()?.string().orEmpty().trim()
        if (cuerpo.isBlank()) {
            return "Error del servidor (${error.code()})"
        }
        return runCatching {
            val json = JsonParser.parseString(cuerpo).asJsonObject
            when {
                json.has("mensaje") && !json.get("mensaje").isJsonNull ->
                    json.get("mensaje").asString
                json.has("error") && !json.get("error").isJsonNull ->
                    json.get("error").asString
                else -> cuerpo
            }
        }.getOrElse { cuerpo }
    }
}
