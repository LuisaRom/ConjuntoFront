package com.example.app.Model

import com.google.gson.annotations.SerializedName


data class Usuario(
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("documento")
    val documento: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("email")
    val email: String = "",

    @SerializedName("usuario")
    val usuario: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("rol")
    val rol: String,

    @SerializedName("torre")
    val torre: String,

    @SerializedName("apartamento")
    val apartamento: String
){
    enum class Rol {
        ADMINISTRADOR, CELADOR, RESIDENTE
    }
}
