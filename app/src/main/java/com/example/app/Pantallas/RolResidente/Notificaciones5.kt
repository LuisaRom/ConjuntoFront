package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.Model.Paqueteria
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.PaqueteriaViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PantallaNotificacionesResidente(
    navController: NavController,
    notificacionViewModel: NotificacionViewModel = hiltViewModel(),
    paqueteriaViewModel: PaqueteriaViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("Paqueteria") }
    
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val notificaciones by notificacionViewModel.notificaciones.collectAsState()
    val paquetes by paqueteriaViewModel.paquetes.collectAsState()
    val isLoadingNotificaciones by notificacionViewModel.isLoading.collectAsState()
    val isLoadingPaquetes by paqueteriaViewModel.isLoading.collectAsState()

    // Cargar notificaciones y paquetes siempre
    LaunchedEffect(Unit) {
        android.util.Log.d("NotificacionesResidente", "Cargando notificaciones y paquetes...")
        android.util.Log.d("NotificacionesResidente", "Usuario actual: ${usuarioActual?.nombre} (ID: ${usuarioActual?.id})")
        notificacionViewModel.obtenerTodos()
        paqueteriaViewModel.obtenerTodos()
    }
    
    // Cargar también cuando cambia el usuario actual
    LaunchedEffect(usuarioActual?.id) {
        if (usuarioActual?.id != null) {
            android.util.Log.d("NotificacionesResidente", "Usuario actualizado - Cargando notificaciones para usuario: id=${usuarioActual?.id}, nombre=${usuarioActual?.nombre}")
            notificacionViewModel.obtenerTodos()
            paqueteriaViewModel.obtenerTodos()
        } else {
            android.util.Log.w("NotificacionesResidente", "ADVERTENCIA: Usuario actual es null")
        }
    }

    // Obtener paquetes pendientes del usuario actual - PRIMERO obtenemos los paquetes para determinar el usuario
    val paquetesPendientes = remember(paquetes, usuarioActual?.id) {
        android.util.Log.d("NotificacionesResidente", "=== FILTRANDO PAQUETES PENDIENTES ===")
        android.util.Log.d("NotificacionesResidente", "Total paquetes: ${paquetes.size}")
        android.util.Log.d("NotificacionesResidente", "Usuario actual: ${usuarioActual?.nombre} (ID: ${usuarioActual?.id})")
        
        // Si no hay usuario actual, intentar inferirlo de los paquetes pendientes
        val usuarioIdDesdePaquetes = if (usuarioActual?.id == null && paquetes.isNotEmpty()) {
            // Obtener el ID del usuario más común en los paquetes pendientes
            val paquetesPendientesTodos = paquetes.filter { it.estado == "PENDIENTE" && it.usuario?.id != null }
            val userIds = paquetesPendientesTodos.mapNotNull { it.usuario?.id }
            val userIdMasComun = userIds.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            android.util.Log.d("NotificacionesResidente", "Usuario inferido desde paquetes: ID=$userIdMasComun")
            userIdMasComun
        } else {
            usuarioActual?.id
        }
        
        val filtrados = paquetes.filter { paquete ->
            val esParaUsuario = if (usuarioIdDesdePaquetes != null) {
                paquete.usuario?.id == usuarioIdDesdePaquetes
            } else {
                // Si no hay usuario, no mostrar ningún paquete
                false
            }
            val esPendiente = paquete.estado == "PENDIENTE"
            
            android.util.Log.d("NotificacionesResidente", "Paquete ID=${paquete.id}, Transportadora=${paquete.transportadora}, Usuario ID=${paquete.usuario?.id}, Estado=${paquete.estado}, Incluir: ${esParaUsuario && esPendiente}")
            
            esParaUsuario && esPendiente
        }
        
        android.util.Log.d("NotificacionesResidente", "Paquetes pendientes filtrados: ${filtrados.size}")
        filtrados
    }
    
    // Obtener el ID del usuario desde los paquetes pendientes si no hay usuario actual
    val usuarioIdEfectivo = usuarioActual?.id ?: paquetesPendientes.firstOrNull()?.usuario?.id
    
    // Filtrar notificaciones de paquetería para el usuario efectivo
    val notificacionesPaqueteria = remember(notificaciones, usuarioIdEfectivo) {
        android.util.Log.d("NotificacionesResidente", "=== FILTRANDO NOTIFICACIONES DE PAQUETERÍA ===")
        android.util.Log.d("NotificacionesResidente", "Total notificaciones: ${notificaciones.size}")
        android.util.Log.d("NotificacionesResidente", "Usuario efectivo ID: $usuarioIdEfectivo")
        
        val filtradas = notificaciones.filter { notificacion ->
            val esPaqueteria = esNotificacionPaqueteriaMensaje(notificacion.mensaje)
            val esParaUsuario = if (usuarioIdEfectivo != null) {
                notificacion.usuario?.id == usuarioIdEfectivo
            } else {
                false
            }
            
            android.util.Log.d("NotificacionesResidente", "Notificación ID=${notificacion.id}, mensaje=${notificacion.mensaje}, usuarioId=${notificacion.usuario?.id}, esPaqueteria=$esPaqueteria, esParaUsuario=$esParaUsuario")
            
            esParaUsuario && esPaqueteria
        }
        
        android.util.Log.d("NotificacionesResidente", "Notificaciones de paquetería filtradas: ${filtradas.size}")
        filtradas
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Notificaciones", color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Paqueteria", "Recibos", "Pagos").forEach { tab ->
                val isSelected = selectedTab == tab
                Button(
                    onClick = {
                        selectedTab = tab
                        when (tab) {
                            "Recibos" -> navController.navigate("PantallaRecibos")
                            "Pagos" -> navController.navigate("PantallaPagos")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) DoradoElegante else Color.Gray
                    )
                ) {
                    Text(tab, color = if (isSelected) AzulOscuro else Color.White)
                }
            }
        }

        if (selectedTab == "Paqueteria") {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Paquetes Pendientes (${paquetesPendientes.size})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingNotificaciones || isLoadingPaquetes) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (paquetesPendientes.isEmpty() && notificacionesPaqueteria.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No tienes paquetes pendientes",
                        color = GrisClaro,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Usuario actual: ${usuarioActual?.nombre} (ID: ${usuarioActual?.id})",
                        color = GrisClaro,
                        fontSize = 12.sp
                    )
                    Text(
                        "Total notificaciones: ${notificaciones.size}",
                        color = GrisClaro,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mostrar TODOS los paquetes pendientes, buscando su notificación relacionada
                    items(paquetesPendientes) { paquete ->
                        // Buscar la notificación relacionada por transportadora y usuario
                        val notificacionRelacionada = notificacionesPaqueteria.find { notificacion ->
                            val transportadoraEnMensaje = notificacion.mensaje?.contains(paquete.transportadora) == true
                            val mismoUsuario = notificacion.usuario?.id == paquete.usuario?.id
                            transportadoraEnMensaje && mismoUsuario
                        }
                        
                        PaqueteNotificacionItem(
                            paquete = paquete,
                            notificacion = notificacionRelacionada,
                            onConfirmar = {
                                scope.launch {
                                    try {
                                        paqueteriaViewModel.actualizarEstado(
                                            paquete.id!!,
                                            "ENTREGADO"
                                        )
                                        Toast.makeText(
                                            context,
                                            "Paquete confirmado como recibido",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Refrescar listas
                                        paqueteriaViewModel.obtenerTodos()
                                        notificacionViewModel.obtenerTodos()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error al confirmar paquete: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            isLoading = isLoadingPaquetes
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaqueteNotificacionItem(
    paquete: Paqueteria,
    notificacion: com.example.app.Model.Notificacion?,
    onConfirmar: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Paquete #${paquete.id}",
                color = DoradoElegante,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            EtiquetaCampo("Transportadora", paquete.transportadora)
            EtiquetaCampo("Fecha", formatearFecha(paquete.fechaRecepcion))
            
            notificacion?.mensaje?.let { mensaje ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensaje,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onConfirmar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Confirmar Recepción", color = AzulOscuro, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun formatearFecha(fechaISO: String?): String {
    if (fechaISO.isNullOrBlank()) return "No disponible"
    return try {
        val formatosEntrada = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        val fecha = formatosEntrada.firstNotNullOfOrNull { patron ->
            runCatching {
                SimpleDateFormat(patron, Locale.getDefault()).parse(fechaISO)
            }.getOrNull()
        } ?: return fechaISO
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(fecha)
    } catch (e: Exception) {
        fechaISO
    }
}

// Función helper para identificar notificaciones de paquetería por mensaje
fun esNotificacionPaqueteriaMensaje(mensaje: String?): Boolean {
    if (mensaje.isNullOrBlank()) return false
    val mensajeLower = mensaje.lowercase()
    return mensajeLower.contains("paquete") || 
           mensajeLower.contains("paquetería") || 
           mensajeLower.contains("transportadora") || 
           mensajeLower.contains("recoger") || 
           mensajeLower.contains("portería") ||
           mensajeLower.contains("disponible")
}

@Composable
fun EtiquetaCampo(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = GrisClaro, fontSize = 12.sp)
        Text(
            text = valor,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                .padding(12.dp)
        )
    }
}