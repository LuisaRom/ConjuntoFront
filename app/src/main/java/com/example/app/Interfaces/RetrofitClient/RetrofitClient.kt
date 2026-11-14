package com.example.app.Interfaces.RetrofitClient

import com.example.app.Auth.AuthManager
import com.example.app.Interfaces.AccesoPeatonalApiService
import com.example.app.Interfaces.AccesoAdminApiService
import com.example.app.Interfaces.AccesoVehicularApiService
import com.example.app.Interfaces.IaApiService
import com.example.app.Interfaces.MascotaApiService
import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Interfaces.PaqueteriaApiService
import com.example.app.Interfaces.QuejaApiService
import com.example.app.Interfaces.ReservaZonaComunApiService
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Interfaces.VehiculoResidenteApiService
import com.example.app.Interfaces.VisitanteApiService
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.google.gson.GsonBuilder
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"
    private const val BASE_URL = "https://conjuntoback.onrender.com/"

    private fun shouldSkipAuth(path: String): Boolean {
        return path.contains("/api/usuarios/login") ||
            path.contains("/swagger") ||
            path.contains("/v3/api-docs")
    }

    private fun shouldSkipForbiddenToast(path: String): Boolean {
        // Algunos endpoints se prueban con fallback entre rutas.
        // Evitamos mostrar toast de permisos en la primera ruta fallida.
        return path.contains("/api/usuarios/crear")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val path = original.url.encodedPath
            val token = AuthManager.getToken()

            val request: Request = if (
                token.isNullOrBlank() || shouldSkipAuth(path)
            ) {
                if (path.contains("/api/pagos/checkout/administracion")) {
                    Log.w(TAG, "POST /api/pagos/checkout/administracion SIN Authorization (token vacío o ruta exenta)")
                }
                original
            } else {
                if (path.contains("/api/pagos/checkout/administracion")) {
                    Log.d(TAG, "POST /api/pagos/checkout/administracion con Authorization: Bearer *** (len=${token.length})")
                }
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }

            val response = chain.proceed(request)
            if (path.contains("/api/pagos/checkout/administracion")) {
                Log.d(TAG, "POST /api/pagos/checkout/administracion -> HTTP ${response.code}")
            }
            when (response.code) {
                401 -> {
                    if (path.contains("/api/pagos/checkout/administracion")) {
                        Log.e(
                            TAG,
                            "401 en checkout/administracion. JWT expirado=${AuthManager.isTokenExpired()}"
                        )
                    }
                    AuthManager.handleUnauthorized()
                }
                403 -> if (!shouldSkipForbiddenToast(path)) AuthManager.handleForbidden()
            }
            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Servicios de API
    val usuarioApiService: UsuarioApiService = retrofit.create(UsuarioApiService::class.java)

    // Servicios de acceso
    val accesoAdminApiService: AccesoAdminApiService = retrofit.create(AccesoAdminApiService::class.java)
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
    val iaApiService: IaApiService = retrofit.create(IaApiService::class.java)
}