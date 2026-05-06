package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class AccesoAdmin(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("tipoAcceso")
    val tipoAcceso: String? = null,

    @SerializedName("nombreVisitante")
    val nombreVisitante: String? = null,

    @SerializedName(value = "quienIngresa", alternate = ["nombrePersona", "visitante"])
    val quienIngresa: String? = null,

    @SerializedName("placaVehiculo")
    val placaVehiculo: String? = null,

    @SerializedName("torre")
    val torre: String? = null,

    @SerializedName("apartamento")
    val apartamento: String? = null,

    @SerializedName("horaAutorizada")
    val horaAutorizada: String? = null,

    @SerializedName("horaEntrada")
    val horaEntrada: String? = null,

    @SerializedName("horaSalida")
    val horaSalida: String? = null,

    @SerializedName(value = "usuario", alternate = ["autorizadoPor"])
    val usuario: Usuario? = null
) {
    fun tipoVisual(): String {
        val tipo = tipoAcceso?.trim().orEmpty().lowercase()
        return when {
            tipo.contains("peat") -> "Peatonal"
            tipo.contains("veh") -> "Vehicular"
            !placaVehiculo.isNullOrBlank() -> "Vehicular"
            else -> "Peatonal"
        }
    }

    fun tituloVisual(): String {
        return when (tipoVisual()) {
            "Vehicular" -> "Vehicular - ${placaVehiculo?.ifBlank { "Sin placa" } ?: "Sin placa"}"
            else -> "Peatonal - ${nombreVisitante?.ifBlank { "Sin nombre" } ?: "Sin nombre"}"
        }
    }

    fun detalleVisual(): String {
        val torreBase = usuario?.torre?.takeIf { it.isNotBlank() } ?: torre?.takeIf { it.isNotBlank() }
        val aptoBase = usuario?.apartamento?.takeIf { it.isNotBlank() } ?: apartamento?.takeIf { it.isNotBlank() }
        val ubicacion = listOfNotNull(
            torreBase?.let { "Torre $it" },
            aptoBase?.let { "Apto $it" }
        ).joinToString(" | ")

        val hora = horaEntrada?.takeIf { it.isNotBlank() }
            ?: horaAutorizada?.takeIf { it.isNotBlank() }
            ?: "Hora no disponible"

        return if (ubicacion.isBlank()) hora else "$ubicacion | $hora"
    }

    fun nombreIngresoVisual(): String {
        return quienIngresa?.takeIf { it.isNotBlank() }
            ?: nombreVisitante?.takeIf { it.isNotBlank() }
            ?: usuario?.nombre?.takeIf { it.isNotBlank() }
            ?: usuario?.usuario?.takeIf { it.isNotBlank() }
            ?: "-"
    }

    fun torreVisual(): String {
        return usuario?.torre?.takeIf { it.isNotBlank() } ?: torre?.takeIf { it.isNotBlank() } ?: "-"
    }

    fun apartamentoVisual(): String {
        return usuario?.apartamento?.takeIf { it.isNotBlank() } ?: apartamento?.takeIf { it.isNotBlank() } ?: "-"
    }
}
