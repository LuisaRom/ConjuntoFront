package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class AccesoVehicular(
    val id: Long? = null,

    @SerializedName("placaVehiculo")
    val placaVehiculo: String,

    @SerializedName(value = "tipoVehiculo", alternate = ["tipo"])
    val tipoVehiculo: String? = null,

    @SerializedName(value = "quienIngresa", alternate = ["nombrePersona"])
    val quienIngresa: String? = null,

    @SerializedName(value = "fecha", alternate = ["fechaAcceso"])
    val fecha: String? = null,

    @SerializedName("torre")
    val torre: String,

    @SerializedName("apartamento")
    val apartamento: String,
    
    @SerializedName("codigoQr")
    val codigoQr: String? = null,

    @SerializedName("qrBase64")
    val qrBase64: String? = null,

    @SerializedName("qrDataUrl")
    val qrDataUrl: String? = null,

    @SerializedName("visitante")
    val visitante: Visitante? = null,

    @SerializedName("autorizadoPor")
    val autorizadoPor: Usuario? = null,

    @SerializedName("horaAutorizada")
    val horaAutorizada: String?,  // ISO 8601

    @SerializedName("horaEntrada")
    val horaEntrada: String?,

    @SerializedName("horaSalida")
    val horaSalida: String?
)