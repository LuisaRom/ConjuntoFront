package com.example.app.Interfaces.RetrofitClient

import com.example.app.Interfaces.AccesoPeatonalApiService
import com.example.app.Interfaces.AccesoVehicularApiService
import com.example.app.Interfaces.MascotaApiService
import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Interfaces.PaqueteriaApiService
import com.example.app.Interfaces.QuejaApiService
import com.example.app.Interfaces.ReservaZonaComunApiService
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Interfaces.VehiculoResidenteApiService
import com.example.app.Interfaces.VisitanteApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://conjuntoback.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Servicios de API
    val usuarioApiService: UsuarioApiService = retrofit.create(UsuarioApiService::class.java)

    // Servicios de acceso
    val accesoPeatonalApiService: AccesoPeatonalApiService = retrofit.create(AccesoPeatonalApiService::class.java)
    val accesoVehicularApiService: AccesoVehicularApiService = retrofit.create(AccesoVehicularApiService::class.java)
    val visitanteApiService: VisitanteApiService = retrofit.create(VisitanteApiService::class.java)
    val vehiculoResidenteApiService: VehiculoResidenteApiService = retrofit.create(VehiculoResidenteApiService::class.java)
    val mascotaApiService: MascotaApiService = retrofit.create(MascotaApiService::class.java)

    // Servicios de gestión y notificaciones
    val notificacionApiService: NotificacionApiService = retrofit.create(NotificacionApiService::class.java)
    val pagoAdministracionApiService: PagoAdministracionApiService = retrofit.create(PagoAdministracionApiService::class.java)
    val paqueteriaApiService: PaqueteriaApiService = retrofit.create(PaqueteriaApiService::class.java)
    val quejaApiService: QuejaApiService = retrofit.create(QuejaApiService::class.java)
    val reservaZonaComunApiService: ReservaZonaComunApiService = retrofit.create(ReservaZonaComunApiService::class.java)
}