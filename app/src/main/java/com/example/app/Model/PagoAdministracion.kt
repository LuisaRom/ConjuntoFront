package com.example.app.Model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

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
