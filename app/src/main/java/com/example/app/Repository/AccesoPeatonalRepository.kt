package com.example.app.Repository

import com.example.app.Interfaces.AccesoPeatonalApiService
import com.example.app.Model.AccesoPeatonal
import javax.inject.Inject

class AccesoPeatonalRepository @Inject constructor(
    private val api: AccesoPeatonalApiService
) {

    suspend fun obtenerAccesosPeatonales(): List<AccesoPeatonal> {
        return api.obtenerAccesosPeatonales()
    }

    suspend fun obtenerAccesoPeatonal(id: Long): AccesoPeatonal {
        return api.obtenerAccesoPeatonal(id)
    }

    suspend fun guardarAccesoPeatonal(accesoPeatonal: AccesoPeatonal): AccesoPeatonal {
        return runCatching { api.guardarAccesoPeatonalCrear(accesoPeatonal) }
            .getOrElse { api.guardarAccesoPeatonal(accesoPeatonal) }
    }

    suspend fun eliminarAccesoPeatonal(id: Long) {
        api.eliminarAccesoPeatonal(id)
    }
}
