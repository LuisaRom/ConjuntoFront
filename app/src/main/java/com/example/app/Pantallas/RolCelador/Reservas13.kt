package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.ReservaZonaComun
import com.example.app.ViewModel.ReservaZonaComunViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaReservasCelador(
    navController: NavController,
    reservaZonaComunViewModel: ReservaZonaComunViewModel = hiltViewModel()
) {
    val reservas by reservaZonaComunViewModel.reservas.collectAsState()
    val isLoading by reservaZonaComunViewModel.isLoading.collectAsState()
    val error by reservaZonaComunViewModel.error.collectAsState()
    var reservaSeleccionada by remember { mutableStateOf<ReservaZonaComun?>(null) }

    LaunchedEffect(Unit) {
        reservaZonaComunViewModel.obtenerTodos()
    }

    val reservasPiscina = reservas.filter { normalizarTipoReserva(it.zonaComun) == "Piscina" }
    val reservasSalon = reservas.filter { normalizarTipoReserva(it.zonaComun) == "Salón Comunal" }
    val reservasGimnasio = reservas.filter { normalizarTipoReserva(it.zonaComun) == "Gimnasio" }
    val reservasBbq = reservas.filter { normalizarTipoReserva(it.zonaComun) == "Zona BBQ" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = GrisClaro
                )
            }
            Text(
                text = "Reservas",
                style = MaterialTheme.typography.headlineMedium,
                color = GrisClaro,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

        if (isLoading && reservas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else if (reservas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay reservas registradas.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                ReservaCategoriaCelador(
                    titulo = "Piscina",
                    icono = Icons.Default.Pool,
                    reservas = reservasPiscina,
                    onClick = { reservaSeleccionada = it }
                )
                ReservaCategoriaCelador(
                    titulo = "Salón Comunal",
                    icono = Icons.Default.Home,
                    reservas = reservasSalon,
                    onClick = { reservaSeleccionada = it }
                )
                ReservaCategoriaCelador(
                    titulo = "Gimnasio",
                    icono = Icons.Default.FitnessCenter,
                    reservas = reservasGimnasio,
                    onClick = { reservaSeleccionada = it }
                )
                ReservaCategoriaCelador(
                    titulo = "Zona BBQ",
                    icono = Icons.Default.OutdoorGrill,
                    reservas = reservasBbq,
                    onClick = { reservaSeleccionada = it }
                )
            }
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    reservaSeleccionada?.let { reserva ->
        AlertDialog(
            onDismissRequest = { reservaSeleccionada = null },
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                    Text("Detalle Reserva", color = Color.White)
                    Text("X", color = DoradoElegante, modifier = Modifier.clickable { reservaSeleccionada = null })
                }
            },
            text = {
                Column {
                    Text("Zona: ${normalizarTipoReserva(reserva.zonaComun)}", color = Color.White)
                    Text("Fecha: ${reserva.fechaReserva}", color = Color.White)
                    Text("Horario: ${reserva.horaInicio} - ${reserva.horaFin}", color = Color.White)
                    Text("Usuario: ${reserva.usuario?.nombre ?: reserva.usuario?.usuario ?: "-"}", color = Color.White)
                    Text("Torre/Apt: ${reserva.usuario?.torre ?: "-"} - ${reserva.usuario?.apartamento ?: "-"}", color = Color.White)
                    if (!reserva.serviciosAdicionales.isNullOrBlank()) {
                        Text("Servicios: ${reserva.serviciosAdicionales}", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { reservaSeleccionada = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
                ) {
                    Text("Cerrar", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AzulOscuro
        )
    }
}

@Composable
private fun ReservaCategoriaCelador(
    titulo: String,
    icono: ImageVector,
    reservas: List<ReservaZonaComun>,
    onClick: (ReservaZonaComun) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = DoradoElegante,
                modifier = Modifier.size(35.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "${reservas.size} Reservas",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 43.dp, top = 2.dp)
        )

        if (reservas.isEmpty()) {
            Text(
                text = "No hay reservas creadas",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 43.dp, top = 6.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(reservas, key = { it.id ?: it.hashCode().toLong() }) { reserva ->
                val nombreUsuario = reserva.usuario?.nombre ?: reserva.usuario?.usuario ?: "Sin usuario"
                val torre = reserva.usuario?.torre ?: "-"
                val apto = reserva.usuario?.apartamento ?: "-"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .clickable { onClick(reserva) }
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(nombreUsuario, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Torre $torre - Apt $apto", color = GrisClaro, fontSize = 12.sp)
                        Text("Fecha: ${reserva.fechaReserva} | ${reserva.horaInicio} - ${reserva.horaFin}", color = GrisClaro, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun normalizarTipoReserva(zona: String): String {
    return when (zona.trim().lowercase()) {
        "piscina" -> "Piscina"
        "salon comunal", "salón comunal" -> "Salón Comunal"
        "gimnasio" -> "Gimnasio"
        "zona bbq", "bbq" -> "Zona BBQ"
        else -> zona.ifBlank { "Sin tipo" }
    }
}

private fun iconoPorTipo(zona: String): ImageVector {
    return when (normalizarTipoReserva(zona)) {
        "Piscina" -> Icons.Default.Pool
        "Salón Comunal" -> Icons.Default.Home
        "Gimnasio" -> Icons.Default.FitnessCenter
        "Zona BBQ" -> Icons.Default.OutdoorGrill
        else -> Icons.Default.Home
    }
}