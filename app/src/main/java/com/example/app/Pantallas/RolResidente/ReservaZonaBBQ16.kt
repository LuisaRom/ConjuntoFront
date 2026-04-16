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
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.Model.ReservaZonaComun
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ViewModel.ReservaZonaComunViewModel
import com.example.app.ViewModel.UsuarioViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReservaZonaBBQ(
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
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedHoraInicio by remember { mutableStateOf(false) }
    var expandedHoraFin by remember { mutableStateOf(false) }

    var sillasSeleccion by remember { mutableStateOf("No") }
    var mesasSeleccion by remember { mutableStateOf("No") }
    var expandedSillas by remember { mutableStateOf(false) }
    var expandedMesas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reservaZonaComunViewModel.obtenerTodos()
    }

    val torreApto = remember(usuarioActual) {
        val torre = usuarioActual?.torre?.ifBlank { "-" } ?: "-"
        val apto = usuarioActual?.apartamento?.ifBlank { "-" } ?: "-"
        "$torre - $apto"
    }

    val horasInicioDisponibles = remember { construirHorasInicioBbq() }
    val horasFinDisponibles = remember(horaInicio) { construirHorasFinBbq(horaInicio) }

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
    val totalServicios = sillasCosto + mesasCosto

    val serviciosAdicionales = remember(sillasSeleccion, mesasSeleccion) {
        buildList {
            if (sillasSeleccion != "No") add("sillas=$sillasSeleccion")
            if (mesasSeleccion != "No") add("mesas=$mesasSeleccion")
        }.joinToString("; ")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Zona BBQ",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoSoloLecturaBBQ("Torre - Apartamento", torreApto)

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
                label = { Text("Hora inicio (10:00 - 21:00)") },
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

        Spacer(modifier = Modifier.height(12.dp))
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

        Spacer(modifier = Modifier.height(14.dp))
        Text("Total", color = Color.LightGray, fontSize = 14.sp)
        Text(
            text = "${"%,d".format(totalServicios).replace(',', '.')} COP",
            color = DoradoElegante,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
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
                val diaSemana = obtenerDiaSemana(fecha)
                if (diaSemana == null || diaSemana !in setOf(
                        Calendar.THURSDAY,
                        Calendar.FRIDAY,
                        Calendar.SATURDAY,
                        Calendar.SUNDAY
                    )
                ) {
                    Toast.makeText(context, "Zona BBQ solo disponible jueves-domingo", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (horaInicio.isBlank() || horaFin.isBlank()) {
                    Toast.makeText(context, "Selecciona hora inicio y hora fin", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!esRangoHorarioValidoBbq(horaInicio, horaFin)) {
                    Toast.makeText(context, "Selecciona un horario válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val yaTieneReserva = reservas.any {
                    it.zonaComun.equals("zona bbq", ignoreCase = true) &&
                        it.fechaReserva == fecha &&
                        it.usuario?.id == usuarioActual?.id
                }
                if (yaTieneReserva) {
                    Toast.makeText(context, "Solo se permite 1 reserva de BBQ por día", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val reserva = ReservaZonaComun(
                    zonaComun = "zona bbq",
                    fechaReserva = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    serviciosAdicionales = serviciosAdicionales,
                    usuario = usuarioActual
                )
                reservaZonaComunViewModel.guardar(reserva) {
                    scope.launch {
                        Toast.makeText(context, "Reserva zona BBQ creada", Toast.LENGTH_SHORT).show()
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
        val today = remember { Calendar.getInstance().timeInMillis }
        val state = androidx.compose.material3.rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val calendar = Calendar.getInstance().apply { timeInMillis = utcTimeMillis }
                    val diaValido = calendar.get(Calendar.DAY_OF_WEEK) in setOf(
                        Calendar.THURSDAY,
                        Calendar.FRIDAY,
                        Calendar.SATURDAY,
                        Calendar.SUNDAY
                    )
                    val inicioDelDia = Calendar.getInstance().apply {
                        timeInMillis = utcTimeMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val hoyInicio = Calendar.getInstance().apply {
                        timeInMillis = today
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    return diaValido && inicioDelDia >= hoyInicio
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        fecha = millisToFechaIsoBbq(millis)
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
private fun CampoSoloLecturaBBQ(label: String, valor: String) {
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

private fun construirHorasInicioBbq(): List<String> {
    val horas = mutableListOf<String>()
    for (inicio in 10..21) {
        horas.add("%02d:00".format(inicio))
    }
    return horas
}

private fun construirHorasFinBbq(horaInicio: String): List<String> {
    val inicio = horaInicio.substringBefore(":").toIntOrNull() ?: return emptyList()
    val horas = mutableListOf<String>()
    for (fin in (inicio + 1)..22) {
        horas.add("%02d:00".format(fin))
    }
    return horas
}

private fun esRangoHorarioValidoBbq(horaInicio: String, horaFin: String): Boolean {
    val inicio = horaInicio.substringBefore(":").toIntOrNull() ?: return false
    val fin = horaFin.substringBefore(":").toIntOrNull() ?: return false
    return inicio in 10..21 && fin in 11..22 && fin > inicio
}

private fun millisToFechaIsoBbq(millis: Long): String {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
    } catch (e: Exception) {
        ""
    }
}

private fun obtenerDiaSemana(fechaIso: String): Int? {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaIso) ?: return null
        Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK)
    } catch (e: Exception) {
        null
    }
}