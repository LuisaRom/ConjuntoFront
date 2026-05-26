package com.example.app.Interfaces

import com.example.app.Model.Notificacion
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificacionApiService {
    @GET("/api/notificaciones")
    suspend fun obtenerNotificaciones(): List<Notificacion>

    @GET("/api/notificaciones/novedades")
    suspend fun obtenerNovedades(
        @Query("search") search: String? = null
    ): List<Notificacion>

    @GET("/api/notificaciones/{id}")
    suspend fun obtenerNotificacion(@Path("id") id: Long): Notificacion

    @POST("/api/notificaciones")
    suspend fun guardarNotificacion(@Body notificacion: Notificacion): Notificacion

    @Multipart
    @POST("/api/notificaciones")
    suspend fun guardarNotificacionMultipart(
        @Query("mensaje") mensaje: String,
        @Query("fechaEnvio") fechaEnvio: String? = null,
        @Query("usuariosEtiquetados") usuariosEtiquetados: String? = null,
        @Part imagen: MultipartBody.Part? = null,
        @Part foto: MultipartBody.Part? = null,
        @Part video: MultipartBody.Part? = null
    ): Notificacion

    @Multipart
    @POST("/api/notificaciones")
    suspend fun guardarNotificacionMultipartForm(
        @Part("mensaje") mensaje: RequestBody,
        @Part("fechaEnvio") fechaEnvio: RequestBody? = null,
        @Part("usuariosEtiquetados") usuariosEtiquetados: RequestBody? = null,
        @Part imagen: MultipartBody.Part? = null,
        @Part foto: MultipartBody.Part? = null,
        @Part video: MultipartBody.Part? = null
    ): Notificacion

    @POST("/api/notificaciones/recibos/enviar")
    suspend fun enviarRecibo(@Body request: com.example.app.Model.EnviarReciboRequest): Notificacion

    @GET("/api/notificaciones/chat/historial")
    suspend fun obtenerHistorialChat(): List<Notificacion>

    @POST("/api/notificaciones/chat/enviar")
    suspend fun enviarMensajeChat(@Body request: com.example.app.Model.ChatMensajeRequest): Notificacion

    @PUT("/api/notificaciones/{id}")
    suspend fun actualizarNotificacion(@Path("id") id: Long, @Body notificacion: Notificacion): Notificacion

    @DELETE("/api/notificaciones/{id}")
    suspend fun eliminarNotificacion(@Path("id") id: Long)
}
