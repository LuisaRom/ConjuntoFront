package com.example.app.Repository

import com.example.app.Interfaces.AccesoVehicularApiService
import com.example.app.Model.AccesoVehicular
import javax.inject.Inject

class AccesoVehicularRepository @Inject constructor(
    private val api: AccesoVehicularApiService
) {

    suspend fun obtenerTodos(): List<AccesoVehicular> {
        return api.obtenerAccesosVehiculares()
    }

    suspend fun obtenerPorId(id: Long): AccesoVehicular {
        return api.obtenerAccesoVehicular(id)
    }

    suspend fun guardar(accesoVehicular: AccesoVehicular): AccesoVehicular {
        return runCatching { api.guardarAccesoVehicularCrear(accesoVehicular) }
            .getOrElse { api.guardarAccesoVehicular(accesoVehicular) }
    }

    suspend fun eliminar(id: Long) {
        api.eliminarAccesoVehicular(id)
    }
}
