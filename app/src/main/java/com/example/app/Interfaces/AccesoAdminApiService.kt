package com.example.app.Interfaces

import com.example.app.Model.AccesoAdmin
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface AccesoAdminApiService {
    @GET("/api/accesos/todos")
    suspend fun obtenerTodos(): List<AccesoAdmin>

    @DELETE("/api/accesos/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
