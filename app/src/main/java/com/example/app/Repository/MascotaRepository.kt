package com.example.app.Repository

import com.example.app.Interfaces.MascotaApiService
import com.example.app.Model.Mascota
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
        // El backend obtiene el usuario autenticado desde el token.
        return api.guardarMascota(mascota.copy(usuario = null))
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
            if (e.code() == 400) {
                api.guardarMascotaMultipartImagen(
                    nombre = nombre,
                    tipo = tipo,
                    raza = raza,
                    usuarioId = usuarioId,
                    imagen = imagenPart
                )
            } else {
                throw e
            }
        }
    }

    suspend fun eliminar(id: Long) {
        api.eliminarMascota(id)
    }
}