package com.example.app.Interfaces

import com.example.app.Model.PagoAdministracion
import com.example.app.Model.CheckoutAdministracionResponse
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

    @POST("/api/pagos/iniciar-pse")
    suspend fun iniciarPagoPse(): CheckoutAdministracionResponse

    @DELETE("/api/pagos/{id}")
    suspend fun eliminarPago(@Path("id") id: Long)
}
