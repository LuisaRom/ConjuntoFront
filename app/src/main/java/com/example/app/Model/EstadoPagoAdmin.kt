package com.example.app.Model

import com.google.gson.annotations.SerializedName

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
