@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
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
import com.example.app.Model.VehiculoResidente
import com.example.app.ViewModel.VehiculoResidenteViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro

private val TIPOS_VEHICULO = listOf("CARRO" to "Carro", "MOTO" to "Moto", "CAMIONETA" to "Camioneta")
private val PLACA_VALIDA = Regex("^[A-Z]{3}\\d{2}[A-Z0-9]?$")

@Composable
fun SeccionMisVehiculos(viewModel: VehiculoResidenteViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val vehiculos by viewModel.misVehiculos.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var porEliminar by remember { mutableStateOf<VehiculoResidente?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarMisVehiculos() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mis vehículos", color = GrisClaro, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.clearError(); mostrarFormulario = true }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DoradoElegante)
                Spacer(Modifier.width(4.dp))
                Text("Agregar vehículo", color = DoradoElegante)
            }
        }

        if (vehiculos.isEmpty() && !cargando) {
            Text(
                "No tienes vehículos registrados. Agrégalos para que la administración te asigne parqueadero.",
                color = GrisClaro.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        vehiculos.forEach { v ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
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
                    Text(v.placa.orEmpty(), color = GrisClaro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        listOfNotNull(v.marca, v.modelo, v.color).joinToString(" · "),
                        color = GrisClaro.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        if (v.parqueadero.isNullOrBlank()) "Parqueadero pendiente de asignación" else "Parqueadero ${v.parqueadero}",
                        color = if (v.parqueadero.isNullOrBlank()) GrisClaro.copy(alpha = 0.6f) else DoradoElegante,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { porEliminar = v }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = GrisClaro.copy(alpha = 0.6f))
                }
            }
        }

        if (!mostrarFormulario) {
            error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
        }
    }

    if (mostrarFormulario) {
        DialogoNuevoVehiculo(
            cargando = cargando,
            error = error,
            onCerrar = { mostrarFormulario = false; viewModel.clearError() },
            onGuardar = { placa, marca, modelo, color, tipo ->
                viewModel.registrarMiVehiculo(placa, marca, modelo, color, tipo) { mostrarFormulario = false }
            }
        )
    }

    porEliminar?.let { v ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            containerColor = GrisOscuro,
            title = { Text("Eliminar vehículo", color = GrisClaro) },
            text = { Text("¿Eliminar el vehículo ${v.placa}? Si tiene parqueadero asignado, quedará libre.", color = GrisClaro) },
            confirmButton = {
                TextButton(onClick = {
                    v.id?.let { viewModel.eliminarMiVehiculo(it) }
                    porEliminar = null
                }) { Text("Eliminar", color = Color(0xFFFF8A80)) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar", color = GrisClaro) }
            }
        )
    }
}

@Composable
private fun DialogoNuevoVehiculo(
    cargando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onGuardar: (String, String, String, String, String) -> Unit
) {
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("CARRO") }
    val placaValida = PLACA_VALIDA.matches(placa)
    val completo = marca.isNotBlank() && modelo.isNotBlank() && placaValida

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = GrisOscuro,
        title = { Text("Agregar vehículo", color = GrisClaro) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TIPOS_VEHICULO.forEach { (valor, etiqueta) ->
                        FilterChip(
                            selected = tipo == valor,
                            onClick = { tipo = valor },
                            label = { Text(etiqueta, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = GrisClaro,
                                selectedContainerColor = DoradoElegante,
                                selectedLabelColor = AzulOscuro
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                CampoVehiculo(marca, { marca = it }, "Marca (ej. Mazda)")
                CampoVehiculo(modelo, { modelo = it }, "Modelo (ej. 2 Sedán 2021)")
                CampoVehiculo(
                    valor = placa,
                    onCambio = { nuevo -> placa = nuevo.uppercase().filter { it.isLetterOrDigit() }.take(6) },
                    etiqueta = if (tipo == "MOTO") "Placa (ej. ABC12D)" else "Placa (ej. ABC123)",
                    esError = placa.length >= 5 && !placaValida,
                    mayusculas = true
                )
                CampoVehiculo(color, { color = it }, "Color (opcional)")
                if (placa.length >= 5 && !placaValida) {
                    Text("Usa 3 letras y 3 números (carro) o 3 letras, 2 números y una letra (moto).", color = Color(0xFFFF8A80), fontSize = 12.sp)
                }
                error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(placa, marca, modelo, color, tipo) }, enabled = completo && !cargando) {
                Text(if (cargando) "Guardando..." else "Guardar", color = if (completo) DoradoElegante else GrisClaro.copy(alpha = 0.4f))
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar", color = GrisClaro) }
        }
    )
}

@Composable
private fun CampoVehiculo(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    esError: Boolean = false,
    mayusculas: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = esError,
        keyboardOptions = KeyboardOptions(
            capitalization = if (mayusculas) KeyboardCapitalization.Characters else KeyboardCapitalization.Sentences
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
}
