package com.example.app.Model

import com.google.gson.annotations.SerializedName

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
