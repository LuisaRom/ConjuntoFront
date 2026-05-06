package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CrearUsuarioRequest(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("documento")
    val documento: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("usuario")
    val usuario: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("rol")
    val rol: String,
    @SerializedName("torre")
    val torre: String? = null,
    @SerializedName("apartamento")
    val apartamento: String? = null
)
