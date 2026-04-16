package com.example.app.Interfaces

import com.example.app.Model.Mascota
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part

interface MascotaApiService {

    @GET("/api/mascotas")
    suspend fun obtenerMascotas(): List<Mascota>

    @GET("/api/mascotas/{id}")
    suspend fun obtenerMascota(@Path("id") id: Long): Mascota

    @POST("/api/mascotas/crear")
    suspend fun guardarMascota(@Body mascota: Mascota): Mascota

    @Multipart
    @POST("/api/mascotas/crear")
    suspend fun guardarMascotaMultipart(
        @Part("nombre") nombre: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part foto: MultipartBody.Part
    ): Mascota

    @DELETE("/api/mascotas/{id}")
    suspend fun eliminarMascota(@Path("id") id: Long)
}
