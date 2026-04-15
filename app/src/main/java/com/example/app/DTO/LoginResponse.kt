package com.example.app.DTO

import com.example.app.Model.Usuario
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("tokenType")
    val tokenType: String = "Bearer",
    @SerializedName("usuario")
    val usuario: Usuario
)
