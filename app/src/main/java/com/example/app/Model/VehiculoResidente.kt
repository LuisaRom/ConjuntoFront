package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class VehiculoResidente(
    val id: Long? = null,

    @SerializedName("placa")
    val placa: String? = null,

    @SerializedName("marca")
    val marca: String? = null,

    @SerializedName("modelo")
    val modelo: String? = null,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("tipo")
    val tipo: String? = null,

    @SerializedName("parqueadero")
    val parqueadero: String? = null,

    @SerializedName("fechaRegistro")
    val fechaRegistro: String? = null,

    @SerializedName("usuario")
    val usuario: Usuario? = null
)

data class NuevoVehiculoRequest(
    val placa: String,
    val marca: String,
    val modelo: String,
    val color: String?,
    val tipo: String
)

data class AsignarParqueaderoRequest(val parqueadero: String?)
