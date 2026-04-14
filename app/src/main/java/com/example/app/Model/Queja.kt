package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class Queja(
    val id: Long? = null,

    @SerializedName("descripcion")
    val descripcion: String = "",

    @SerializedName(value = "tipo", alternate = ["categoria", "tipoQueja"])
    val tipo: String? = null,

    @SerializedName(value = "torreApartamento", alternate = ["torreApto", "ubicacion"])
    val torreApartamento: String? = null,

    @SerializedName(value = "mensaje", alternate = ["detalle"])
    val mensaje: String? = null,

    @SerializedName("fechaCreacion")
    val fechaCreacion: String?, // Formato ISO 8601

    @SerializedName("estado")
    val estado: String = "",

    @SerializedName("usuario")
    val usuario: Usuario? = null
) {
    fun categoriaVisual(): String {
        return tipo?.takeIf { it.isNotBlank() } ?: "Sin categoría"
    }

    fun detalleVisual(): String {
        return mensaje?.takeIf { it.isNotBlank() } ?: descripcion
    }
}

//Preguntar al profe porque no deja poner enumclass y si es mejor dejarlo y agregar un nuevo archivo o asi como esta