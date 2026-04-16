package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material3.FilterChip
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
    var filtroTipo by remember { mutableStateOf("Todas") }
    var reservaSeleccionada by remember { mutableStateOf<ReservaZonaComun?>(null) }

    LaunchedEffect(Unit) {
        reservaZonaComunViewModel.obtenerTodos()
    }

    val tiposDisponibles = remember(reservas) {
        listOf("Todas") + reservas
            .map { normalizarTipoReserva(it.zonaComun) }
            .distinct()
            .sorted()
    }

    val reservasFiltradas = remember(reservas, filtroTipo) {
        val base = if (filtroTipo == "Todas") {
            reservas
        } else {
            reservas.filter { normalizarTipoReserva(it.zonaComun) == filtroTipo }
        }
        base.sortedWith(
            compareByDescending<ReservaZonaComun> { it.fechaReserva }
                .thenByDescending { it.horaInicio }
        )
    }

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
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            maxItemsInEachRow = 3
        ) {
            tiposDisponibles.forEach { tipo ->
                FilterChip(
                    selected = filtroTipo == tipo,
                    onClick = { filtroTipo = tipo },
                    label = { Text(tipo) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }

        if (isLoading && reservas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else if (reservasFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay reservas para el filtro seleccionado.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = reservasFiltradas,
                    key = { it.id ?: it.hashCode().toLong() }
                ) { reserva ->
                    ReservaCardSoloLectura(
                        reserva = reserva,
                        icono = iconoPorTipo(reserva.zonaComun),
                        onClick = { reservaSeleccionada = reserva }
                    )
                }
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
private fun ReservaCardSoloLectura(
    reserva: ReservaZonaComun,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = reserva.zonaComun,
                    tint = DoradoElegante,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = normalizarTipoReserva(reserva.zonaComun),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Fecha: ${reserva.fechaReserva}",
                color = GrisClaro,
                fontSize = 13.sp
            )
            Text(
                text = "Horario: ${reserva.horaInicio} - ${reserva.horaFin}",
                color = GrisClaro,
                fontSize = 13.sp
            )
            Text(
                text = "Usuario: ${reserva.usuario?.nombre ?: reserva.usuario?.usuario ?: "Sin usuario"}",
                color = GrisClaro,
                fontSize = 13.sp
            )
            Text(
                text = "Torre/Apto: ${reserva.usuario?.torre ?: "-"} / ${reserva.usuario?.apartamento ?: "-"}",
                color = GrisClaro,
                fontSize = 13.sp
            )
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