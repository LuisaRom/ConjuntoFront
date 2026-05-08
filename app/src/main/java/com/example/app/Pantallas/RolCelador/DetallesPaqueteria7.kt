package com.example.app.Pantallas.RolCelador

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.app.Model.Paqueteria
import com.example.app.Model.Usuario
import com.example.app.ViewModel.PaqueteriaViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var residenteSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var torreApto by remember { mutableStateOf("") }
    var numeroPaquete by remember { mutableStateOf("") }
    var transportadora by remember { mutableStateOf("") }
    
    // Estados para los menús desplegables
    var expandedResidente by remember { mutableStateOf(false) }
    var expandedTransportadora by remember { mutableStateOf(false) }
    
    // Estados del ViewModel
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val isLoading by usuarioViewModel.isLoading.collectAsState()
    val errorUsuarios by usuarioViewModel.error.collectAsState()
    val paquetes by paqueteriaViewModel.paquetes.collectAsState()
    val isLoadingPaquete by paqueteriaViewModel.isLoading.collectAsState()
    val errorPaquete by paqueteriaViewModel.error.collectAsState()
    var intentoFallbackUsuarios by remember { mutableStateOf(false) }
    
    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerResidentes()
        paqueteriaViewModel.obtenerTodos()
    }

    // Si no cargan usuarios por permisos/ruta, intentar endpoint alterno.
    LaunchedEffect(isLoading, usuarios) {
        if (!isLoading && usuarios.isEmpty() && !intentoFallbackUsuarios) {
            intentoFallbackUsuarios = true
            usuarioViewModel.obtenerContactosMensajeria()
            // Reintento con carga de residentes por rutas alternativas.
            kotlinx.coroutines.delay(300)
            usuarioViewModel.obtenerResidentes()
        }
    }
    
    // Filtrar residentes desde usuarios y fallback desde paquetes existentes.
    val residentes = remember(usuarios, paquetes) {
        val residentesUsuarios = usuarios.filter { esRolResidente(it.rol) }
        val residentesDesdePaquetes = paquetes
            .mapNotNull { it.usuario }
            .filter { usuario ->
                esRolResidente(usuario.rol) ||
                    usuario.torre.isNotBlank() ||
                    usuario.apartamento.isNotBlank()
            }

        (residentesUsuarios + residentesDesdePaquetes)
            .distinctBy { it.id ?: "${it.usuario}-${it.nombre}-${it.documento}" }
            .sortedBy { if (it.nombre.isNotBlank()) it.nombre else it.usuario }
    }
    
    // Autocompletar torre-apto cuando se selecciona un residente
    LaunchedEffect(residenteSeleccionado) {
        residenteSeleccionado?.let { residente ->
            val torre = residente.torre ?: ""
            val apto = residente.apartamento ?: ""
            torreApto = if (torre.isNotBlank() && apto.isNotBlank()) {
                "$torre - $apto"
            } else {
                ""
            }
        }
    }
    
    // Mostrar errores
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
        if (residenteSeleccionado == null) {
            Toast.makeText(context, "Por favor selecciona un residente", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (residenteSeleccionado?.id == null) {
            Toast.makeText(context, "Error: El residente seleccionado no tiene ID válido", Toast.LENGTH_SHORT).show()
            Log.e("Paqueteria", "Residente seleccionado sin ID: $residenteSeleccionado")
            return
        }
        
        if (transportadora.isBlank()) {
            Toast.makeText(context, "Por favor selecciona una transportadora", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Crear el objeto Paqueteria con usuario completo
        val fechaActual = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).format(Date())
        
        // Usar el usuario completo para el paquete (el backend debería poder manejarlo)
        val paquete = Paqueteria(
            transportadora = transportadora,
            fechaRecepcion = fechaActual,
            estado = "PENDIENTE",
            usuario = residenteSeleccionado!!
        )
        
        Log.d("Paqueteria", "Guardando paquete: transportadora=$transportadora, usuarioId=${residenteSeleccionado!!.id}, nombre=${residenteSeleccionado!!.nombre}, fecha=$fechaActual")
        Log.d("Paqueteria", "Usuario completo seleccionado: id=${residenteSeleccionado!!.id}, nombre=${residenteSeleccionado!!.nombre}, rol=${residenteSeleccionado!!.rol}, documento=${residenteSeleccionado!!.documento}")
        
        scope.launch {
            try {
                // Guardar el paquete pasando el usuario completo para la notificación
                paqueteriaViewModel.guardar(paquete, residenteSeleccionado!!)
                
                // Esperar un momento para que se guarde la notificación
                kotlinx.coroutines.delay(1000)
                
                Toast.makeText(context, "Paquete registrado y notificación enviada al residente", Toast.LENGTH_SHORT).show()
                
                // Limpiar campos
                residenteSeleccionado = null
                torreApto = ""
                transportadora = ""
                numeroPaquete = ""
                
                // Esperar un momento antes de volver para que se vea el mensaje
                kotlinx.coroutines.delay(500)
                navController.popBackStack()
            } catch (e: Exception) {
                Log.e("Paqueteria", "Error al guardar paquete", e)
                Log.e("Paqueteria", "Excepción completa: ${e.message}", e)
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
        
        // Menú desplegable para seleccionar residente
        Text("Residente *", color = GrisClaro, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedResidente,
            onExpandedChange = { expandedResidente = !expandedResidente }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = residenteSeleccionado?.let { 
                    "${it.nombre} - ${it.torre} - ${it.apartamento}"
                } ?: "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .padding(vertical = 4.dp),
                label = { Text("Selecciona el residente", color = Color.White) },
                placeholder = { Text("Buscar residente...", color = GrisClaro) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedResidente)
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
                expanded = expandedResidente,
                onDismissRequest = { expandedResidente = false },
                modifier = Modifier
                    .background(AzulOscuro)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isLoading) {
                        DropdownMenuItem(
                            text = { Text("Cargando residentes...", color = Color.White) },
                            onClick = {},
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White
                            )
                        )
                    } else if (residentes.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    errorUsuarios ?: "Sin residentes cargados",
                                    color = Color.White
                                )
                            },
                            onClick = {
                                usuarioViewModel.obtenerResidentes()
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White
                            )
                        )
                    } else {
                        residentes.forEach { residente ->
                            DropdownMenuItem(
                                text = { 
                                    Column(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (residente.nombre.isNotBlank()) residente.nombre else residente.usuario,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Torre ${residente.torre} - Apt ${residente.apartamento}",
                                            color = GrisClaro,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    residenteSeleccionado = residente
                                    expandedResidente = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        if (!errorUsuarios.isNullOrBlank() && residentes.isEmpty()) {
            Text(
                text = errorUsuarios ?: "",
                color = Color(0xFFFFB4AB),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Campo Torre - Apartamento (se autocompleta)
        CampoTexto(
            label = "Torre - Apartamento",
            valor = torreApto,
            onValueChange = { torreApto = it },
            enabled = false
        )
        
        // Campo Número de Paquete (opcional)
        CampoTexto(
            label = "Número de Paquete (opcional)",
            valor = numeroPaquete,
            onValueChange = { numeroPaquete = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        // Menú desplegable para transportadora
        Text("Transportadora *", color = GrisClaro, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .padding(vertical = 4.dp),
                label = { Text("Selecciona la transportadora", color = Color.White) },
                placeholder = { Text("Buscar transportadora...", color = GrisClaro) },
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
                modifier = Modifier
                    .background(AzulOscuro)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    opcionesTransportadoras.forEach { opcion ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    opcion, 
                                    color = Color.White,
                                    fontSize = 14.sp
                                ) 
                            },
                            onClick = {
                                transportadora = opcion
                                expandedTransportadora = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botón Enviar
        Button(
            onClick = { guardarPaquete() },
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoadingPaquete
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

private fun esRolResidente(rol: String?): Boolean {
    val valor = rol.orEmpty().trim().uppercase()
    return valor == "RESIDENTE" || valor.contains("RESIDENTE")
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
