package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CrearPagoRequest(
    @SerializedName("monto")
    val monto: Long? = null,
    @SerializedName("valor")
    val valor: Long? = null,
    @SerializedName("concepto")
    val concepto: String? = null,
    @SerializedName("descripcion")
    val descripcion: String? = null,
    @SerializedName("periodo")
    val periodo: String? = null,
    @SerializedName("usuarioId")
    val usuarioId: Long? = null,
    @SerializedName("apartamento")
    val apartamento: String? = null
)
