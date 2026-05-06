package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun PantallaReservasCelador(
    navController: NavController,
    reservaZonaComunViewModel: ReservaZonaComunViewModel = hiltViewModel()
) {
    val reservas by reservaZonaComunViewModel.reservas.collectAsState()
    val isLoading by reservaZonaComunViewModel.isLoading.collectAsState()
    val error by reservaZonaComunViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        reservaZonaComunViewModel.obtenerTodos()
    }

    val reservasPiscina = reservas.filter { it.zonaComun.equals("Piscina", ignoreCase = true) }
    val reservasSalon = reservas.filter {
        it.zonaComun.equals("Salon Comunal", ignoreCase = true) ||
            it.zonaComun.equals("Salón Comunal", ignoreCase = true)
    }
    val reservasGimnasio = reservas.filter { it.zonaComun.equals("Gimnasio", ignoreCase = true) }
    val reservasBbq = reservas.filter {
        it.zonaComun.equals("Zona BBQ", ignoreCase = true) ||
            it.zonaComun.equals("BBQ", ignoreCase = true)
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                ReservaCategoriaCelador(
                    titulo = "Piscina",
                    icono = Icons.Default.Pool,
                    reservas = reservasPiscina
                )
                ReservaCategoriaCelador(
                    titulo = "Salón Comunal",
                    icono = Icons.Default.Home,
                    reservas = reservasSalon
                )
                ReservaCategoriaCelador(
                    titulo = "Gimnasio",
                    icono = Icons.Default.FitnessCenter,
                    reservas = reservasGimnasio
                )
                ReservaCategoriaCelador(
                    titulo = "Zona BBQ",
                    icono = Icons.Default.OutdoorGrill,
                    reservas = reservasBbq
                )

                error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservaCategoriaCelador(
    titulo: String,
    icono: ImageVector,
    reservas: List<ReservaZonaComun>
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                color = Color.White
            )
        }

        Text(
            text = "${reservas.size} Reservas",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 35.dp, top = 2.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (reservas.isEmpty()) {
            Text(
                text = "No hay reservas creadas",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 35.dp, top = 6.dp)
            )
        } else {
            reservas.forEach { reserva ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = reserva.zonaComun,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Fecha: ${reserva.fechaReserva} | ${reserva.horaInicio} - ${reserva.horaFin}",
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Usuario: ${reserva.usuario?.nombre ?: reserva.usuario?.usuario ?: "Sin usuario"}",
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
