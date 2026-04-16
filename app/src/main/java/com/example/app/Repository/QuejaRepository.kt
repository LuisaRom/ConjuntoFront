package com.example.app.Repository

import com.example.app.Interfaces.QuejaApiService
import com.example.app.Model.Queja
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class QuejaRepository @Inject constructor(
    private val api: QuejaApiService
) {
    private val gson = Gson()

    suspend fun obtenerTodos(): List<Queja> {
        return runCatching { api.obtenerQuejas() }.getOrElse {
            val raw = api.obtenerQuejasRaw().string()
            parseQuejasFlexible(raw)
        }
    }

    suspend fun obtenerPorCategoria(categoria: String): List<Queja> {
        return runCatching { api.obtenerQuejasPorCategoria(categoria) }.getOrElse {
            val raw = api.obtenerQuejasPorCategoriaRaw(categoria).string()
            parseQuejasFlexible(raw)
        }
    }

    suspend fun obtenerPorId(id: Long): Queja {
        return api.obtenerQueja(id)
    }

    suspend fun guardar(queja: Queja): Queja {
        // El backend obtiene el usuario desde JWT, no desde payload.
        return api.guardarQueja(queja.copy(usuario = null))
    }

    suspend fun eliminar(id: Long) {
        api.eliminarQueja(id)
    }

    suspend fun finalizar(id: Long): Queja {
        return api.finalizarQueja(id)
    }

    private fun parseQuejasFlexible(raw: String): List<Queja> {
        if (raw.isBlank()) return emptyList()
        return try {
            val trimmed = raw.trim()
            when {
                trimmed.startsWith("[") -> {
                    val type = object : TypeToken<List<Queja>>() {}.type
                    gson.fromJson(trimmed, type) ?: emptyList()
                }
                trimmed.startsWith("{") -> {
                    val jsonObject = JSONObject(trimmed)
                    val array = when {
                        jsonObject.has("data") -> jsonObject.getJSONArray("data")
                        jsonObject.has("content") -> jsonObject.getJSONArray("content")
                        jsonObject.has("quejas") -> jsonObject.getJSONArray("quejas")
                        else -> JSONArray()
                    }
                    val type = object : TypeToken<List<Queja>>() {}.type
                    gson.fromJson(array.toString(), type) ?: emptyList()
                }
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}