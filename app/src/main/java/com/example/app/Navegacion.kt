package com.example.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
// ⛔️ NO usar androidx.navigation.Navigation en Compose
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.Auth.AuthManager

import com.example.app.Pantallas.PantallaLogin
import com.example.app.Pantallas.PantallaSeleccionRol
import com.example.app.Pantallas.RolAdministrador.PantallaAccesoPeatonalDetalle
import com.example.app.Pantallas.RolAdministrador.PantallaAccesoVehicularDetalle
import com.example.app.Pantallas.RolAdministrador.PantallaAccesos
import com.example.app.Pantallas.RolAdministrador.PantallaCreacionPublicacionAdmin
import com.example.app.Pantallas.RolAdministrador.PantallaCrearUsuario
import com.example.app.Pantallas.RolAdministrador.PantallaDashboardAdmin
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleQuejas
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleReservaPiscina
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleReservaSalonComunal
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleReservaZonaBBQ
import com.example.app.Pantallas.RolAdministrador.PantallaInicioAdmin
import com.example.app.Pantallas.RolAdministrador.PantallaMascotas
import com.example.app.Pantallas.RolAdministrador.PantallaMensajes
import com.example.app.Pantallas.RolAdministrador.PantallaMenu
import com.example.app.Pantallas.RolAdministrador.PantallaNotificaciones
import com.example.app.Pantallas.RolAdministrador.PantallaPerfil
import com.example.app.Pantallas.RolAdministrador.PantallaQuejas
import com.example.app.Pantallas.RolAdministrador.PantallaReservas

import com.example.app.Pantallas.RolCelador.PantallaAccesoPeatonalCelador
import com.example.app.Pantallas.RolCelador.PantallaAccesoVehicularCelador
import com.example.app.Pantallas.RolCelador.PantallaAccesosCelador
import com.example.app.Pantallas.RolCelador.PantallaDashboardCelador
import com.example.app.Pantallas.RolCelador.PantallaDetalleQuejasCelador
import com.example.app.Pantallas.RolCelador.PantallaDetallesPaqueteriaCelador
import com.example.app.Pantallas.RolCelador.PantallaMascotasCelador
import com.example.app.Pantallas.RolCelador.PantallaMenuCelador
import com.example.app.Pantallas.RolCelador.PantallaMensajesCelador
import com.example.app.Pantallas.RolCelador.PantallaNotificacionesCelador
import com.example.app.Pantallas.RolCelador.PantallaPaqueteriaCelador
import com.example.app.Pantallas.RolCelador.PantallaPerfilCelador
import com.example.app.Pantallas.RolCelador.PantallaQuejasCelador
import com.example.app.Pantallas.RolCelador.PantallaRecibosCelador
import com.example.app.Pantallas.RolCelador.PantallaReservaPiscinaCelador
import com.example.app.Pantallas.RolCelador.PantallaReservaSalonComunalCelador
import com.example.app.Pantallas.RolCelador.PantallaReservaZonaBBQCelador
import com.example.app.Pantallas.RolCelador.PantallaReservasCelador

import com.example.app.Pantallas.RolResidente.PantallaAccesoPeatonalResidente
import com.example.app.Pantallas.RolResidente.PantallaAccesoVehicularResidente
import com.example.app.Pantallas.RolResidente.PantallaAccesosResidente
import com.example.app.Pantallas.RolResidente.PantallaInicioResidentes
import com.example.app.Pantallas.RolResidente.PantallaMascotasResidente
import com.example.app.Pantallas.RolResidente.PantallaMenuResidente
import com.example.app.Pantallas.RolResidente.PantallaNotificacionesResidente
import com.example.app.Pantallas.RolResidente.PantallaNuevaPublicacion
import com.example.app.Pantallas.RolResidente.PantallaPagos
import com.example.app.Pantallas.RolResidente.PantallaPerfilResidente
import com.example.app.Pantallas.RolResidente.PantallaQuejasResidente
import com.example.app.Pantallas.RolResidente.PantallaRecibos
import com.example.app.Pantallas.RolResidente.PantallaReservaGimnasio
import com.example.app.Pantallas.RolResidente.PantallaReservaPiscina
import com.example.app.Pantallas.RolResidente.PantallaReservaSalonComunal
import com.example.app.Pantallas.RolResidente.PantallaReservaZonaBBQ
import com.example.app.Pantallas.RolResidente.PantallaReservasResidente

