package com.example.app.Repository

import com.example.app.DTO.LoginRequest
import com.example.app.Interfaces.RetrofitClient.RetrofitClient
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class UsuarioRepository @Inject constructor() {

    private val api: UsuarioApiService
        get() = RetrofitClient.usuarioApiService

    suspend fun obtenerTodos(): List<Usuario> {
        return api.obtenerUsuarios()
    }

    suspend fun obtenerPorId(id: Long): Usuario {
        return api.obtenerUsuario(id)
    }

    suspend fun guardar(usuario: Usuario): Usuario {
        return try {
            api.crearUsuario(usuario)
        } catch (e: HttpException) {
            // Compatibilidad con versiones del backend que aún usan /api/usuarios.
            if (e.code() == 404 || e.code() == 405) {
                api.guardarUsuario(usuario)
            } else {
                throw e
            }
        }
    }

    suspend fun eliminar(id: Long) {
        api.eliminarUsuario(id)
    }

    suspend fun login(usuario: String, password: String): Usuario {
        return withContext(Dispatchers.IO) {
            val response = api.login(LoginRequest(usuario, password))
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else {
                when (response.code()) {
                    401 -> throw Exception("Usuario o contraseña incorrectos")
                    404 -> throw Exception("Servicio no encontrado")
                    500 -> throw Exception("Error del servidor. Intenta más tarde.")
                    else -> throw Exception("Error de login: ${response.code()} - ${response.message()}")
                }
            }
        }
    }
}