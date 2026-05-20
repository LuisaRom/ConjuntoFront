package com.example.app.Interfaces

import com.example.app.Model.CheckoutAdministracionResponse
import com.example.app.Model.ConfirmarPagoRequest
import com.example.app.Model.ConfirmarPagoResponse
import com.example.app.Model.CrearPagoRequest
import com.example.app.Model.EstadoPagoAdmin
import com.example.app.Model.PagoAdministracion
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PagoAdministracionApiService {
    @GET("/api/pagos/mis-pagos")
    suspend fun obtenerMisPagos(): List<PagoAdministracion>

    @GET("/api/pagos/residente")
    suspend fun obtenerPagosResidente(): List<PagoAdministracion>

    @GET("/api/pagos/residente/pagos")
    suspend fun obtenerPagosResidenteAlias(): List<PagoAdministracion>

    @POST("/api/pagos/residente/pagos")
    suspend fun obtenerPagosResidentePost(): List<PagoAdministracion>

    @GET("/api/pagos/{id}")
    suspend fun obtenerPago(@Path("id") id: Long): PagoAdministracion

    @POST("/api/pagos/checkout/administracion")
    suspend fun crearCheckoutAdministracion(@Body request: CrearPagoRequest): CheckoutAdministracionResponse

    @POST("/api/pagos/crear")
    suspend fun crearPagoEnLinea(@Body request: CrearPagoRequest): CheckoutAdministracionResponse

    @POST("/api/pagos/checkout")
    suspend fun crearPagoEnLineaCheckout(@Body request: CrearPagoRequest): CheckoutAdministracionResponse

    @POST("/api/pagos/mercadopago")
    suspend fun crearPagoEnLineaMercadoPago(@Body request: CrearPagoRequest): CheckoutAdministracionResponse

    @POST("/api/pagos/confirmar")
    suspend fun confirmarPago(@Body request: ConfirmarPagoRequest): ConfirmarPagoResponse

    @GET("/api/pagos/admin/estado-pagos")
    suspend fun obtenerEstadoPagosAdmin(): List<EstadoPagoAdmin>

    @DELETE("/api/pagos/{id}")
    suspend fun eliminarPago(@Path("id") id: Long)
}
