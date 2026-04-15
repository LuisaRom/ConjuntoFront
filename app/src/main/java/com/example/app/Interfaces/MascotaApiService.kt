package com.example.app.Interfaces

import com.example.app.Model.Mascota
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MascotaApiService {

    @GET("/api/mascotas")
    suspend fun obtenerMascotas(): List<Mascota>

    @GET("/api/mascotas/{id}")
    suspend fun obtenerMascota(@Path("id") id: Long): Mascota

    @POST("/api/mascotas/crear")
    suspend fun guardarMascota(@Body mascota: Mascota): Mascota

    @DELETE("/api/mascotas/{id}")
    suspend fun eliminarMascota(@Path("id") id: Long)
}
