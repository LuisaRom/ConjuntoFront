package com.example.app.Repository

import com.example.app.Interfaces.TerminosApiService
import com.example.app.Model.AceptarTerminosRequest
import com.example.app.Model.EstadoTerminos
import com.example.app.Model.TerminosCondiciones
import javax.inject.Inject

class TerminosRepository @Inject constructor(
    private val api: TerminosApiService
) {
    suspend fun estado(): EstadoTerminos = api.estado()

    suspend fun aceptar(dispositivo: String): EstadoTerminos = api.aceptar(
        AceptarTerminosRequest(
            version = TerminosCondiciones.VERSION,
            aceptaTerminos = true,
            autorizaTratamientoDatos = true,
            aceptaAvisoIA = true,
            dispositivo = dispositivo
        )
    )
}
