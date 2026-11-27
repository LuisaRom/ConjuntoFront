package com.example.app.Interfaces

import com.example.app.Model.AceptarTerminosRequest
import com.example.app.Model.EstadoTerminos
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TerminosApiService {

    @GET("/api/terminos/estado")
    suspend fun estado(): EstadoTerminos

    @POST("/api/terminos/aceptar")
    suspend fun aceptar(@Body request: AceptarTerminosRequest): EstadoTerminos
}
