package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class CheckoutAdministracionResponse(
    @SerializedName(value = "init_point", alternate = ["initPoint", "preferenceInitPoint", "mercadoPagoUrl"])
    val initPoint: String? = null,
    @SerializedName(value = "checkoutUrl", alternate = ["checkout_url", "url", "paymentUrl", "link"])
    val checkoutUrl: String? = null,
    @SerializedName(value = "redirectUrl", alternate = ["redirect_url"])
    val redirectUrl: String? = null,
    @SerializedName(value = "sandbox_init_point", alternate = ["sandboxInitPoint"])
    val sandboxInitPoint: String? = null,
    @SerializedName(value = "preferenceId", alternate = ["preference_id", "id", "preference"])
    val preferenceId: String? = null
)
