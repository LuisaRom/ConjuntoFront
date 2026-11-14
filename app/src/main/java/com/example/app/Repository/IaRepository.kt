package com.example.app.Repository

import com.example.app.Interfaces.IaApiService
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
import com.example.app.Model.RespuestaMalCalificada
import com.example.app.Model.ResumenGestionIA
import javax.inject.Inject

class IaRepository @Inject constructor(
    private val api: IaApiService
) {
    suspend fun estado(): EstadoIA = api.estado()

    suspend fun enviarMensaje(mensaje: String, conversacionId: Long?): ChatIAResponse =
        api.chat(ChatIARequest(mensaje = mensaje, conversacionId = conversacionId))

    suspend fun conversaciones(): List<ConversacionIA> = api.conversaciones()

    suspend fun mensajes(conversacionId: Long): List<MensajeIA> = api.mensajes(conversacionId)

    suspend fun eliminarConversacion(conversacionId: Long) {
        api.eliminarConversacion(conversacionId)
    }

    suspend fun calificar(mensajeId: Long, util: Boolean): Boolean {
        val respuesta = api.feedback(mensajeId, FeedbackIARequest(util))
        return respuesta["propuestoComoConocimiento"] == true
    }

    suspend fun conocimiento(estado: String? = null): List<ConocimientoIA> = api.conocimiento(estado)

    suspend fun crearConocimiento(titulo: String, contenido: String, categoria: String): ConocimientoIA =
        api.crearConocimiento(ConocimientoIARequest(titulo, contenido, categoria))

    suspend fun aprobarConocimiento(id: Long): ConocimientoIA = api.aprobarConocimiento(id)

    suspend fun rechazarConocimiento(id: Long): ConocimientoIA = api.rechazarConocimiento(id)

    suspend fun eliminarConocimiento(id: Long) {
        api.eliminarConocimiento(id)
    }

    suspend fun respuestasMalCalificadas(): List<RespuestaMalCalificada> = api.respuestasMalCalificadas()

    suspend fun resumenGestion(): ResumenGestionIA = api.resumenGestion()

    suspend fun redactarComunicado(tema: String, tono: String?, detalles: String?): ComunicadoIAResponse =
        api.redactarComunicado(ComunicadoIARequest(tema, tono, detalles))
}
