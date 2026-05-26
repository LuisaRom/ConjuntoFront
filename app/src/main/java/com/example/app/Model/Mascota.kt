package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class Mascota(
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("raza")
    val raza: String,

    @SerializedName(
        value = "imagenUrl",
        alternate = [
            "fotoHttpUrl",
            "fotoUrl",
            "foto_url",
            "foto_http_url",
            "imagen_url",
            "urlFoto",
            "urlImagen",
            "foto",
            "imagen"
        ]
    )
    val imagenUrl: String? = null,

    @SerializedName(value = "fechaCreacion", alternate = ["fecha", "createdAt", "fechaPublicacion"])
    val fechaCreacion: String? = null,

    @SerializedName("usuario")
    val usuario: Usuario? = null
)