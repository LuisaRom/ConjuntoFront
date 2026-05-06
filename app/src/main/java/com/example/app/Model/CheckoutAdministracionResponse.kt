package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CheckoutAdministracionResponse(
    @SerializedName("init_point")
    val initPoint: String? = null,
    @SerializedName("checkoutUrl")
    val checkoutUrl: String? = null,
    @SerializedName("redirectUrl")
    val redirectUrl: String? = null
)
