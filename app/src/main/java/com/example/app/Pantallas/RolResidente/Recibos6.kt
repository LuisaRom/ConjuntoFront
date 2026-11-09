package com.example.app.Pantallas.RolResidente

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Notificacion
import com.example.app.R
import com.example.app.ViewModel.NotificacionViewModel
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
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val notificacionesRecibos by notificacionViewModel.misRecibos.collectAsState()
    val isLoading by notificacionViewModel.cargandoMisRecibos.collectAsState()
    val error by notificacionViewModel.error.collectAsState()

    // Carga al entrar y refresca cada 5 segundos para detectar recibos nuevos
    LaunchedEffect(Unit) {
        notificacionViewModel.cargarMisRecibos()
        while (true) {
            delay(5000)
            notificacionViewModel.cargarMisRecibos(silencioso = true)
        }
    }

    val notificacionesEnel = notificacionesRecibos.filter { tipoReciboDeNotificacion(it) == "ENEL" }
    val notificacionesVanti = notificacionesRecibos.filter { tipoReciboDeNotificacion(it) == "VANTI" }
    val notificacionesEpz = notificacionesRecibos.filter { tipoReciboDeNotificacion(it) == "EPZ" }

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

        if (isLoading && notificacionesRecibos.isEmpty()) {
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
                    if (!error.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "",
                            color = Color(0xFFFFB4AB),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notificacionesEnel) { notificacion ->
                    ReciboNotificacionItem(
                        logoResId = R.drawable.logoenel,
                        nombreRecibo = "ENEL",
                        notificacion = notificacion
                    )
                }
                items(notificacionesVanti) { notificacion ->
                    ReciboNotificacionItem(
                        logoResId = R.drawable.logovanti,
                        nombreRecibo = "VANTI",
                        notificacion = notificacion
                    )
                }
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

private fun tipoReciboDeNotificacion(notificacion: Notificacion): String? {
    notificacion.tipoRecibo?.uppercase()?.let { return it }
    val mensaje = notificacion.mensaje?.uppercase().orEmpty()
    return when {
        mensaje.contains("ENEL") -> "ENEL"
        mensaje.contains("VANTI") -> "VANTI"
        mensaje.contains("EPZ") -> "EPZ"
        else -> null
    }
}

// Formatea la fecha del recibo (ignora fracciones de segundo y zona horaria)
fun formatearFechaRecibo(fechaISO: String?): String {
    if (fechaISO.isNullOrBlank()) return "Fecha no disponible"
    return try {
        val base = fechaISO.take(19) // yyyy-MM-ddTHH:mm:ss
        val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(base)
        if (fecha != null) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(fecha)
        } else {
            fechaISO
        }
    } catch (e: Exception) {
        fechaISO
    }
}