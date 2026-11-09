package com.example.app.Pantallas.RolCelador

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.app.Model.Paqueteria
import com.example.app.Model.Usuario
import com.example.app.ViewModel.PaqueteriaViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun nombreVisible(u: Usuario): String =
    u.nombre.orEmpty().ifBlank { u.usuario.orEmpty() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetallesPaqueteriaCelador(
    navController: NavController,
    paqueteriaViewModel: PaqueteriaViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados del formulario
    var torre by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var residenteSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var numeroPaquete by remember { mutableStateOf("") }
    var transportadora by remember { mutableStateOf("") }
    var expandedTransportadora by remember { mutableStateOf(false) }

    // Estados de los ViewModels
    val encontrados by usuarioViewModel.residentesEncontrados.collectAsState()
    val buscando by usuarioViewModel.buscando.collectAsState()
    val busquedaRealizada by usuarioViewModel.busquedaRealizada.collectAsState()
    val errorBusqueda by usuarioViewModel.errorBusqueda.collectAsState()
    val isLoadingPaquete by paqueteriaViewModel.isLoading.collectAsState()
    val errorPaquete by paqueteriaViewModel.error.collectAsState()

    // Verifica en la base de datos cuando torre y apartamento tienen texto
    // (espera 500 ms después de que el celador deja de escribir)
    LaunchedEffect(torre, apartamento) {
        residenteSeleccionado = null
        usuarioViewModel.limpiarBusqueda()
        if (torre.isNotBlank() && apartamento.isNotBlank()) {
            delay(500)
            usuarioViewModel.buscarResidente(torre, apartamento)
        }
    }

    // Si hay un único residente en ese apartamento, se selecciona solo
    LaunchedEffect(encontrados) {
        if (encontrados.size == 1) residenteSeleccionado = encontrados.first()
    }

    // Mostrar errores del paquete
    LaunchedEffect(errorPaquete) {
        errorPaquete?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    val opcionesTransportadoras = listOf(
        "Servientrega", "Coordinadora", "Interrapidísimo", "Envía", "Deprisa", "Fedex", "DHL"
    )

    // Función para guardar el paquete
    fun guardarPaquete() {
        val residente = residenteSeleccionado
        if (residente == null) {
            Toast.makeText(
                context,
                "Escribe torre y apartamento de un residente registrado",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (residente.id == null) {
            Toast.makeText(context, "Error: El residente seleccionado no tiene ID válido", Toast.LENGTH_SHORT).show()
            Log.e("Paqueteria", "Residente seleccionado sin ID: $residente")
            return
        }

        if (transportadora.isBlank()) {
            Toast.makeText(context, "Por favor selecciona una transportadora", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaActual = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val paquete = Paqueteria(
            transportadora = transportadora,
            fechaRecepcion = fechaActual,
            estado = "PENDIENTE",
            usuario = residente
        )

        Log.d(
            "Paqueteria",
            "Guardando paquete: transportadora=$transportadora, usuarioId=${residente.id}, nombre=${residente.nombre}, fecha=$fechaActual"
        )

        scope.launch {
            try {
                paqueteriaViewModel.guardar(paquete, residente)

                // Esperar un momento para que se guarde la notificación
                delay(1000)

                Toast.makeText(
                    context,
                    "Paquete registrado y notificación enviada al residente",
                    Toast.LENGTH_SHORT
                ).show()

                // Limpiar campos
                transportadora = ""
                numeroPaquete = ""
                torre = ""
                apartamento = ""

                delay(500)
                navController.popBackStack()
            } catch (e: Exception) {
                Log.e("Paqueteria", "Error al guardar paquete", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
                tint = Color.White,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registrar Paquete",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Datos del Paquete", fontWeight = FontWeight.Bold, color = GrisClaro, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Residente: se escribe torre y apartamento y se verifica en la base de datos
        Text(
            "Residente *",
            color = GrisClaro,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                CampoTexto(label = "Torre", valor = torre, onValueChange = { torre = it })
            }
            Box(modifier = Modifier.weight(1f)) {
                CampoTexto(label = "Apartamento", valor = apartamento, onValueChange = { apartamento = it })
            }
        }

        when {
            buscando -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = DoradoElegante,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verificando residente...", color = GrisClaro, fontSize = 12.sp)
            }

            errorBusqueda != null -> Text(
                text = errorBusqueda ?: "",
                color = Color(0xFFFFB4AB),
                fontSize = 12.sp
            )

            busquedaRealizada && encontrados.isEmpty() -> Text(
                text = "No hay un residente registrado en Torre $torre - Apto $apartamento",
                color = Color(0xFFFFB4AB),
                fontSize = 12.sp
            )

            encontrados.size == 1 -> Text(
                text = "✔ Residente: ${nombreVisible(encontrados.first())}",
                color = Color(0xFF81C784),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            encontrados.size > 1 -> {
                Text(
                    "Varios residentes en este apartamento, elige uno:",
                    color = GrisClaro,
                    fontSize = 12.sp
                )
                encontrados.forEach { r ->
                    val elegido = residenteSeleccionado?.id == r.id
                    Text(
                        text = (if (elegido) "● " else "○ ") + nombreVisible(r),
                        color = if (elegido) DoradoElegante else Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { residenteSeleccionado = r }
                            .padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Número de Paquete (opcional)
        CampoTexto(
            label = "Número de Paquete (opcional)",
            valor = numeroPaquete,
            onValueChange = { numeroPaquete = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Menú desplegable para transportadora
        Text(
            "Transportadora *",
            color = GrisClaro,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expandedTransportadora,
            onExpandedChange = { expandedTransportadora = !expandedTransportadora }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = transportadora,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(vertical = 4.dp),
                label = { Text("Selecciona la transportadora", color = Color.White) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransportadora)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = DoradoElegante,
                    unfocusedLabelColor = GrisClaro,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedContainerColor = AzulOscuro,
                    unfocusedContainerColor = AzulOscuro
                )
            )
            ExposedDropdownMenu(
                expanded = expandedTransportadora,
                onDismissRequest = { expandedTransportadora = false },
                modifier = Modifier.background(AzulOscuro)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    opcionesTransportadoras.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, color = Color.White, fontSize = 14.sp) },
                            onClick = {
                                transportadora = opcion
                                expandedTransportadora = false
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Enviar
        Button(
            onClick = { guardarPaquete() },
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoadingPaquete && residenteSeleccionado != null
        ) {
            if (isLoadingPaquete) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Enviar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CampoTexto(
    label: String,
    valor: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = GrisClaro, fontSize = 14.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = DoradoElegante,
                unfocusedLabelColor = GrisClaro,
                disabledTextColor = GrisClaro,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedContainerColor = AzulOscuro,
                unfocusedContainerColor = AzulOscuro
            )
        )
    }
}