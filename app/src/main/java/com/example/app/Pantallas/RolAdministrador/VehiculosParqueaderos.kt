@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.Pantallas.RolAdministrador

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Auth.AuthManager
import com.example.app.Model.VehiculoResidente
import com.example.app.ViewModel.VehiculoResidenteViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro

@Composable
fun PantallaVehiculosParqueaderos(
    navController: NavController,
    viewModel: VehiculoResidenteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuario by AuthManager.currentUser.collectAsState()
    val puedeAsignar = usuario?.rol?.uppercase()?.contains("ADMIN") == true
    val vehiculos by viewModel.vehiculos.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("TODOS") }
    var enAsignacion by remember { mutableStateOf<VehiculoResidente?>(null) }

    LaunchedEffect(Unit) { viewModel.obtenerTodos() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    val total = vehiculos.size
    val conParqueadero = vehiculos.count { !it.parqueadero.isNullOrBlank() }
    val termino = busqueda.trim().lowercase()
    val visibles = vehiculos
        .filter {
            when (filtro) {
                "SIN" -> it.parqueadero.isNullOrBlank()
                "CON" -> !it.parqueadero.isNullOrBlank()
                else -> true
            }
        }
        .filter { v ->
            termino.isEmpty() || listOfNotNull(
                v.placa, v.marca, v.modelo, v.parqueadero, v.usuario?.nombre,
                v.usuario?.let { "t${it.torre}-${it.apartamento}" }, v.usuario?.let { "torre ${it.torre}" }
            ).any { it.lowercase().contains(termino) }
        }
        .sortedWith(compareBy<VehiculoResidente> { !it.parqueadero.isNullOrBlank() }.thenBy { it.usuario?.torre?.toIntOrNull() ?: 0 })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = GrisClaro)
            }
            Text(
                if (puedeAsignar) "Vehículos y parqueaderos" else "Vehículos de residentes",
                style = MaterialTheme.typography.headlineSmall.copy(color = GrisClaro),
                modifier = Modifier.weight(1f)
            )
            if (cargando) {
                CircularProgressIndicator(color = DoradoElegante, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Indicador("Registrados", total.toString(), Modifier.weight(1f))
            Indicador("Con parqueadero", conParqueadero.toString(), Modifier.weight(1f))
            Indicador("Sin asignar", (total - conParqueadero).toString(), Modifier.weight(1f))
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar placa, residente, torre o parqueadero", color = GrisClaro.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrisClaro.copy(alpha = 0.6f)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GrisClaro,
                unfocusedTextColor = GrisClaro,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro.copy(alpha = 0.3f),
                cursorColor = DoradoElegante
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("TODOS" to "Todos", "SIN" to "Sin parqueadero", "CON" to "Con parqueadero").forEach { (valor, etiqueta) ->
                FilterChip(
                    selected = filtro == valor,
                    onClick = { filtro = valor },
                    label = { Text(etiqueta, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = GrisClaro,
                        selectedContainerColor = DoradoElegante,
                        selectedLabelColor = AzulOscuro
                    )
                )
            }
        }

        error?.let {
            Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
        }

        if (visibles.isEmpty() && !cargando) {
            Text(
                if (vehiculos.isEmpty()) "Aún no hay vehículos registrados por los residentes." else "No hay vehículos con ese filtro.",
                color = GrisClaro.copy(alpha = 0.6f),
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visibles, key = { it.id ?: it.placa.hashCode().toLong() }) { v ->
                TarjetaVehiculo(v, puedeAsignar, onAsignar = { viewModel.clearError(); enAsignacion = v })
            }
        }
    }

    enAsignacion?.let { v ->
        DialogoAsignarParqueadero(
            vehiculo = v,
            cargando = cargando,
            error = error,
            onCerrar = { enAsignacion = null; viewModel.clearError() },
            onGuardar = { parqueadero ->
                v.id?.let { id -> viewModel.asignarParqueadero(id, parqueadero) { enAsignacion = null } }
            }
        )
    }
}

@Composable
private fun TarjetaVehiculo(v: VehiculoResidente, puedeAsignar: Boolean, onAsignar: () -> Unit) {
    val asignado = !v.parqueadero.isNullOrBlank()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(12.dp)
    ) {
        Icon(
            if (v.tipo == "MOTO") Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = DoradoElegante,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(v.placa.orEmpty(), color = GrisClaro, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                listOfNotNull(v.marca, v.modelo, v.color).joinToString(" · "),
                color = GrisClaro.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
            v.usuario?.let { u ->
                Text(
                    "${u.nombre} · Torre ${u.torre ?: "?"} - Apto ${u.apartamento ?: "?"}",
                    color = GrisClaro.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (asignado) DoradoElegante else Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = if (asignado) AzulOscuro else GrisClaro.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (asignado) v.parqueadero.orEmpty() else "Sin asignar",
                    color = if (asignado) AzulOscuro else GrisClaro.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (puedeAsignar) {
                TextButton(onClick = onAsignar, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                    Text(if (asignado) "Cambiar" else "Asignar", color = DoradoElegante, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DialogoAsignarParqueadero(
    vehiculo: VehiculoResidente,
    cargando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onGuardar: (String?) -> Unit
) {
    var parqueadero by remember { mutableStateOf(vehiculo.parqueadero.orEmpty()) }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = GrisOscuro,
        title = { Text("Parqueadero para ${vehiculo.placa}", color = GrisClaro) },
        text = {
            Column {
                Text(
                    listOfNotNull(vehiculo.marca, vehiculo.modelo).joinToString(" ") +
                            (vehiculo.usuario?.let { " · ${it.nombre}, T${it.torre}-${it.apartamento}" } ?: ""),
                    color = GrisClaro.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = parqueadero,
                    onValueChange = { parqueadero = it.uppercase().take(20) },
                    label = { Text("Número o código (ej. P-12, S1-045)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GrisClaro,
                        unfocusedTextColor = GrisClaro,
                        focusedBorderColor = DoradoElegante,
                        unfocusedBorderColor = GrisClaro.copy(alpha = 0.3f),
                        focusedLabelColor = DoradoElegante,
                        unfocusedLabelColor = GrisClaro.copy(alpha = 0.6f),
                        cursorColor = DoradoElegante
                    )
                )
                Text(
                    "El residente recibirá una notificación con el parqueadero asignado.",
                    color = GrisClaro.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(parqueadero) }, enabled = parqueadero.isNotBlank() && !cargando) {
                Text("Guardar", color = DoradoElegante)
            }
        },
        dismissButton = {
            Row {
                if (!vehiculo.parqueadero.isNullOrBlank()) {
                    TextButton(onClick = { onGuardar(null) }, enabled = !cargando) {
                        Text("Liberar", color = Color(0xFFFF8A80))
                    }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar", color = GrisClaro) }
            }
        }
    )
}

@Composable
private fun Indicador(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(10.dp)
    ) {
        Text(valor, color = DoradoElegante, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(titulo, color = GrisClaro.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}
