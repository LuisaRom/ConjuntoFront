package com.example.app.Repository

import com.example.app.Interfaces.QuejaApiService
import com.example.app.Model.Queja
import javax.inject.Inject

class QuejaRepository @Inject constructor(
    private val api: QuejaApiService
) {

    suspend fun obtenerTodos(): List<Queja> {
        return api.obtenerQuejas()
    }

    suspend fun obtenerPorCategoria(categoria: String): List<Queja> {
        return api.obtenerQuejasPorCategoria(categoria)
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
}