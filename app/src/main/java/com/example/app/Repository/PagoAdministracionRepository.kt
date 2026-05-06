package com.example.app.Repository

import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Model.CrearPagoRequest
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

    suspend fun crearCheckoutAdministracion(request: CrearPagoRequest): String {
        val response = api.crearPagoEnLinea(request)
        return response.initPoint ?: response.redirectUrl ?: response.checkoutUrl
            ?: throw IllegalStateException("El backend no devolvió init_point para redirección")
    }

    suspend fun eliminar(id: Long) {
        api.eliminarPago(id)
    }
}
