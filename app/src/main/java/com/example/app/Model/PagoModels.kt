package com.example.app.Model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val MONTO_ADMINISTRACION = 150_000L
const val CONCEPTO_PAGO_ADMINISTRACION = "Pago administracion"
const val MONTO_ADMINISTRACION_TEXTO = "150.000 COP"

enum class ResultadoSimulacionPago(val estadoPago: String, val mensaje: String) {
    APROBADO("APROBADO", "Pago de administración simulado correctamente (modo prueba)."),
    RECHAZADO("RECHAZADO", "Pago simulado rechazado (modo prueba)."),
    PENDIENTE("PENDIENTE", "Pago simulado quedó pendiente (modo prueba).")
}

fun referenciaSimulacionPago(usuarioId: Long?, periodo: String): String {
    val uid = usuarioId ?: 0L
    return "SIM-ADMIN-$uid-$periodo-${System.currentTimeMillis()}"
}

data class CrearPagoRequest(
    @SerializedName("monto")
    val monto: Long,
    @SerializedName("concepto")
    val concepto: String,
    @SerializedName("periodo")
    val periodo: String
)

data class ConfirmarPagoRequest(
    @SerializedName("referenciaExterna")
    val referenciaExterna: String
)

data class ConfirmarPagoResponse(
    @SerializedName("estadoPago")
    val estadoPago: String? = null,
    @SerializedName("mensaje")
    val mensaje: String? = null,
    @SerializedName("referenciaExterna")
    val referenciaExterna: String? = null,
    @SerializedName("pagoId")
    val pagoId: Long? = null
)

data class CheckoutAdministracionResponse(
    @SerializedName(value = "init_point", alternate = ["initPoint", "preferenceInitPoint", "mercadoPagoUrl"])
    val initPoint: String? = null,
    @SerializedName(value = "checkoutUrl", alternate = ["checkout_url", "url", "paymentUrl", "link"])
    val checkoutUrl: String? = null,
    @SerializedName(value = "redirectUrl", alternate = ["redirect_url"])
    val redirectUrl: String? = null,
    @SerializedName(value = "sandbox_init_point", alternate = ["sandboxInitPoint"])
    val sandboxInitPoint: String? = null,
    @SerializedName(value = "preferenceId", alternate = ["preference_id", "mercadoPagoPreferenceId", "preference"])
    val preferenceId: String? = null,
    @SerializedName("referenciaExterna")
    val referenciaExterna: String? = null,
    @SerializedName("estado")
    val estado: String? = null,
    @SerializedName("mensaje")
    val mensaje: String? = null
)

data class PagoAdministracion(
    val id: Long? = null,
    @SerializedName("monto")
    val monto: BigDecimal? = null,
    @SerializedName("fechaPago")
    val fechaPago: String? = null,
    @SerializedName("metodoPago")
    val metodoPago: String? = null,
    @SerializedName("estadoPago")
    val estadoPago: String? = null,
    @SerializedName("concepto")
    val concepto: String? = null,
    @SerializedName("periodo")
    val periodo: String? = null,
    @SerializedName("referenciaExterna")
    val referenciaExterna: String? = null,
    @SerializedName("checkoutUrl")
    val checkoutUrl: String? = null,
    @SerializedName("usuario")
    val usuario: Usuario? = null,
    @SerializedName("usuarioNombre")
    val usuarioNombre: String? = null
)

data class EstadoPagoAdmin(
    @SerializedName("usuarioId")
    val usuarioId: Long? = null,
    @SerializedName(value = "usuarioNombre", alternate = ["nombre"])
    val usuarioNombre: String? = null,
    @SerializedName("usuarioUsername")
    val usuarioUsername: String? = null,
    @SerializedName("torre")
    val torre: String? = null,
    @SerializedName("apartamento")
    val apartamento: String? = null,
    @SerializedName("periodoActual")
    val periodoActual: String? = null,
    @SerializedName("diaLimitePago")
    val diaLimitePago: Int? = null,
    @SerializedName("fechaLimitePago")
    val fechaLimitePago: String? = null,
    @SerializedName("estadoAdministracion")
    val estadoAdministracion: String? = null,
    @SerializedName("estaAlDia")
    val estaAlDia: Boolean? = null,
    @SerializedName(value = "alDia", alternate = ["al_dia"])
    val alDia: Boolean? = null,
    @SerializedName("estaEnMora")
    val estaEnMora: Boolean? = null,
    @SerializedName("diasMora")
    val diasMora: Long? = null,
    @SerializedName("pagoPeriodoActual")
    val pagoPeriodoActual: PagoAdministracion? = null,
    @SerializedName("historialPagos")
    val historialPagos: List<PagoAdministracion>? = null,
    @SerializedName("usuario")
    val usuario: Usuario? = null
) {
    fun nombreMostrar(): String =
        usuarioNombre?.takeIf { it.isNotBlank() }
            ?: usuario?.nombre?.takeIf { it.isNotBlank() }
            ?: usuario?.usuario?.takeIf { it.isNotBlank() }
            ?: "Residente"

    fun estaAlDiaEfectivo(): Boolean {
        estaAlDia?.let { return it }
        alDia?.let { return it }
        val estado = estadoAdministracion.orEmpty().uppercase()
        return estado == "AL_DIA" || estado == "PAGADO_EN_MORA"
    }

    fun etiquetaEstado(): String {
        val estado = estadoAdministracion.orEmpty().ifBlank { return "Sin registro" }
        return when (estado.uppercase()) {
            "AL_DIA" -> "Al día"
            "PENDIENTE_EN_PLAZO" -> "Pendiente (en plazo)"
            "EN_MORA" -> "En mora"
            "PAGADO_EN_MORA" -> "Pagado en mora"
            else -> estado.replace('_', ' ')
        }
    }
}

/** Equivalente a `YearMonth.now().toString()` (yyyy-MM), compatible con minSdk 24. */
fun periodoCheckoutActual(): String {
    return SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time)
}

fun normalizarPeriodoPago(periodo: String?): String {
    val valor = periodo?.trim().orEmpty()
    if (valor.length >= 7) return valor.substring(0, 7)
    return valor
}

fun esPagoAprobado(estadoPago: String?): Boolean {
    val estado = estadoPago.orEmpty().uppercase()
    return estado.contains("APROB") || estado == "PAGADO" || estado == "PAGADO_EN_MORA"
}

fun esPagoPendienteCheckout(estadoPago: String?): Boolean {
    val estado = estadoPago.orEmpty().uppercase()
    if (estado.isBlank()) return true
    return estado.contains("PEND") ||
        estado.contains("INICI") ||
        estado.contains("CREAD") ||
        estado == "EN_PROCESO"
}

fun PagoAdministracion.urlCheckoutDisponible(): String? {
    return checkoutUrl?.trim()?.takeIf { it.isNotBlank() }
}
