package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class AccesoPeatonal(
    val id: Long? = null,

    @SerializedName("nombreVisitante")
    val nombreVisitante: String,

    @SerializedName(value = "fecha", alternate = ["fechaAcceso"])
    val fecha: String? = null,

    @SerializedName("torre")
    val torre: String,

    @SerializedName("apartamento")
    val apartamento: String,

    @SerializedName("codigoQr")
    val codigoQr: String,

    @SerializedName("visitante")
    val visitante: Visitante? = null,

    @SerializedName("autorizadoPor")
    val autorizadoPor: Usuario? = null,

    @SerializedName("horaAutorizada")
    val horaAutorizada: String?,  // En formato ISO 8601

    @SerializedName("horaEntrada")
    val horaEntrada: String?,

    @SerializedName("horaSalida")
    val horaSalida: String?
)