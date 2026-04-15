package com.example.app.Interfaces

import com.example.app.DTO.LoginRequest
import com.example.app.DTO.LoginResponse
import com.example.app.Model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioApiService {

    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): List<Usuario>

    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): Usuario

    @POST("api/usuarios")
    suspend fun guardarUsuario(@Body usuario: Usuario): Usuario

    @POST("api/usuarios/crear")
    suspend fun crearUsuario(@Body usuario: Usuario): Usuario

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long)

    @POST("api/usuarios/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}

