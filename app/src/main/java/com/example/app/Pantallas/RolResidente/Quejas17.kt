package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Queja
import com.example.app.ViewModel.QuejaViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaQuejasResidente(
    navController: NavController,
    quejaViewModel: QuejaViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val quejas by quejaViewModel.quejas.collectAsState()
    val isLoading by quejaViewModel.isLoading.collectAsState()

    var torreAcusado by remember { mutableStateOf("") }
    var apartamentoAcusado by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val opcionesTipo = listOf("Ruido", "Violencia", "Mascota")
    val torreAptoReporta = remember(usuarioActual) {
        val torre = usuarioActual?.torre?.ifBlank { "-" } ?: "-"
        val apto = usuarioActual?.apartamento?.ifBlank { "-" } ?: "-"
        "$torre - $apto"
    }
    val misQuejas = remember(quejas, usuarioActual?.id) {
        quejas.filter { it.usuario?.id == usuarioActual?.id }
    }

    LaunchedEffect(Unit) {
        quejaViewModel.obtenerTodos()
    }

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
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quejas",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Reporta (torre y apartamento)", color = Color.White)
        OutlinedTextField(
            value = torreAptoReporta,
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

        Spacer(modifier = Modifier.height(12.dp))

        Text("Torre del acusado *", color = Color.White)
        OutlinedTextField(
            value = torreAcusado,
            onValueChange = { torreAcusado = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AzulOscuro,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Apartamento del acusado (opcional)", color = Color.White)
        OutlinedTextField(
            value = apartamentoAcusado,
            onValueChange = { apartamentoAcusado = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AzulOscuro,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Fecha *", color = Color.White)
        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            readOnly = false,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { abrirDatePickerQuejas(context) { fecha = it } }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Abrir calendario",
                        tint = GrisClaro
                    )
                }
            },
            placeholder = { Text("Seleccionar en calendario") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AzulOscuro,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Tipo", color = Color.White)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = tipo,
                onValueChange = {},
                label = { Text("Selecciona el tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AzulOscuro,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                opcionesTipo.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            tipo = opcion
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Mensaje", color = Color.White)
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AzulOscuro,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (usuarioActual?.id == null) {
                    Toast.makeText(context, "No hay sesión activa", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (torreAcusado.isBlank() || fecha.isBlank() || tipo.isBlank() || mensaje.isBlank()) {
                    Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val detalle = buildString {
                    append("Fecha: $fecha\n")
                    append("Tipo: $tipo\n")
                    append("Acusado Torre: $torreAcusado")
                    if (apartamentoAcusado.isNotBlank()) append(" - Apto: $apartamentoAcusado")
                    append("\n")
                    append("Descripción: $mensaje")
                }
                val queja = Queja(
                    descripcion = detalle,
                    tipo = tipo,
                    torreApartamento = "$torreAcusado${if (apartamentoAcusado.isNotBlank()) " - $apartamentoAcusado" else ""}",
                    mensaje = mensaje,
                    fechaCreacion = fecha,
                    estado = "En proceso",
                    usuario = usuarioActual
                )
                quejaViewModel.guardar(queja) {
                    Toast.makeText(context, "Queja creada con éxito", Toast.LENGTH_SHORT).show()
                    navController.navigate("PantallaMenuResidente") {
                        popUpTo("PantallaMenuResidente") { inclusive = false }
                        launchSingleTop = true
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
                if (isLoading) "Guardando..." else "Crear Queja",
                color = AzulOscuro,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Mis quejas y seguimiento", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (misQuejas.isEmpty()) {
            Text("Aún no has creado quejas.", color = Color.LightGray)
        } else {
            misQuejas.forEach { q ->
                val finalizada = q.estado.equals("finalizada", true) || q.estado.equals("finalizado", true)
                val colorEstado = if (finalizada) Color(0xFF2E7D32) else Color(0xFFE6A700)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Tipo: ${q.categoriaVisual()}", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Detalle: ${q.detalleVisual()}", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (finalizada) "Finalizada" else "En proceso",
                            color = Color.White,
                            modifier = Modifier
                                .background(colorEstado, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

}

private fun abrirDatePickerQuejas(
    context: android.content.Context,
    onDateSelected: (String) -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
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
            onDateSelected(fechaSeleccionada)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    ).show()
}