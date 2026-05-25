package com.example.app.Repository

import com.example.app.Interfaces.MascotaApiService
import com.example.app.Model.Mascota
import com.example.app.Model.Usuario
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
                api.guardarMascotaCrear(payloadConId)
            } catch (_: Exception) {
                try {
                    api.guardarMascota(mascota.copy(usuario = null))
                } catch (_: Exception) {
                    api.guardarMascotaCrear(mascota.copy(usuario = null))
                }
            }
        }
    }

    suspend fun guardarConFoto(mascota: Mascota, fotoFile: File): Mascota {
        val descripcion = mascota.raza.substringBefore(" | Vacunación:").trim().ifBlank { null }

        fun crearPartes(): Pair<MultipartBody.Part, MultipartBody.Part> {
            val mimeFoto = when (fotoFile.extension.lowercase(Locale.getDefault())) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
            val fotoRequest = fotoFile.asRequestBody(mimeFoto.toMediaTypeOrNull())
            val fotoPart = MultipartBody.Part.createFormData("foto", fotoFile.name, fotoRequest)
            val imagenRequest = fotoFile.asRequestBody(mimeFoto.toMediaTypeOrNull())
            val imagenPart = MultipartBody.Part.createFormData("imagen", fotoFile.name, imagenRequest)
            return fotoPart to imagenPart
        }

        val intentos: List<suspend () -> Mascota> = listOf(
            {
                val (fotoPart, imagenPart) = crearPartes()
                api.guardarMascotaConFoto(
                    nombre = mascota.nombre,
                    tipo = mascota.tipo,
                    raza = mascota.raza,
                    descripcion = descripcion,
                    foto = fotoPart
                )
            },
            {
                val (fotoPart, imagenPart) = crearPartes()
                api.guardarMascotaConImagen(
                    nombre = mascota.nombre,
                    tipo = mascota.tipo,
                    raza = mascota.raza,
                    descripcion = descripcion,
                    imagen = imagenPart
                )
            },
            {
                val (fotoPart, _) = crearPartes()
                api.guardarMascotaConFotoCrear(
                    nombre = mascota.nombre,
                    tipo = mascota.tipo,
                    raza = mascota.raza,
                    descripcion = descripcion,
                    foto = fotoPart
                )
            },
            {
                val (_, imagenPart) = crearPartes()
                api.guardarMascotaConImagenCrear(
                    nombre = mascota.nombre,
                    tipo = mascota.tipo,
                    raza = mascota.raza,
                    descripcion = descripcion,
                    imagen = imagenPart
                )
            }
        )

        var ultimoError: Exception? = null
        for (intento in intentos) {
            try {
                return intento()
            } catch (e: HttpException) {
                ultimoError = e
                if (e.code() !in listOf(400, 404, 405, 415)) {
                    throw e
                }
            } catch (e: Exception) {
                ultimoError = e
            }
        }

        throw ultimoError ?: IllegalStateException("No se pudo subir la foto de la mascota")
    }

    suspend fun eliminar(id: Long) {
        api.eliminarMascota(id)
    }
}
