package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class ConfirmarPagoRequest(
    @SerializedName("referenciaExterna")
    val referenciaExterna: String
)
