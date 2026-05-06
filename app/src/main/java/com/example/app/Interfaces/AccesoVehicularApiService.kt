package com.example.app.Interfaces

import com.example.app.Model.AccesoVehicular
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AccesoVehicularApiService {
    @GET("/api/accesos-vehiculares")
    suspend fun obtenerAccesosVehiculares(): List<AccesoVehicular>

    @GET("/api/accesos-vehiculares/{id}")
    suspend fun obtenerAccesoVehicular(@Path("id") id: Long): AccesoVehicular

    @POST("/api/accesos-vehiculares/crear")
    suspend fun guardarAccesoVehicularCrear(@Body accesoVehicular: AccesoVehicular): AccesoVehicular

    @POST("/api/accesos-vehiculares")
    suspend fun guardarAccesoVehicular(@Body accesoVehicular: AccesoVehicular): AccesoVehicular

    @DELETE("/api/accesos-vehiculares/{id}")
    suspend fun eliminarAccesoVehicular(@Path("id") id: Long)
}
