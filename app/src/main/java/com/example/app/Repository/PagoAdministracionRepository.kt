package com.example.app.Repository

import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Model.CheckoutAdministracionResponse
import com.example.app.Model.ConfirmarPagoRequest
import com.example.app.Model.ConfirmarPagoResponse
import com.example.app.Model.CrearPagoRequest
import com.example.app.Model.EstadoPagoAdmin
import com.example.app.Model.PagoAdministracion
import retrofit2.HttpException
import javax.inject.Inject

class PagoAdministracionRepository @Inject constructor(
    private val api: PagoAdministracionApiService
) {

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
        val response = runCatching { api.crearCheckoutAdministracion(request) }
            .recoverCatching { api.crearPagoEnLinea(request) }
            .recoverCatching { api.crearPagoEnLineaCheckout(request) }
            .recoverCatching { api.crearPagoEnLineaMercadoPago(request) }
            .getOrElse { error ->
                if (error is HttpException && error.code() == 404) {
                    throw IllegalStateException(
                        "El backend no tiene habilitada la ruta de checkout de Mercado Pago (404)."
                    )
                }
                throw error
            }
        return response
    }

    suspend fun extraerUrlCheckout(response: CheckoutAdministracionResponse): String {
        val url = response.initPoint
            ?: response.sandboxInitPoint
            ?: response.checkoutUrl
            ?: response.redirectUrl
            ?: response.preferenceId?.let { construirUrlDesdePreferencia(it) }
            ?: throw IllegalStateException("El backend no devolvió URL de checkout para redirección")
        return normalizarCheckoutUrl(url)
    }

    suspend fun confirmarPago(referenciaExterna: String): ConfirmarPagoResponse {
        return api.confirmarPago(ConfirmarPagoRequest(referenciaExterna = referenciaExterna.trim()))
    }

    suspend fun obtenerEstadoPagosAdmin(): List<EstadoPagoAdmin> {
        return api.obtenerEstadoPagosAdmin()
    }

    suspend fun eliminar(id: Long) {
        api.eliminarPago(id)
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
