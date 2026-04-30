package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CheckoutAdministracionResponse(
    @SerializedName("checkoutUrl")
    val checkoutUrl: String
)
