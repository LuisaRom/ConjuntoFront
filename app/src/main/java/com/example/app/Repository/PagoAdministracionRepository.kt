package com.example.app.Repository

import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Model.CheckoutAdministracionResponse
import com.example.app.Model.ConfirmarPagoRequest
import com.example.app.Model.ConfirmarPagoResponse
import com.example.app.Model.CrearPagoRequest
import com.example.app.Model.EstadoPagoAdmin
import com.example.app.Model.PagoAdministracion
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import javax.inject.Inject

class PagoAdministracionRepository @Inject constructor(
    private val api: PagoAdministracionApiService
) {

    private val gson = Gson()

    suspend fun obtenerTodos(): List<PagoAdministracion> {
        return runCatching { api.obtenerMisPagos() }
            .recoverCatching { api.obtenerPagosResidente() }
            .recoverCatching { api.obtenerPagosResidenteAlias() }
            .recoverCatching { api.obtenerPagosResidentePost() }
            .getOrThrow()
    }

    suspend fun obtenerPorId(id: Long): PagoAdministracion {
        return api.obtenerPago(id)
    }

    suspend fun crearCheckoutAdministracion(request: CrearPagoRequest): CheckoutAdministracionResponse {
        return try {
            api.crearCheckoutAdministracion(request)
        } catch (error: HttpException) {
            if (error.code() == 404) {
                runCatching { api.crearPagoEnLinea(request) }
                    .recoverCatching { api.crearPagoEnLineaCheckout(request) }
                    .recoverCatching { api.crearPagoEnLineaMercadoPago(request) }
                    .getOrElse { throw IllegalStateException(extraerMensajeHttp(error), error) }
            } else {
                throw IllegalStateException(extraerMensajeHttp(error), error)
            }
        }
    }

    suspend fun extraerUrlCheckout(response: CheckoutAdministracionResponse): String {
        val url = response.sandboxInitPoint
            ?: response.initPoint
            ?: response.checkoutUrl
            ?: response.redirectUrl
            ?: response.preferenceId?.let { construirUrlDesdePreferencia(it) }
            ?: throw IllegalStateException("El backend no devolvió URL de checkout para redirección")
        return normalizarCheckoutUrl(url)
    }

    suspend fun confirmarPago(referenciaExterna: String): ConfirmarPagoResponse {
        return try {
            api.confirmarPago(ConfirmarPagoRequest(referenciaExterna = referenciaExterna.trim()))
        } catch (error: HttpException) {
            throw IllegalStateException(extraerMensajeHttp(error), error)
        }
    }

    suspend fun obtenerEstadoPagosAdmin(): List<EstadoPagoAdmin> {
        return api.obtenerEstadoPagosAdmin()
    }

    suspend fun eliminar(id: Long) {
        api.eliminarPago(id)
    }

    private fun extraerMensajeHttp(error: HttpException): String {
        val cuerpo = error.response()?.errorBody()?.string().orEmpty().trim()
        if (cuerpo.isBlank()) {
            return "Error del servidor (${error.code()})"
        }
        return runCatching {
            val json = gson.fromJson(cuerpo, JsonObject::class.java)
            json.get("mensaje")?.asString
                ?: json.get("error")?.asString
                ?: json.get("message")?.asString
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: cuerpo.removeSurrounding("\"")
    }

    private fun normalizarCheckoutUrl(url: String): String {
        val valor = url.trim()
        return if (valor.startsWith("http://") || valor.startsWith("https://")) valor else "https://$valor"
    }

    private fun construirUrlDesdePreferencia(preferenceId: String): String {
        val pref = preferenceId.trim()
        if (pref.isBlank()) throw IllegalStateException("PreferenceId vacío")
        return "https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=$pref"
    }
}
