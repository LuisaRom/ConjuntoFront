package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CrearPagoRequest(
    @SerializedName("usuarioId")
    val usuarioId: Long,
    @SerializedName("apartamento")
    val apartamento: String,
    @SerializedName("valor")
    val valor: Long,
    @SerializedName("descripcion")
    val descripcion: String
)