import com.example.app.ViewModel.UsuarioViewModel

private fun homeRouteForRole(rol: String): String {
    return when (rol.uppercase()) {
        "ADMINISTRADOR" -> "PantallaInicioAdmin"
        "CELADOR" -> "PantallaDashboardCelador"
        "RESIDENTE" -> "PantallaInicioResidentes"
        else -> "PantallaSeleccionRol"
    }
}

@Composable
private fun RoleGuard(
    navController: NavHostController,
    allowedRoles: Set<String>,
    content: @Composable () -> Unit
) {
    val user by AuthManager.currentUser.collectAsState()
    val role = user?.rol?.uppercase()
    val isAllowed = role != null && role in allowedRoles

    LaunchedEffect(role) {
        if (user == null) {
            navController.navigate("PantallaLogin") {
                popUpTo("PantallaSeleccionRol") { inclusive = false }
                launchSingleTop = true
            }
        } else if (!isAllowed) {
            AuthManager.handleForbidden()
            navController.navigate(homeRouteForRole(user?.rol.orEmpty())) {
                launchSingleTop = true
            }
        }
    }

    if (isAllowed) {
        content()
    }
}

@Composable
fun Navegacion(navController: NavHostController) {
    // Obtenemos el VM via Hilt dentro de un contexto @Composable
    val usuarioViewModel: UsuarioViewModel = hiltViewModel()
    val currentUser by AuthManager.currentUser.collectAsState()

    val startDestination = if (currentUser == null) {
        "PantallaSeleccionRol"
    } else {
        homeRouteForRole(currentUser?.rol.orEmpty())
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- Comunes ---
        composable("PantallaSeleccionRol") {
            // Si tu firma de PantallaSeleccionRol SOLO recibe navController, quita el VM
            // PantallaSeleccionRol(navController)
            PantallaSeleccionRol(navController, usuarioViewModel)
        }
        composable("PantallaLogin") {
            PantallaLogin(navController, usuarioViewModel)
        }

        // --- ADMIN ---
        composable("PantallaInicioAdmin") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaInicioAdmin(navController) }
        }
        composable("PantallaCrearUsuario") { 
            RoleGuard(navController, setOf("ADMINISTRADOR")) {
                PantallaCrearUsuario(navController, usuarioViewModel)
            }
        }
        composable("PantallaDashboardAdmin") { 
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaDashboardAdmin(navController) }
        }
        composable("PantallaNotificaciones") { 
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaNotificaciones(navController) }
        }
        composable("PantallaCreacionPublicacionAdmin") { 
            RoleGuard(navController, setOf("ADMINISTRADOR")) {
                PantallaCreacionPublicacionAdmin(navController, usuarioViewModel)
            }
        }
        composable("PantallaMensajes/{nombre}") { backStackEntry ->
            RoleGuard(navController, setOf("ADMINISTRADOR", "CELADOR")) {
                val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                PantallaMensajes(nombre = nombre, navController = navController)
            }
        }
        composable("PantallaMenu") { RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaMenu(navController) } }
        composable("PantallaAccesos") { RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaAccesos(navController) } }
        composable("PantallaAccesoVehicularDetalle") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaAccesoVehicularDetalle(navController) }
        }
        composable("PantallaAccesoPeatonalDetalle") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaAccesoPeatonalDetalle(navController) }
        }
        composable("PantallaReservas") { RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaReservas(navController) } }
        composable("PantallaDetalleReservaPiscina") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaDetalleReservaPiscina(navController) }
        }
        composable("PantallaDetalleReservaSalonComunal") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaDetalleReservaSalonComunal(navController) }
        }
        composable("PantallaDetalleReservaZonaBBQ") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaDetalleReservaZonaBBQ(navController) }
        }
        composable("PantallaQuejas") { RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaQuejas(navController) } }
        composable("PantallaDetalleQuejas") {
            RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaDetalleQuejas(navController) }
        }
        composable("PantallaMascotas") { RoleGuard(navController, setOf("ADMINISTRADOR")) { PantallaMascotas(navController) } }
        composable("PantallaPerfil") { 
            RoleGuard(navController, setOf("ADMINISTRADOR")) {
                PantallaPerfil(navController, usuarioViewModel)
            }
        }

        // --- RESIDENTE ---
        composable("PantallaInicioResidentes") {
            RoleGuard(navController, setOf("RESIDENTE")) { PantallaInicioResidentes(navController) }
        }
        composable("PantallaNuevaPublicacion") { 
            RoleGuard(navController, setOf("RESIDENTE")) {
                PantallaNuevaPublicacion(navController, usuarioViewModel)
            }
        }
        composable("PantallaNotificacionesResidente") {
            RoleGuard(navController, setOf("RESIDENTE")) { PantallaNotificacionesResidente(navController) }
        }
        composable("PantallaRecibos") {
            RoleGuard(navController, setOf("RESIDENTE")) { PantallaRecibos(navController) }
        }
        composable("PantallaPagos") { 
            RoleGuard(navController, setOf("RESIDENTE")) {
                PantallaPagos(navController, usuarioViewModel)
            }
        }
        composable("PantallaMenuResidente") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaMenuResidente(navController) } }
        composable("PantallaAccesosResidente") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaAccesosResidente(navController) } }
        composable("PantallaAccesoVehicularResidente") {
            RoleGuard(navController, setOf("RESIDENTE")) { PantallaAccesoVehicularResidente(navController) }
        }
        composable("PantallaAccesoPeatonalResidente") {
            RoleGuard(navController, setOf("RESIDENTE")) { PantallaAccesoPeatonalResidente(navController) }
        }
        composable("PantallaReservasResidente") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaReservasResidente(navController) } }
        composable("PantallaReservaPiscina") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaReservaPiscina(navController) } }
        composable("PantallaReservaSalonComunal") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaReservaSalonComunal(navController) } }
        composable("PantallaReservaGimnasio") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaReservaGimnasio(navController) } }
        composable("PantallaReservaZonaBBQ") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaReservaZonaBBQ(navController) } }
        composable("PantallaQuejasResidente") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaQuejasResidente(navController) } }
        composable("PantallaMascotasResidente") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaMascotasResidente(navController) } }
        composable("PantallaPerfilResidente") { RoleGuard(navController, setOf("RESIDENTE")) { PantallaPerfilResidente(navController) } }

        // --- CELADOR ---
        composable("PantallaDashboardCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaDashboardCelador(navController) } }
        composable("PantallaRecibosCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaRecibosCelador(navController) } }
        composable("PantallaMenuCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaMenuCelador(navController) } }
        composable("PantallaNotificacionesCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaNotificacionesCelador(navController) } }
        composable("PantallaMensajesCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaMensajesCelador(navController) } }
        composable("PantallaPaqueteriaCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaPaqueteriaCelador(navController) } }
        composable("PantallaDetallesPaqueteriaCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaDetallesPaqueteriaCelador(navController) } }
        composable("PantallaAccesosCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaAccesosCelador(navController) } }
        composable("PantallaAccesoVehicularCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaAccesoVehicularCelador(navController) } }
        composable("PantallaAccesoPeatonalCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaAccesoPeatonalCelador(navController) } }
        composable("PantallaReservasCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaReservasCelador(navController) } }
        composable("PantallaReservaPiscinaCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaReservaPiscinaCelador(navController) } }
        composable("PantallaReservaSalonComunalCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaReservaSalonComunalCelador(navController) } }
        composable("PantallaReservaZonaBBQCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaReservaZonaBBQCelador(navController) } }
        composable("PantallaQuejasCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaQuejasCelador(navController) } }
        composable("PantallaDetalleQuejasCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaDetalleQuejasCelador(navController) } }
        composable("PantallaMascotasCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaMascotasCelador(navController) } }
        composable("PantallaPerfilCelador") { RoleGuard(navController, setOf("CELADOR")) { PantallaPerfilCelador(navController) } }
    }
}
