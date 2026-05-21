package com.example.app.Model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CrearPagoRequest(
    @SerializedName("usuarioId")
    val usuarioId: Long,
    @SerializedName("apartamento")
    val apartamento: String,
    @SerializedName("monto")
    val monto: Long,
    @SerializedName("valor")
    val valor: Long,
    @SerializedName("concepto")
    val concepto: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("periodo")
    val periodo: String
)

fun periodoPagoMesActual(): String {
    return SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time)
}

fun apartamentoParaPago(usuario: Usuario?): String {
    val torre = usuario?.torre?.trim().orEmpty()
    val apto = usuario?.apartamento?.trim().orEmpty()
    return when {
        torre.isNotBlank() && apto.isNotBlank() -> "$torre-$apto"
        apto.isNotBlank() -> apto
        torre.isNotBlank() -> torre
        else -> usuario?.usuario?.trim().orEmpty().ifBlank { "0" }
    }
}
