package com.example.app.Interfaces

import com.example.app.Model.Queja
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.ResponseBody

interface QuejaApiService {
    @GET("/api/quejas/todas")
    suspend fun obtenerQuejas(): List<Queja>

    @GET("/api/quejas/todas")
    suspend fun obtenerQuejasRaw(): ResponseBody

    @GET("/api/quejas/todas")
    suspend fun obtenerQuejasPorCategoria(
        @Query("categoria") categoria: String
    ): List<Queja>

    @GET("/api/quejas/todas")
    suspend fun obtenerQuejasPorCategoriaRaw(
        @Query("categoria") categoria: String
    ): ResponseBody

    @GET("/api/quejas/{id}")
    suspend fun obtenerQueja(@Path("id") id: Long): Queja

    @POST("/api/quejas/crear")
    suspend fun guardarQueja(@Body queja: Queja): Queja

    @DELETE("/api/quejas/{id}")
    suspend fun eliminarQueja(@Path("id") id: Long)

    @PUT("/api/quejas/{id}/finalizar")
    suspend fun finalizarQueja(@Path("id") id: Long): Queja
}
