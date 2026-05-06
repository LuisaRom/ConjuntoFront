package com.example.app.Repository

import com.example.app.Auth.AuthManager
import com.example.app.DTO.ApiErrorResponse
import com.example.app.DTO.LoginRequest
import com.example.app.DTO.LoginResponse
import com.example.app.Interfaces.RetrofitClient.RetrofitClient
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Model.CrearUsuarioRequest
import com.example.app.Model.Usuario
import com.google.gson.Gson
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
        val request = CrearUsuarioRequest(
            nombre = usuario.nombre.trim(),
            documento = usuario.documento.trim(),
            telefono = usuario.telefono.trim(),
            email = usuario.email.trim(),
            usuario = usuario.usuario.trim(),
            password = usuario.password,
            rol = usuario.rol.trim().uppercase(),
            torre = usuario.torre.takeIf { it.isNotBlank() }?.trim(),
            apartamento = usuario.apartamento.takeIf { it.isNotBlank() }?.trim()
        )
        return try {
            api.crearUsuario(request)
        } catch (e: HttpException) {
            // Compatibilidad con versiones del backend que aún usan /api/usuarios.
            if (e.code() == 404 || e.code() == 405 || e.code() == 403) {
                api.guardarUsuario(request)
            } else {
                val errorBody = e.response()?.errorBody()?.string().orEmpty()
                val backendError = runCatching {
                    Gson().fromJson(errorBody, ApiErrorResponse::class.java)?.error
                }.getOrNull().orEmpty()
                if (e.code() == 500) {
                    throw Exception(backendError.ifBlank { "Error del servidor al crear usuario. Verifica documento, rol y datos obligatorios." })
                }
                throw Exception(backendError.ifBlank { "Error al crear usuario (${e.code()})" })
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
                val body: LoginResponse = response.body()
                    ?: throw Exception("Respuesta vacía del servidor")
                AuthManager.saveSession(body.token, body.usuario)
                body.usuario
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                val backendError = runCatching {
                    Gson().fromJson(errorBody, ApiErrorResponse::class.java)?.error
                }.getOrNull().orEmpty()
                when (response.code()) {
                    401 -> throw Exception(
                        backendError.ifBlank { "Usuario o contraseña incorrectos" }
                    )
                    403 -> throw Exception("No tienes permisos")
                    404 -> throw Exception("Servicio no encontrado")
                    500 -> throw Exception("Error del servidor. Intenta más tarde.")
                    else -> throw Exception("Error de login: ${response.code()} - ${response.message()}")
                }
            }
        }
    }
}