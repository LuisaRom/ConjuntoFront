package com.example.app.Interfaces

import com.example.app.Model.Mascota
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MascotaApiService {

    @GET("/api/mascotas")
    suspend fun obtenerMascotas(): List<Mascota>

    @GET("/api/mascotas/{id}")
    suspend fun obtenerMascota(@Path("id") id: Long): Mascota

    @POST("/api/mascotas")
    suspend fun guardarMascota(@Body mascota: Mascota): Mascota

    @POST("/api/mascotas/crear")
    suspend fun guardarMascotaCrear(@Body mascota: Mascota): Mascota

    @Multipart
    @POST("/api/mascotas")
    suspend fun guardarMascotaConFoto(
        @Query("nombre") nombre: String,
        @Query("tipo") tipo: String,
        @Query("raza") raza: String,
        @Query("descripcion") descripcion: String? = null,
        @Part foto: MultipartBody.Part
    ): Mascota

    @Multipart
    @POST("/api/mascotas")
    suspend fun guardarMascotaConImagen(
        @Query("nombre") nombre: String,
        @Query("tipo") tipo: String,
        @Query("raza") raza: String,
        @Query("descripcion") descripcion: String? = null,
        @Part imagen: MultipartBody.Part
    ): Mascota

    @Multipart
    @POST("/api/mascotas/crear")
    suspend fun guardarMascotaConFotoCrear(
        @Query("nombre") nombre: String,
        @Query("tipo") tipo: String,
        @Query("raza") raza: String,
        @Query("descripcion") descripcion: String? = null,
        @Part foto: MultipartBody.Part
    ): Mascota

    @Multipart
    @POST("/api/mascotas/crear")
    suspend fun guardarMascotaConImagenCrear(
        @Query("nombre") nombre: String,
        @Query("tipo") tipo: String,
        @Query("raza") raza: String,
        @Query("descripcion") descripcion: String? = null,
        @Part imagen: MultipartBody.Part
    ): Mascota

    @DELETE("/api/mascotas/{id}")
    suspend fun eliminarMascota(@Path("id") id: Long)
}
