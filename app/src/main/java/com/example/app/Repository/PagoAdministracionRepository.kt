package com.example.app.Repository

import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Model.PagoAdministracion
import javax.inject.Inject

class PagoAdministracionRepository @Inject constructor(
    private val api: PagoAdministracionApiService
) {

    suspend fun obtenerTodos(): List<PagoAdministracion> {
        return api.obtenerPagos()
    }

    suspend fun obtenerPorId(id: Long): PagoAdministracion {
        return api.obtenerPago(id)
    }

    suspend fun guardar(pagoAdministracion: PagoAdministracion): PagoAdministracion {
        return api.guardarPago(pagoAdministracion)
    }

    suspend fun crearCheckoutAdministracion(): String {
        val response = api.iniciarPagoPse()
        return response.redirectUrl ?: response.checkoutUrl
        ?: throw IllegalStateException("El backend no devolvió redirectUrl/checkoutUrl")
    }

    suspend fun eliminar(id: Long) {
        api.eliminarPago(id)
    }
}
