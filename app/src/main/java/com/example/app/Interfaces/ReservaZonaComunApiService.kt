package com.example.app.Interfaces

import com.example.app.Model.ReservaZonaComun
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservaZonaComunApiService {
    @GET("/api/reservas/todas")
    suspend fun obtenerReservas(): List<ReservaZonaComun>

    @GET("/api/reservas/{id}")
    suspend fun obtenerReserva(@Path("id") id: Long): ReservaZonaComun

    @POST("/api/reservas/crear")
    suspend fun guardarReserva(@Body reserva: ReservaZonaComun): ReservaZonaComun

    @DELETE("/api/reservas/{id}")
    suspend fun eliminarReserva(@Path("id") id: Long)
}
