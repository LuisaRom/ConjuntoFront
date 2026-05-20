package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CrearPagoRequest(
    @SerializedName("monto")
    val monto: Long,
    @SerializedName("valor")
    val valor: Long,
    @SerializedName("concepto")
    val concepto: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("periodo")
    val periodo: String
)
