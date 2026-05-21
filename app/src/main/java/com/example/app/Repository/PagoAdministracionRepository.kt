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
import com.example.app.Model.esPagoAprobado
import com.example.app.Model.esPagoPendienteCheckout
import com.example.app.Model.normalizarPeriodoPago
import com.example.app.Model.periodoCheckoutActual
import com.example.app.Model.urlCheckoutDisponible
import com.example.app.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import retrofit2.Response
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

    suspend fun buscarPagoPeriodoActual(periodo: String = periodoCheckoutActual()): PagoAdministracion? {
        val periodoNorm = normalizarPeriodoPago(periodo)
        return obtenerTodos()
            .filter { normalizarPeriodoPago(it.periodo) == periodoNorm }
            .maxByOrNull { it.id ?: 0L }
    }

    suspend fun resolverCheckoutPeriodoActual(periodo: String = periodoCheckoutActual()): String? {
        val pago = buscarPagoPeriodoActual(periodo) ?: return null
        return pago.urlCheckoutDisponible()?.let { normalizarCheckoutUrl(it) }
    }

    suspend fun prepararCheckoutPeriodo(periodo: String = periodoCheckoutActual()): ResultadoPreparacionCheckout {
        val pago = buscarPagoPeriodoActual(periodo)
        if (pago == null) return ResultadoPreparacionCheckout.ContinuarCreacion

        if (esPagoAprobado(pago.estadoPago)) {
            return ResultadoPreparacionCheckout.YaPagado
        }

        pago.urlCheckoutDisponible()?.let { url ->
            return ResultadoPreparacionCheckout.AbrirUrlExistente(normalizarCheckoutUrl(url))
        }

        if (esPagoPendienteCheckout(pago.estadoPago) && pago.id != null) {
            Log.w(tag, "Eliminando pago pendiente incompleto id=${pago.id} periodo=$periodo")
            runCatching { eliminar(pago.id) }
                .onFailure { e ->
                    Log.e(tag, "No se pudo eliminar pago pendiente id=${pago.id}", e)
                }
        }

        return ResultadoPreparacionCheckout.ContinuarCreacion
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

        var response = api.crearCheckout(request)
        if (!response.isSuccessful && response.code() in listOf(404, 500, 502, 503)) {
            Log.w(tag, "checkout/administracion falló (${response.code()}), probando rutas Mercado Pago...")
            response = intentarCheckoutAlternativo(request, response)
        }
        val cuerpoError = response.errorBody()?.string().orEmpty()
        Log.d(tag, "Response HTTP ${response.code()} body=${if (response.isSuccessful) gson.toJson(response.body()) else cuerpoError}")

        if (response.isSuccessful) {
            val body = response.body()
                ?: throw IllegalStateException("El servidor respondió sin datos de checkout")
            validarRespuestaCheckout(body)
            Log.d(
                tag,
                "initPoint=${body.initPoint}, checkoutUrl=${body.checkoutUrl}, " +
                    "sandboxInitPoint=${body.sandboxInitPoint}, redirectUrl=${body.redirectUrl}"
            )
            return body
        }

        when (response.code()) {
            400 -> {
                Log.e(tag, "400 Bad Request. Revisa monto (${request.monto}) y periodo (${request.periodo}).")
                throw IllegalStateException(
                    extraerMensajeHttp(cuerpoError, response.code()).ifBlank {
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
                        extraerMensajeHttp(cuerpoError, response.code()).ifBlank { "No autorizado (401). Verifica tu sesión." }
                    }
                )
            }
            404 -> {
                Log.w(tag, "404 en checkout/administracion, probando rutas alternas...")
                val alt = intentarCheckoutAlternativo(request, response)
                if (!alt.isSuccessful) {
                    throw IllegalStateException(
                        extraerMensajeHttp(cuerpoError, response.code()),
                        HttpException(response)
                    )
                }
                val body = alt.body()
                    ?: throw IllegalStateException("El servidor respondió sin datos de checkout")
                validarRespuestaCheckout(body)
                return body
            }
            500, 409, 422 -> {
                val mensaje = extraerMensajeHttp(cuerpoError, response.code())
                if (esErrorRegistroBd(mensaje)) {
                    resolverCheckoutPeriodoActual(request.periodo)?.let { url ->
                        Log.w(tag, "Recuperado checkout existente tras error de BD: $url")
                        return CheckoutAdministracionResponse(checkoutUrl = url, initPoint = url)
                    }
                }
                throw IllegalStateException(mensaje, HttpException(response))
            }
            else -> throw IllegalStateException(
                extraerMensajeHttp(cuerpoError, response.code()),
                HttpException(response)
            )
        }
    }

    private fun validarRespuestaCheckout(body: CheckoutAdministracionResponse) {
        val mensaje = body.mensaje?.trim().orEmpty()
        val tieneUrl = !body.initPoint.isNullOrBlank() ||
            !body.checkoutUrl.isNullOrBlank() ||
            !body.sandboxInitPoint.isNullOrBlank() ||
            !body.redirectUrl.isNullOrBlank() ||
            !body.preferenceId.isNullOrBlank()

        if (tieneUrl) return

        if (mensaje.isNotBlank()) {
            throw IllegalStateException(mensaje)
        }
        throw IllegalStateException("El servidor no devolvió la URL de Mercado Pago.")
    }

    private fun esErrorRegistroBd(mensaje: String): Boolean {
        val texto = mensaje.lowercase()
        return texto.contains("base de datos") ||
            texto.contains("registrar el pago") ||
            texto.contains("registro") && texto.contains("pago")
    }

    sealed class ResultadoPreparacionCheckout {
        data object ContinuarCreacion : ResultadoPreparacionCheckout()
        data object YaPagado : ResultadoPreparacionCheckout()
        data class AbrirUrlExistente(val url: String) : ResultadoPreparacionCheckout()
    }

    suspend fun extraerUrlCheckout(response: CheckoutAdministracionResponse): String {
        val url = if (BuildConfig.PAGO_MODO_PRUEBA) {
            response.sandboxInitPoint
                ?: response.initPoint
                ?: response.checkoutUrl
                ?: response.redirectUrl
        } else {
            response.initPoint
                ?: response.checkoutUrl
                ?: response.sandboxInitPoint
                ?: response.redirectUrl
        } ?: response.preferenceId?.let { construirUrlDesdePreferencia(it, sandbox = BuildConfig.PAGO_MODO_PRUEBA) }
            ?: throw IllegalStateException(
                "El backend no devolvió URL de Mercado Pago. " +
                    "Revisa credenciales TEST en el servidor (Render)."
            )

        Log.d(tag, "URL de checkout seleccionada (modoPrueba=${BuildConfig.PAGO_MODO_PRUEBA}): $url")
        return normalizarCheckoutUrl(url)
    }

    private suspend fun intentarCheckoutAlternativo(
        request: CrearPagoRequest,
        original: Response<CheckoutAdministracionResponse>
    ): Response<CheckoutAdministracionResponse> {
        val alternativas = listOf(
            "mercado-pago/crear" to suspend { api.crearPagoMercadoPagoCrear(request) },
            "mercado-pago" to suspend { api.crearPagoMercadoPagoDash(request) },
            "mercadopago/crear" to suspend { api.crearPagoMercadoPagoCrearAlt(request) },
            "mercadopago" to suspend { api.crearPagoEnLineaMercadoPago(request) },
            "checkout" to suspend { api.crearPagoEnLineaCheckout(request) },
            "crear" to suspend { api.crearPagoEnLinea(request) }
        )
        for ((nombre, llamada) in alternativas) {
            val alt = runCatching { llamada() }.getOrNull() ?: continue
            Log.d(tag, "Ruta alternativa $nombre -> HTTP ${alt.code()}")
            if (alt.isSuccessful) return alt
        }
        return original
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

    private fun extraerMensajeHttp(cuerpo: String, codigo: Int): String {
        val texto = cuerpo.trim()
        if (texto.isBlank()) {
            return "Error del servidor ($codigo)"
        }
        return runCatching {
            val json = gson.fromJson(texto, JsonObject::class.java)
            json.get("mensaje")?.asString
                ?: json.get("error")?.asString
                ?: json.get("message")?.asString
                ?: json.get("detalle")?.asString
                ?: json.get("detail")?.asString
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: texto.removeSurrounding("\"")
    }

    private fun extraerMensajeHttp(error: HttpException): String {
        val cuerpo = error.response()?.errorBody()?.string().orEmpty()
        return extraerMensajeHttp(cuerpo, error.code())
    }

    private fun normalizarCheckoutUrl(url: String): String {
        val valor = url.trim()
        return if (valor.startsWith("http://") || valor.startsWith("https://")) valor else "https://$valor"
    }

    private fun construirUrlDesdePreferencia(preferenceId: String, sandbox: Boolean = false): String {
        val pref = preferenceId.trim()
        if (pref.isBlank()) throw IllegalStateException("PreferenceId vacío")
        return if (sandbox) {
            "https://sandbox.mercadopago.com.co/checkout/v1/redirect?pref_id=$pref"
        } else {
            "https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=$pref"
        }
    }
}
