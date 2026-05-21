package com.example.app.Repository

import android.util.Log
import com.example.app.Auth.AuthManager
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
    private val tag = "PagosCheckout"

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
        val token = AuthManager.getToken()
        Log.d(tag, "=== POST /api/pagos/checkout/administracion ===")
        Log.d(tag, "Request body: ${gson.toJson(request)}")
        Log.d(
            tag,
            if (token.isNullOrBlank()) {
                "Authorization: NO CONFIGURADO (sin JWT en sesión)"
            } else {
                "Authorization: Bearer *** (presente, len=${token.length}, expirado=${AuthManager.isTokenExpired()})"
            }
        )

        if (token.isNullOrBlank()) {
            throw IllegalStateException("No hay sesión activa. Inicia sesión de nuevo.")
        }
        if (AuthManager.isTokenExpired()) {
            AuthManager.handleUnauthorized()
            throw IllegalStateException("El token JWT expiró. Inicia sesión de nuevo.")
        }

        return try {
            val response = api.crearCheckoutAdministracion(request)
            Log.d(tag, "Response 200 OK (completo): ${gson.toJson(response)}")
            Log.d(
                tag,
                "initPoint=${response.initPoint}, checkoutUrl=${response.checkoutUrl}, " +
                    "sandboxInitPoint=${response.sandboxInitPoint}, redirectUrl=${response.redirectUrl}"
            )
            response
        } catch (error: HttpException) {
            val cuerpo = error.response()?.errorBody()?.string().orEmpty()
            Log.e(tag, "Response HTTP ${error.code()} body=$cuerpo")

            when (error.code()) {
                400 -> {
                    Log.e(tag, "400 Bad Request. Revisa usuarioId, apartamento, monto y periodo en el body.")
                    throw IllegalStateException(
                        extraerMensajeHttp(error).ifBlank {
                            "Datos de pago inválidos (400). Verifica monto y periodo."
                        }
                    )
                }
                401 -> {
                    val jwtExpirado = AuthManager.isTokenExpired()
                    Log.e(tag, "401 Unauthorized. JWT expirado según payload: $jwtExpirado")
                    AuthManager.handleUnauthorized()
                    throw IllegalStateException(
                        if (jwtExpirado) {
                            "Sesión expirada (JWT vencido). Inicia sesión de nuevo."
                        } else {
                            extraerMensajeHttp(error).ifBlank { "No autorizado (401). Verifica tu sesión." }
                        }
                    )
                }
                404 -> {
                    Log.w(tag, "404 en checkout/administracion, probando rutas alternas...")
                    runCatching { api.crearPagoEnLinea(request) }
                        .recoverCatching { api.crearPagoEnLineaCheckout(request) }
                        .recoverCatching { api.crearPagoEnLineaMercadoPago(request) }
                        .getOrElse { throw IllegalStateException(extraerMensajeHttp(error), error) }
                        .also { fallback ->
                            Log.d(tag, "Response fallback (completo): ${gson.toJson(fallback)}")
                        }
                }
                else -> throw IllegalStateException(extraerMensajeHttp(error), error)
            }
        }
    }

    suspend fun extraerUrlCheckout(response: CheckoutAdministracionResponse): String {
        val url = response.initPoint
            ?: response.checkoutUrl
            ?: response.sandboxInitPoint
            ?: response.redirectUrl
            ?: response.preferenceId?.let { construirUrlDesdePreferencia(it) }
            ?: throw IllegalStateException("El backend no devolvió initPoint ni checkoutUrl para redirección")

        Log.d(tag, "URL de checkout seleccionada: $url")
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
                ?: json.get("detalle")?.asString
                ?: json.get("detail")?.asString
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
