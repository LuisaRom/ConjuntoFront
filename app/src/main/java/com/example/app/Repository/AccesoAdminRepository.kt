package com.example.app.Repository

import com.example.app.Interfaces.AccesoAdminApiService
import com.example.app.Model.AccesoAdmin
import javax.inject.Inject

class AccesoAdminRepository @Inject constructor(
    private val api: AccesoAdminApiService
) {
    suspend fun obtenerTodos(): List<AccesoAdmin> = api.obtenerTodos()

    suspend fun eliminar(id: Long) = api.eliminar(id)
}
