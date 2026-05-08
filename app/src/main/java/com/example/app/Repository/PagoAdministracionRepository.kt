package com.example.app.Repository

import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Model.CrearPagoRequest
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

    suspend fun guardar(pagoAdministracion: PagoAdministracion): PagoAdministracion {
        return api.guardarPago(pagoAdministracion)
    }

    suspend fun crearCheckoutAdministracion(request: CrearPagoRequest): String {
        val response = runCatching { api.crearPagoEnLinea(request) }
            .recoverCatching { api.crearPagoEnLineaCheckout(request) }
            .recoverCatching { api.crearPagoEnLineaMercadoPago(request) }
            .recoverCatching { api.crearPagoEnLineaMercadoPagoCrear(request) }
            .recoverCatching { api.crearPagoEnLineaMercadoPagoDash(request) }
            .recoverCatching { api.crearPagoEnLineaMercadoPagoDashCrear(request) }
            .getOrElse { error ->
                if (error is HttpException && error.code() == 404) {
                    throw IllegalStateException(
                        "El backend no tiene habilitada la ruta de checkout de Mercado Pago (404). " +
                            "Verifica el endpoint de prueba en el backend."
                    )
                }
                throw error
            }

        val url = response.initPoint
            ?: response.sandboxInitPoint
            ?: response.redirectUrl
            ?: response.checkoutUrl
            ?: response.preferenceId?.let { construirUrlDesdePreferencia(it) }
            ?: throw IllegalStateException("El backend no devolvió URL de checkout para redirección")

        return normalizarCheckoutUrl(url)
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
