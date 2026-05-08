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
import java.util.Locale
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
        return try {
            api.guardarMascota(payloadConId)
        } catch (_: Exception) {
            try {
                api.guardarMascotaBase(payloadConId)
            } catch (_: Exception) {
                try {
                    // Fallback para backends que asignan usuario por JWT.
                    api.guardarMascota(mascota.copy(usuario = null))
                } catch (_: Exception) {
                    api.guardarMascotaBase(mascota.copy(usuario = null))
                }
            }
        }
    }

    suspend fun guardarConFoto(mascota: Mascota, fotoFile: File): Mascota {
        val textPlain = "text/plain".toMediaTypeOrNull()
        val nombre = mascota.nombre.toRequestBody(textPlain)
        val tipo = mascota.tipo.toRequestBody(textPlain)
        val raza = mascota.raza.toRequestBody(textPlain)
        val usuarioId = mascota.usuario?.id?.toString()?.toRequestBody(textPlain)
        val mimeFoto = when (fotoFile.extension.lowercase(Locale.getDefault())) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val fotoRequest = fotoFile.asRequestBody(mimeFoto.toMediaTypeOrNull())
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
            if (e.code() == 400 || e.code() == 404 || e.code() == 405 || e.code() == 415) {
                try {
                    api.guardarMascotaMultipartImagen(
                        nombre = nombre,
                        tipo = tipo,
                        raza = raza,
                        usuarioId = usuarioId,
                        imagen = imagenPart
                    )
                } catch (_: Exception) {
                    try {
                        // Fallback: enviar sin usuarioId para backends que lo ignoran o lo rechazan.
                        api.guardarMascotaMultipartImagen(
                            nombre = nombre,
                            tipo = tipo,
                            raza = raza,
                            usuarioId = null,
                            imagen = imagenPart
                        )
                    } catch (_: Exception) {
                        api.guardarMascotaMultipartBase(
                            nombre = nombre,
                            tipo = tipo,
                            raza = raza,
                            usuarioId = usuarioId,
                            foto = fotoPart
                        )
                    }
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