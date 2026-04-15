package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.ReservaZonaComun
import com.example.app.ViewModel.ReservaZonaComunViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaReservaPiscina(
    navController: NavController,
    reservaZonaComunViewModel: ReservaZonaComunViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val reservas by reservaZonaComunViewModel.reservas.collectAsState()
    val isLoading by reservaZonaComunViewModel.isLoading.collectAsState()

    var fecha by remember { mutableStateOf("") }
    var horarioSeleccionado by remember { mutableStateOf("") }
    var numPersonas by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reservaZonaComunViewModel.obtenerTodos()
    }

    val torreApto = remember(usuarioActual) {
        val torre = usuarioActual?.torre?.ifBlank { "-" } ?: "-"
        val apto = usuarioActual?.apartamento?.ifBlank { "-" } ?: "-"
        "$torre - $apto"
    }

    val rangosDisponibles = remember(fecha, reservas) {
        if (fecha.isBlank()) {
            emptyList()
        } else {
            construirRangosDisponiblesPiscina(fecha, reservas)
        }
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
            Text("Piscina", color = GrisClaro, style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoSoloLectura(label = "Torre - Apartamento", valor = torreApto)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha", color = GrisClaro) },
            trailingIcon = {
                Text(
                    text = "Calendario",
                    color = DoradoElegante,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedLabelColor = GrisClaro,
                unfocusedLabelColor = GrisClaro
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Horarios disponibles", color = GrisClaro, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        if (fecha.isBlank()) {
            Text("Selecciona una fecha para ver rangos.", color = Color.LightGray, fontSize = 12.sp)
        } else if (rangosDisponibles.isEmpty()) {
            Text(
                "Sin rangos disponibles para esa fecha. Reglas: mié-dom, 8:00-12:00 y 16:00-20:00, máximo 3 horas.",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        } else {
            FlowRow {
                rangosDisponibles.forEach { rango ->
                    FilterChip(
                        selected = horarioSeleccionado == rango,
                        onClick = { horarioSeleccionado = rango },
                        label = { Text(rango) },
                        leadingIcon = if (horarioSeleccionado == rango) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = AzulOscuro) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DoradoElegante,
                            selectedLabelColor = AzulOscuro
                        ),
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CampoReservaPiscina("Número de Personas", numPersonas) { numPersonas = it }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val rango = horarioSeleccionado.split("-").map { it.trim() }
                if (usuarioActual?.id == null) {
                    Toast.makeText(context, "No hay sesión activa", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (fecha.isBlank()) {
                    Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (rango.size != 2) {
                    Toast.makeText(context, "Selecciona un horario disponible", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (numPersonas.isBlank()) {
                    Toast.makeText(context, "Ingresa el número de personas", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val reserva = ReservaZonaComun(
                    zonaComun = "piscina",
                    fechaReserva = fecha,
                    horaInicio = rango[0],
                    horaFin = rango[1],
                    usuario = usuarioActual
                )
                reservaZonaComunViewModel.guardar(reserva) {
                    scope.launch {
                        Toast.makeText(context, "Reserva piscina creada", Toast.LENGTH_SHORT).show()
                        navController.navigate("PantallaReservasResidente") {
                            popUpTo("PantallaReservasResidente") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoading) "Guardando..." else "Crear Reserva",
                color = AzulOscuro,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDatePicker) {
        val state = androidx.compose.material3.rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        fecha = millisToFechaIsoPiscina(millis)
                        horarioSeleccionado = ""
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                Text(
                    text = "Cancelar",
                    color = GrisClaro,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { showDatePicker = false }
                )
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoReservaPiscina(label: String, valor: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                cursorColor = DoradoElegante,
                focusedLabelColor = GrisClaro,
                unfocusedLabelColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoSoloLectura(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White,
                disabledBorderColor = GrisClaro,
                disabledLabelColor = GrisClaro
            )
        )
    }
}

private fun construirRangosDisponiblesPiscina(
    fechaIso: String,
    reservas: List<ReservaZonaComun>
): List<String> {
    if (!esDiaPermitidoPiscina(fechaIso)) {
        return emptyList()
    }

    val reservasPiscinaDelDia = reservas.filter {
        it.zonaComun.equals("piscina", ignoreCase = true) && it.fechaReserva == fechaIso
    }

    val bloques = listOf(8 to 12, 16 to 20)
    val rangos = mutableListOf<String>()
    for ((inicioBloque, finBloque) in bloques) {
        for (horaInicio in inicioBloque until finBloque) {
            for (duracion in 1..3) {
                val horaFin = horaInicio + duracion
                if (horaFin > finBloque) continue

                val inicioTxt = "%02d:00".format(horaInicio)
                val finTxt = "%02d:00".format(horaFin)
                val estaDisponible = reservasPiscinaDelDia.none { reserva ->
                    !(reserva.horaFin <= inicioTxt || reserva.horaInicio >= finTxt)
                }
                if (estaDisponible) {
                    rangos.add("$inicioTxt - $finTxt")
                }
            }
        }
    }
    return rangos
}

private fun millisToFechaIsoPiscina(millis: Long): String {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
    } catch (e: Exception) {
        ""
    }
}

private fun esDiaPermitidoPiscina(fechaIso: String): Boolean {
    return try {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaIso) ?: return false
        val calendar = Calendar.getInstance().apply { time = fecha }
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY -> true
            else -> false
        }
    } catch (e: Exception) {
        false
    }
}
