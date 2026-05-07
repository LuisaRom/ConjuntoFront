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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.filled.DateRange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReservaPiscina(
    navController: NavController,
    reservaZonaComunViewModel: ReservaZonaComunViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by reservaZonaComunViewModel.isLoading.collectAsState()

    var fecha by remember { mutableStateOf("") }
    var horarioSeleccionado by remember { mutableStateOf("") }
    var numPersonas by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        reservaZonaComunViewModel.obtenerTodos()
    }

    val torreApto = remember(usuarioActual) {
        val torre = usuarioActual?.torre?.ifBlank { "-" } ?: "-"
        val apto = usuarioActual?.apartamento?.ifBlank { "-" } ?: "-"
        "$torre - $apto"
    }

    val opcionesManana = listOf("8-9", "9-10", "10-11", "11-12")
    val opcionesTarde = listOf("16-17", "17-18", "18-19", "19-20")
    val todasLasOpciones = opcionesManana + opcionesTarde

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Horarios",
                        tint = DoradoElegante
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Horarios *",
                        color = DoradoElegante,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Miercoles a Domingo - 8am a 12m y 4pm a 8pm",
                    color = GrisClaro,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        CampoSoloLectura(label = "Torre - Apartamento", valor = torreApto)

        Spacer(modifier = Modifier.height(8.dp))

        CampoFechaConCalendarioReserva(
            label = "Fecha *",
            valor = fecha,
            onDateSelected = {
                fecha = it
                horarioSeleccionado = ""
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Hora", color = GrisClaro, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        if (fecha.isBlank()) {
            Text("Selecciona una fecha para elegir hora.", color = Color.LightGray, fontSize = 12.sp)
        } else {
            Text("MAÑANA", color = DoradoElegante, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()) {
                opcionesManana.forEach { rango ->
                    FilterChip(
                        selected = horarioSeleccionado == rango,
                        onClick = { horarioSeleccionado = rango },
                        label = { Text(formatearRangoPiscina(rango)) },
                        leadingIcon = if (horarioSeleccionado == rango) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = AzulOscuro) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GrisClaro.copy(alpha = 0.28f),
                            labelColor = Color.White,
                            selectedContainerColor = DoradoElegante,
                            selectedLabelColor = AzulOscuro
                        ),
                        modifier = Modifier.padding(end = 8.dp, bottom = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("TARDE", color = DoradoElegante, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()) {
                opcionesTarde.forEach { rango ->
                    FilterChip(
                        selected = horarioSeleccionado == rango,
                        onClick = { horarioSeleccionado = rango },
                        label = { Text(formatearRangoPiscina(rango)) },
                        leadingIcon = if (horarioSeleccionado == rango) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = AzulOscuro) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GrisClaro.copy(alpha = 0.28f),
                            labelColor = Color.White,
                            selectedContainerColor = DoradoElegante,
                            selectedLabelColor = AzulOscuro
                        ),
                        modifier = Modifier.padding(end = 8.dp, bottom = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        CampoReservaPiscina("Número de Personas *", numPersonas) {
            val soloDigitos = it.filter(Char::isDigit)
            val numero = soloDigitos.toIntOrNull()
            if (soloDigitos.isEmpty() || (numero != null && numero <= 6)) {
                numPersonas = soloDigitos
            }
        }

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
                if ((numPersonas.toIntOrNull() ?: 0) > 6) {
                    Toast.makeText(context, "Número de personas máximo: 6", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (horarioSeleccionado !in todasLasOpciones) {
                    Toast.makeText(context, "Selecciona una hora válida", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val reserva = ReservaZonaComun(
                    zonaComun = "piscina",
                    fechaReserva = fecha,
                    horaInicio = "${rango[0].padStart(2, '0')}:00",
                    horaFin = "${rango[1].padStart(2, '0')}:00",
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

}

private fun formatearRangoPiscina(rango: String): String {
    val partes = rango.split("-").map { it.trim().toIntOrNull() ?: return rango }
    if (partes.size != 2) return rango
    val inicio = partes[0]
    val fin = partes[1]
    val sufijo = if (inicio < 12) "am" else "pm"
    return "${inicio}${sufijo}-${fin}${sufijo}"
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


@Composable
private fun CampoFechaConCalendarioReserva(
    label: String,
    valor: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onDateSelected,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val fechaSeleccionada = String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                            )
                            onDateSelected(fechaSeleccionada.toIsoDateTimeOrNow())
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Abrir calendario",
                        tint = GrisClaro
                    )
                }
            },
            readOnly = false,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = DoradoElegante
            )
        )
    }
}

private fun String.toIsoDateTimeOrNow(): String {
    if (isBlank()) {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    return try {
        val parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(this)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate ?: Date())
    } catch (_: Exception) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
