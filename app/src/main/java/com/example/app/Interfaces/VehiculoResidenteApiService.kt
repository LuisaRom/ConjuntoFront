package com.example.app.Interfaces

import com.example.app.Model.AsignarParqueaderoRequest
import com.example.app.Model.NuevoVehiculoRequest
import com.example.app.Model.VehiculoResidente
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VehiculoResidenteApiService {

    @GET("/api/vehiculos-residentes/mis-vehiculos")
    suspend fun misVehiculos(): List<VehiculoResidente>

    @POST("/api/vehiculos-residentes/mis-vehiculos")
    suspend fun registrarMiVehiculo(@Body request: NuevoVehiculoRequest): VehiculoResidente

    @DELETE("/api/vehiculos-residentes/mis-vehiculos/{id}")
    suspend fun eliminarMiVehiculo(@Path("id") id: Long): Response<Unit>

    @PUT("/api/vehiculos-residentes/{id}/parqueadero")
    suspend fun asignarParqueadero(@Path("id") id: Long, @Body request: AsignarParqueaderoRequest): VehiculoResidente

    @GET("/api/vehiculos-residentes")
    suspend fun obtenerVehiculos(): List<VehiculoResidente>

    @GET("/api/vehiculos-residentes/{id}")
    suspend fun obtenerVehiculo(@Path("id") id: Long): VehiculoResidente

    @POST("/api/vehiculos-residentes")
    suspend fun guardarVehiculo(@Body vehiculo: VehiculoResidente): VehiculoResidente

    @DELETE("/api/vehiculos-residentes/{id}")
    suspend fun eliminarVehiculo(@Path("id") id: Long)
}