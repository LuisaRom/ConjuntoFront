package com.example.app.Interfaces

import com.example.app.Model.ChatIARequest
import com.example.app.Model.ChatIAResponse
import com.example.app.Model.ComunicadoIARequest
import com.example.app.Model.ComunicadoIAResponse
import com.example.app.Model.ConocimientoIA
import com.example.app.Model.ConocimientoIARequest
import com.example.app.Model.ConversacionIA
import com.example.app.Model.EstadoIA
import com.example.app.Model.FeedbackIARequest
import com.example.app.Model.MensajeIA
import com.example.app.Model.Queja
import com.example.app.Model.RespuestaMalCalificada
import com.example.app.Model.ResumenGestionIA
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IaApiService {

    @GET("/api/ia/estado")
    suspend fun estado(): EstadoIA

    @POST("/api/ia/chat")
    suspend fun chat(@Body request: ChatIARequest): ChatIAResponse

    @GET("/api/ia/conversaciones")
    suspend fun conversaciones(): List<ConversacionIA>

    @GET("/api/ia/conversaciones/{id}/mensajes")
    suspend fun mensajes(@Path("id") conversacionId: Long): List<MensajeIA>

    @DELETE("/api/ia/conversaciones/{id}")
    suspend fun eliminarConversacion(@Path("id") conversacionId: Long): Response<Unit>

    @POST("/api/ia/mensajes/{id}/feedback")
    suspend fun feedback(@Path("id") mensajeId: Long, @Body request: FeedbackIARequest): Map<String, Any?>

    @GET("/api/ia/conocimiento")
    suspend fun conocimiento(@Query("estado") estado: String? = null): List<ConocimientoIA>

    @POST("/api/ia/conocimiento")
    suspend fun crearConocimiento(@Body request: ConocimientoIARequest): ConocimientoIA

    @PUT("/api/ia/conocimiento/{id}/aprobar")
    suspend fun aprobarConocimiento(@Path("id") id: Long): ConocimientoIA

    @PUT("/api/ia/conocimiento/{id}/rechazar")
    suspend fun rechazarConocimiento(@Path("id") id: Long): ConocimientoIA

    @DELETE("/api/ia/conocimiento/{id}")
    suspend fun eliminarConocimiento(@Path("id") id: Long): Response<Unit>

    @GET("/api/ia/feedback/negativos")
    suspend fun respuestasMalCalificadas(): List<RespuestaMalCalificada>

    @GET("/api/ia/gestion/resumen")
    suspend fun resumenGestion(): ResumenGestionIA

    @POST("/api/ia/comunicados/redactar")
    suspend fun redactarComunicado(@Body request: ComunicadoIARequest): ComunicadoIAResponse

    @POST("/api/ia/quejas/{id}/analizar")
    suspend fun analizarQueja(@Path("id") quejaId: Long): Queja
}
