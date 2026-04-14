package com.example.app

import com.example.app.Interfaces.RetrofitClient.RetrofitClient
import com.example.app.Interfaces.*
import com.example.app.Repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // API Services
    @Provides
    @Singleton
    fun provideUsuarioApiService(): UsuarioApiService {
        return RetrofitClient.usuarioApiService
    }

    @Provides
    @Singleton
    fun provideAccesoPeatonalApiService(): AccesoPeatonalApiService {
        return RetrofitClient.accesoPeatonalApiService
    }

    @Provides
    @Singleton
    fun provideAccesoAdminApiService(): AccesoAdminApiService {
        return RetrofitClient.accesoAdminApiService
    }

    @Provides
    @Singleton
    fun provideAccesoVehicularApiService(): AccesoVehicularApiService {
        return RetrofitClient.accesoVehicularApiService
    }

    @Provides
    @Singleton
    fun provideReservaZonaComunApiService(): ReservaZonaComunApiService {
        return RetrofitClient.reservaZonaComunApiService
    }

    @Provides
    @Singleton
    fun provideNotificacionApiService(): NotificacionApiService {
        return RetrofitClient.notificacionApiService
    }

    @Provides
    @Singleton
    fun providePaqueteriaApiService(): PaqueteriaApiService {
        return RetrofitClient.paqueteriaApiService
    }

    @Provides
    @Singleton
    fun provideQuejaApiService(): QuejaApiService {
        return RetrofitClient.quejaApiService
    }

    @Provides
    @Singleton
    fun provideMascotaApiService(): MascotaApiService {
        return RetrofitClient.mascotaApiService
    }

    @Provides
    @Singleton
    fun providePagoAdministracionApiService(): PagoAdministracionApiService {
        return RetrofitClient.pagoAdministracionApiService
    }

    @Provides
    @Singleton
    fun provideVisitanteApiService(): VisitanteApiService {
        return RetrofitClient.visitanteApiService
    }

    @Provides
    @Singleton
    fun provideVehiculoResidenteApiService(): VehiculoResidenteApiService {
        return RetrofitClient.vehiculoResidenteApiService
    }

    // Repositories
    @Provides
    @Singleton
    fun provideUsuarioRepository(): UsuarioRepository = UsuarioRepository()

    @Provides
    @Singleton
    fun provideAccesoPeatonalRepository(
        api: AccesoPeatonalApiService
    ): AccesoPeatonalRepository = AccesoPeatonalRepository(api)

    @Provides
    @Singleton
    fun provideAccesoVehicularRepository(
        api: AccesoVehicularApiService
    ): AccesoVehicularRepository = AccesoVehicularRepository(api)

    @Provides
    @Singleton
    fun provideAccesoAdminRepository(
        api: AccesoAdminApiService
    ): AccesoAdminRepository = AccesoAdminRepository(api)

    @Provides
    @Singleton
    fun provideReservaZonaComunRepository(
        api: ReservaZonaComunApiService
    ): ReservaZonaComunRepository = ReservaZonaComunRepository(api)

    @Provides
    @Singleton
    fun provideNotificacionRepository(
        api: NotificacionApiService
    ): NotificacionRepository = NotificacionRepository(api)

    @Provides
    @Singleton
    fun providePaqueteriaRepository(
        api: PaqueteriaApiService
    ): PaqueteriaRepository = PaqueteriaRepository(api)

    @Provides
    @Singleton
    fun provideQuejaRepository(
        api: QuejaApiService
    ): QuejaRepository = QuejaRepository(api)

    @Provides
    @Singleton
    fun provideMascotaRepository(
        api: MascotaApiService
    ): MascotaRepository = MascotaRepository(api)

    @Provides
    @Singleton
    fun providePagoAdministracionRepository(
        api: PagoAdministracionApiService
    ): PagoAdministracionRepository = PagoAdministracionRepository(api)

    @Provides
    @Singleton
    fun provideVisitanteRepository(
        api: VisitanteApiService
    ): VisitanteRepository = VisitanteRepository(api)

    @Provides
    @Singleton
    fun provideVehiculoResidenteRepository(
        api: VehiculoResidenteApiService
    ): VehiculoResidenteRepository = VehiculoResidenteRepository(api)
}