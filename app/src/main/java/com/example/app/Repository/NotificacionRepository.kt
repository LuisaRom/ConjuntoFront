package com.example.app.Repository

import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Model.Notificacion
import javax.inject.Inject

class NotificacionRepository @Inject constructor(
    private val api: NotificacionApiService
) {

    suspend fun obtenerTodos(): List<Notificacion> {
        return try {
            api.obtenerNotificaciones()
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para obtener notificaciones")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al obtener notificaciones")
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                500 -> throw Exception("Error del servidor al obtener notificaciones. Verifica que la columna 'imagen_url' exista en la base de datos.")
                else -> throw Exception("Error HTTP ${e.code()}: ${e.message()}")
            }
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

    suspend fun guardar(notificacion: Notificacion): Notificacion {
        val payloadPrincipal = notificacion.copy(
            mensaje = notificacion.mensaje?.trim(),
            // El backend obtiene el usuario del token; evita conflictos de deserialización.
            usuario = null
        )
        return try {
            api.guardarNotificacion(payloadPrincipal)
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor para guardar la publicación")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado al guardar la publicación")
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                400, 500 -> {
                    // Fallback: payload mínimo para backends más estrictos.
                    val payloadMinimo = payloadPrincipal.copy(
                        imagenUrl = null,
                        videoUrl = null,
                        usuariosEtiquetados = null
                    )
                    runCatching { api.guardarNotificacion(payloadMinimo) }.getOrElse { secondError ->
                        throw Exception("No se pudo guardar la publicación. Verifica descripción e intenta de nuevo.")
                    }
                }
                else -> throw Exception("Error HTTP ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Error al guardar publicación: ${e.message}")
        }
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
}