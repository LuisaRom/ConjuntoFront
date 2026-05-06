package com.example.app.Interfaces

import com.example.app.Model.PagoAdministracion
import com.example.app.Model.CheckoutAdministracionResponse
import com.example.app.Model.CrearPagoRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PagoAdministracionApiService {
    @GET("/api/pagos")
    suspend fun obtenerPagos(): List<PagoAdministracion>

    @GET("/api/pagos/{id}")
    suspend fun obtenerPago(@Path("id") id: Long): PagoAdministracion

    @POST("/api/pagos")
    suspend fun guardarPago(@Body pago: PagoAdministracion): PagoAdministracion

    @POST("/api/pagos/crear")
    suspend fun crearPagoEnLinea(@Body request: CrearPagoRequest): CheckoutAdministracionResponse

    @DELETE("/api/pagos/{id}")
    suspend fun eliminarPago(@Path("id") id: Long)
}
