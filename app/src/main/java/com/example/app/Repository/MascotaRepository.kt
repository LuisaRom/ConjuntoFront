package com.example.app.Repository

import com.example.app.Interfaces.MascotaApiService
import com.example.app.Model.Mascota
import com.example.app.Model.Usuario
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class MascotaRepository @Inject constructor(
    private val api: MascotaApiService
) {

    suspend fun obtenerTodos(): List<Mascota> {
        return api.obtenerMascotas()
    }

    suspend fun obtenerPorId(id: Long): Mascota {
        return api.obtenerMascota(id)
    }

    suspend fun guardar(mascota: Mascota): Mascota {
        val payloadConId = mascota.copy(usuario = mascota.usuario?.id?.let { Usuario(id = it) })
        return runCatching { api.guardarMascota(payloadConId) }
            .recoverCatching { api.guardarMascotaBase(payloadConId) }
            .recoverCatching {
                // Fallback para backends que asignan usuario por JWT.
                api.guardarMascota(mascota.copy(usuario = null))
            }
            .recoverCatching { api.guardarMascotaBase(mascota.copy(usuario = null)) }
            .getOrThrow()
    }

    suspend fun guardarConFoto(mascota: Mascota, fotoFile: File): Mascota {
        val textPlain = "text/plain".toMediaTypeOrNull()
        val nombre = mascota.nombre.toRequestBody(textPlain)
        val tipo = mascota.tipo.toRequestBody(textPlain)
        val raza = mascota.raza.toRequestBody(textPlain)
        val usuarioId = mascota.usuario?.id?.toString()?.toRequestBody(textPlain)
        val fotoRequest = fotoFile.asRequestBody("image/*".toMediaTypeOrNull())
        val fotoPart = MultipartBody.Part.createFormData("foto", fotoFile.name, fotoRequest)
        val imagenPart = MultipartBody.Part.createFormData("imagen", fotoFile.name, fotoRequest)

        return try {
            api.guardarMascotaMultipart(
                nombre = nombre,
                tipo = tipo,
                raza = raza,
                usuarioId = usuarioId,
                foto = fotoPart
            )
        } catch (e: HttpException) {
            if (e.code() == 400 || e.code() == 404 || e.code() == 405) {
                runCatching {
                    api.guardarMascotaMultipartImagen(
                        nombre = nombre,
                        tipo = tipo,
                        raza = raza,
                        usuarioId = usuarioId,
                        imagen = imagenPart
                    )
                }.getOrElse {
                    // Último fallback: enviar sin usuarioId para backends que lo ignoran o lo rechazan.
                    api.guardarMascotaMultipartImagen(
                        nombre = nombre,
                        tipo = tipo,
                        raza = raza,
                        usuarioId = null,
                        imagen = imagenPart
                    )
                }.recoverCatching {
                    api.guardarMascotaMultipartBase(
                        nombre = nombre,
                        tipo = tipo,
                        raza = raza,
                        usuarioId = usuarioId,
                        foto = fotoPart
                    )
                }
            } else {
                throw e
            }
        }
    }

    suspend fun eliminar(id: Long) {
        api.eliminarMascota(id)
    }
}