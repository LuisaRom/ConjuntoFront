package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReservaSalonComunal(
    navController: NavController,
    reservaZonaComunViewModel: ReservaZonaComunViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by reservaZonaComunViewModel.isLoading.collectAsState()

    var fecha by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var expandedHorario by remember { mutableStateOf(false) }
    val horariosDisponibles = remember {
        construirRangosSalonComunal()
    }

    var sillasSeleccion by remember { mutableStateOf("No") }
    var mesasSeleccion by remember { mutableStateOf("No") }
    var aseoSeleccion by remember { mutableStateOf("No") }

    var expandedSillas by remember { mutableStateOf(false) }
    var expandedMesas by remember { mutableStateOf(false) }
    var expandedAseo by remember { mutableStateOf(false) }

    val sillasCosto = when (sillasSeleccion) {
        "25u / $32.000" -> 32000
        "50u / $60.000" -> 60000
        "100u / $125.000" -> 125000
        else -> 0
    }
    val mesasCosto = when (mesasSeleccion) {
        "6 puestos / $8.000" -> 8000
        "10 puestos / $18.000" -> 18000
        else -> 0
    }
    val aseoCosto = if (aseoSeleccion == "Sí / $150.000") 150000 else 0
    val totalServicios = sillasCosto + mesasCosto + aseoCosto

    val serviciosAdicionales = remember(sillasSeleccion, mesasSeleccion, aseoSeleccion) {
        buildList {
            if (sillasSeleccion != "No") add("sillas=$sillasSeleccion")
            if (mesasSeleccion != "No") add("mesas=$mesasSeleccion")
            if (aseoSeleccion.startsWith("Sí")) add("aseo=si")
        }.joinToString("; ")
    }

    val torreApto = remember(usuarioActual) {
        val torre = usuarioActual?.torre?.ifBlank { "-" } ?: "-"
        val apto = usuarioActual?.apartamento?.ifBlank { "-" } ?: "-"
        "$torre - $apto"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salón Comunal", style = MaterialTheme.typography.headlineSmall, color = GrisClaro)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoSoloLecturaSalon("Torre - Apartamento", torreApto)

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

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedHorario,
            onExpandedChange = { expandedHorario = !expandedHorario }
        ) {
            OutlinedTextField(
                value = horario,
                onValueChange = {},
                readOnly = true,
                label = { Text("Horario (mínimo 5 horas)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHorario) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )
            ExposedDropdownMenu(
                expanded = expandedHorario,
                onDismissRequest = { expandedHorario = false }
            ) {
                horariosDisponibles.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            horario = opcion
                            expandedHorario = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Servicios adicionales", color = GrisClaro, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedSillas,
            onExpandedChange = { expandedSillas = !expandedSillas }
        ) {
            OutlinedTextField(
                value = sillasSeleccion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sillas") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSillas) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro
                )
            )
            ExposedDropdownMenu(
                expanded = expandedSillas,
                onDismissRequest = { expandedSillas = false }
            ) {
                listOf("No", "25u / $32.000", "50u / $60.000", "100u / $125.000").forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            sillasSeleccion = opcion
                            expandedSillas = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedMesas,
            onExpandedChange = { expandedMesas = !expandedMesas }
        ) {
            OutlinedTextField(
                value = mesasSeleccion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Mesas") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMesas) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro
                )
            )
            ExposedDropdownMenu(
                expanded = expandedMesas,
                onDismissRequest = { expandedMesas = false }
            ) {
                listOf("No", "6 puestos / $8.000", "10 puestos / $18.000").forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            mesasSeleccion = opcion
                            expandedMesas = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedAseo,
            onExpandedChange = { expandedAseo = !expandedAseo }
        ) {
            OutlinedTextField(
                value = aseoSeleccion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Aseo salón") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAseo) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro
                )
            )
            ExposedDropdownMenu(
                expanded = expandedAseo,
                onDismissRequest = { expandedAseo = false }
            ) {
                listOf("No", "Sí / $150.000").forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            aseoSeleccion = opcion
                            expandedAseo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Total", fontSize = 14.sp, color = Color.LightGray)
        Text(
            text = "${"%,d".format(totalServicios).replace(',', '.')} COP",
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val rango = horario.split("-").map { it.trim() }
                if (usuarioActual?.id == null) {
                    Toast.makeText(context, "No hay sesión activa", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (fecha.isBlank()) {
                    Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (rango.size != 2) {
                    Toast.makeText(context, "Selecciona un horario válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val reserva = ReservaZonaComun(
                    zonaComun = "salon comunal",
                    fechaReserva = fecha,
                    horaInicio = rango[0],
                    horaFin = rango[1],
                    serviciosAdicionales = serviciosAdicionales,
                    usuario = usuarioActual
                )
                reservaZonaComunViewModel.guardar(reserva) {
                    scope.launch {
                        Toast.makeText(context, "Reserva salón comunal creada", Toast.LENGTH_SHORT).show()
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

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val state = androidx.compose.material3.rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        fecha = millisToLocalDateSalon(millis).format(DateTimeFormatter.ISO_DATE)
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
private fun CampoSoloLecturaSalon(label: String, valor: String) {
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

private fun construirRangosSalonComunal(): List<String> {
    val rangos = mutableListOf<String>()
    for (inicio in 8 until 22) {
        for (duracion in 5..14) {
            val fin = inicio + duracion
            if (fin > 22) continue
            rangos.add("%02d:00 - %02d:00".format(inicio, fin))
        }
    }
    return rangos
}

private fun millisToLocalDateSalon(millis: Long): LocalDate {
    return java.time.Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

