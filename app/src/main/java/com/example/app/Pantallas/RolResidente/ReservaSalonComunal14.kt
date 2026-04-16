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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.util.Date
import java.util.Locale
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
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var expandedHoraInicio by remember { mutableStateOf(false) }
    var expandedHoraFin by remember { mutableStateOf(false) }
    val horasInicioDisponibles = remember { construirHorasSalonComunalInicio() }
    val horasFinDisponibles = remember(horaInicio) { construirHorasSalonComunalFin(horaInicio) }

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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            expanded = expandedHoraInicio,
            onExpandedChange = { expandedHoraInicio = !expandedHoraInicio }
        ) {
            OutlinedTextField(
                value = horaInicio,
                onValueChange = {},
                readOnly = true,
                label = { Text("Hora inicio (08:00 - 21:00)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraInicio) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
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
                expanded = expandedHoraInicio,
                onDismissRequest = { expandedHoraInicio = false }
            ) {
                horasInicioDisponibles.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            horaInicio = opcion
                            horaFin = ""
                            expandedHoraInicio = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedHoraFin,
            onExpandedChange = { expandedHoraFin = !expandedHoraFin }
        ) {
            OutlinedTextField(
                value = horaFin,
                onValueChange = {},
                readOnly = true,
                label = { Text("Hora fin (máximo 22:00)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraFin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
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
                expanded = expandedHoraFin,
                onDismissRequest = { expandedHoraFin = false }
            ) {
                horasFinDisponibles.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            horaFin = opcion
                            expandedHoraFin = false
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
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
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
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
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
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
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
                if (usuarioActual?.id == null) {
                    Toast.makeText(context, "No hay sesión activa", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (fecha.isBlank()) {
                    Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (horaInicio.isBlank() || horaFin.isBlank()) {
                    Toast.makeText(context, "Selecciona hora inicio y hora fin", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!esRangoHorarioValidoSalon(horaInicio, horaFin)) {
                    Toast.makeText(context, "El rango horario seleccionado no es válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val reserva = ReservaZonaComun(
                    zonaComun = "salon comunal",
                    fechaReserva = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
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
                        fecha = millisToFechaIsoSalon(millis)
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

private fun construirHorasSalonComunalInicio(): List<String> {
    val horas = mutableListOf<String>()
    for (inicio in 8..21) {
        horas.add("%02d:00".format(inicio))
    }
    return horas
}

private fun construirHorasSalonComunalFin(horaInicio: String): List<String> {
    val inicio = horaInicio.substringBefore(":").toIntOrNull() ?: return emptyList()
    val horas = mutableListOf<String>()
    for (fin in (inicio + 1)..22) {
        horas.add("%02d:00".format(fin))
    }
    return horas
}

private fun esRangoHorarioValidoSalon(horaInicio: String, horaFin: String): Boolean {
    val inicio = horaInicio.substringBefore(":").toIntOrNull() ?: return false
    val fin = horaFin.substringBefore(":").toIntOrNull() ?: return false
    return inicio in 8..21 && fin in 9..22 && fin > inicio
}

private fun millisToFechaIsoSalon(millis: Long): String {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
    } catch (e: Exception) {
        ""
    }
}

