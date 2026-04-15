package com.example.app.Pantallas.RolResidente

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.R
import androidx.navigation.NavController
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.app.Model.Notificacion
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

val ColorScheme.doradoElegante: Color
    @Composable
    get() = Color(0xFFD4AF37)

@Composable
fun PantallaRecibos(
    navController: NavController,
    notificacionViewModel: NotificacionViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val notificaciones by notificacionViewModel.notificaciones.collectAsState()
    val isLoading by notificacionViewModel.isLoading.collectAsState()

    // Cargar notificaciones siempre que se entre a la pantalla
    LaunchedEffect(Unit) {
        Log.d("Recibos", "Cargando notificaciones... Usuario actual: ${usuarioActual?.nombre} (ID: ${usuarioActual?.id})")
        // Cargar notificaciones
        notificacionViewModel.obtenerTodos()
        // Si no hay usuario actual, intentar obtenerlo desde los usuarios existentes
        if (usuarioActual == null) {
            Log.w("Recibos", "Usuario actual es null, intentando obtenerlo desde la lista de usuarios...")
            usuarioViewModel.obtenerTodos()
        }
    }

    // Refrescar notificaciones cuando cambia el usuario actual
    LaunchedEffect(usuarioActual?.id) {
        if (usuarioActual?.id != null) {
            Log.d("Recibos", "Usuario actual actualizado, refrescando notificaciones. ID: ${usuarioActual?.id}, Nombre: ${usuarioActual?.nombre}")
            notificacionViewModel.obtenerTodos()
        } else {
            Log.w("Recibos", "ADVERTENCIA: Usuario actual es null")
        }
    }
    
    // Refrescar notificaciones periódicamente para detectar nuevas
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Refrescar cada 3 segundos
            if (usuarioActual?.id != null) {
                Log.d("Recibos", "Refrescando notificaciones periódicamente...")
                notificacionViewModel.obtenerTodos()
            }
        }
    }
    
    // Refrescar notificaciones cuando cambia la lista de notificaciones (para detectar nuevas)
    LaunchedEffect(notificaciones.size) {
        if (usuarioActual?.id != null) {
            Log.d("Recibos", "Lista de notificaciones cambió (${notificaciones.size} totales), verificando recibos...")
        }
    }

    // Inferir usuario actual desde las notificaciones de recibos si es null
    val usuarioIdEfectivo = remember(notificaciones, usuarioActual?.id) {
        if (usuarioActual?.id != null) {
            usuarioActual?.id
        } else {
            // Intentar inferir desde las notificaciones de recibos
            val recibos = notificaciones.filter { esNotificacionRecibo(it.mensaje) && it.usuario?.id != null }
            val userIds = recibos.mapNotNull { it.usuario?.id }
            val userIdMasComun = userIds.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            if (userIdMasComun != null) {
                Log.d("Recibos", "Usuario inferido desde notificaciones de recibos: ID=$userIdMasComun")
            }
            userIdMasComun
        }
    }
    
    // Filtrar notificaciones de recibos para el usuario actual (o inferido)
    val notificacionesRecibos = remember(notificaciones, usuarioIdEfectivo) {
        Log.d("Recibos", "=== FILTRANDO NOTIFICACIONES DE RECIBOS ===")
        Log.d("Recibos", "Total notificaciones: ${notificaciones.size}")
        Log.d("Recibos", "Usuario actual ID: ${usuarioActual?.id}, Nombre: ${usuarioActual?.nombre}")
        Log.d("Recibos", "Usuario efectivo ID: $usuarioIdEfectivo")
        
        val filtradas = notificaciones.filter { notificacion ->
            val esRecibo = esNotificacionRecibo(notificacion.mensaje)
            val esParaUsuario = if (usuarioIdEfectivo != null) {
                notificacion.usuario?.id == usuarioIdEfectivo
            } else {
                false
            }
            
            Log.d("Recibos", "Notificación ID=${notificacion.id}")
            Log.d("Recibos", "  - Mensaje: ${notificacion.mensaje}")
            Log.d("Recibos", "  - Usuario ID: ${notificacion.usuario?.id}, Nombre: ${notificacion.usuario?.nombre}")
            Log.d("Recibos", "  - Es recibo: $esRecibo")
            Log.d("Recibos", "  - Es para usuario: $esParaUsuario")
            Log.d("Recibos", "  - Incluir: ${esRecibo && esParaUsuario}")
            
            esRecibo && esParaUsuario
        }
        
        Log.d("Recibos", "Notificaciones de recibos filtradas: ${filtradas.size}")
        filtradas
    }

    // Agrupar notificaciones por tipo de recibo
    val notificacionesEnel = notificacionesRecibos.filter { it.mensaje?.contains("ENEL", ignoreCase = true) == true }
    val notificacionesVanti = notificacionesRecibos.filter { it.mensaje?.contains("VANTI", ignoreCase = true) == true }
    val notificacionesEpz = notificacionesRecibos.filter { it.mensaje?.contains("EPZ", ignoreCase = true) == true }

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
            Text(
                "Recibos Disponibles",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else if (notificacionesRecibos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No tienes recibos disponibles",
                        color = GrisClaro,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Los recibos aparecerán aquí cuando lleguen a la portería",
                        color = GrisClaro,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                // Mostrar recibos ENEL
                if (notificacionesEnel.isNotEmpty()) {
                    items(notificacionesEnel) { notificacion ->
                        ReciboNotificacionItem(
                            logoResId = R.drawable.logoenel,
                            nombreRecibo = "ENEL",
                            notificacion = notificacion
                        )
                    }
                }
                
                // Mostrar recibos VANTI
                if (notificacionesVanti.isNotEmpty()) {
                    items(notificacionesVanti) { notificacion ->
                        ReciboNotificacionItem(
                            logoResId = R.drawable.logovanti,
                            nombreRecibo = "VANTI",
                            notificacion = notificacion
                        )
                    }
                }
                
                // Mostrar recibos EPZ
                if (notificacionesEpz.isNotEmpty()) {
                    items(notificacionesEpz) { notificacion ->
                        ReciboNotificacionItem(
                            logoResId = R.drawable.logoepz,
                            nombreRecibo = "EPZ",
                            notificacion = notificacion
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReciboNotificacionItem(
    logoResId: Int,
    nombreRecibo: String,
    notificacion: Notificacion
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AzulOscuro.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = nombreRecibo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    // Badge "NUEVO" destacado
                    Box(
                        modifier = Modifier
                            .background(Color.Green, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NUEVO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Mensaje de alerta destacado
                Text(
                    text = "⚠️ ${notificacion.mensaje ?: "Recibo disponible"}",
                    color = DoradoElegante,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fecha: ${formatearFechaRecibo(notificacion.fechaEnvio)}",
                    color = GrisClaro,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Indicador grande verde destacado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green, CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓",
                    color = Color.Green,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Función para identificar notificaciones de recibos
fun esNotificacionRecibo(mensaje: String?): Boolean {
    if (mensaje.isNullOrBlank()) {
        Log.d("Recibos", "Mensaje es null o blank")
        return false
    }
    val mensajeLower = mensaje.lowercase()
    
    // Verificar que contenga "recibo" y uno de los tipos de recibo (ENEL, VANTI, EPZ)
    val tieneRecibo = mensajeLower.contains("recibo")
    val tieneEnel = mensajeLower.contains("enel")
    val tieneVanti = mensajeLower.contains("vanti")
    val tieneEpz = mensajeLower.contains("epz")
    val tieneTipoRecibo = tieneEnel || tieneVanti || tieneEpz
    
    // El mensaje debe contener "recibo" y alguno de los tipos
    val esRecibo = tieneRecibo && tieneTipoRecibo
    
    Log.d("Recibos", "Verificando si es recibo - Mensaje completo: $mensaje")
    Log.d("Recibos", "  - Mensaje lowercase: $mensajeLower")
    Log.d("Recibos", "  - Tiene 'recibo': $tieneRecibo")
    Log.d("Recibos", "  - Tiene 'enel': $tieneEnel")
    Log.d("Recibos", "  - Tiene 'vanti': $tieneVanti")
    Log.d("Recibos", "  - Tiene 'epz': $tieneEpz")
    Log.d("Recibos", "  - Tiene tipo recibo (ENEL/VANTI/EPZ): $tieneTipoRecibo")
    Log.d("Recibos", "  - Es recibo: $esRecibo")
    
    return esRecibo
}

// Función para formatear la fecha del recibo
fun formatearFechaRecibo(fechaISO: String?): String {
    if (fechaISO.isNullOrBlank()) return "Fecha no disponible"
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