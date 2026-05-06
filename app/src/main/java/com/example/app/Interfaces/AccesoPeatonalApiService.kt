package com.example.app.Interfaces

import com.example.app.Model.AccesoPeatonal
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AccesoPeatonalApiService {

    @GET("/api/accesos-peatonales")
    suspend fun obtenerAccesosPeatonales(): List<AccesoPeatonal>

    @GET("/api/accesos-peatonales/{id}")
    suspend fun obtenerAccesoPeatonal(@Path("id") id: Long): AccesoPeatonal

    @POST("/api/accesos-peatonales/crear")
    suspend fun guardarAccesoPeatonalCrear(@Body accesoPeatonal: AccesoPeatonal): AccesoPeatonal

    @POST("/api/accesos-peatonales")
    suspend fun guardarAccesoPeatonal(@Body accesoPeatonal: AccesoPeatonal): AccesoPeatonal

    @DELETE("/api/accesos-peatonales/{id}")
    suspend fun eliminarAccesoPeatonal(@Path("id") id: Long)
}

