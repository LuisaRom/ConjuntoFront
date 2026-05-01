package com.example.app.Pantallas.RolCelador

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Notificacion
import com.example.app.Model.Usuario
import com.example.app.R
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaRecibosCelador(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel(),
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val residentes = remember(usuarios) {
        usuarios.filter { it.rol.equals("RESIDENTE", ignoreCase = true) }
    }
    var residenteFiltroId by remember { mutableStateOf<Long?>(null) }
    val usuariosDestino = remember(residentes, residenteFiltroId) {
        if (residenteFiltroId == null) residentes else residentes.filter { it.id == residenteFiltroId }
    }
    
    // Estados para controlar qué recibos están expandidos
    var enelExpanded by remember { mutableStateOf(false) }
    var vantiExpanded by remember { mutableStateOf(false) }
    var epzExpanded by remember { mutableStateOf(false) }
    
    // Estados para controlar qué residentes están seleccionados para cada tipo de recibo
    var enelSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var vantiSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var epzSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // Estados para controlar si "TODOS" está seleccionado
    var enelTodosSeleccionado by remember { mutableStateOf(false) }
    var vantiTodosSeleccionado by remember { mutableStateOf(false) }
    var epzTodosSeleccionado by remember { mutableStateOf(false) }
    
    // Estados para controlar qué recibos tienen notificaciones activas (punto verde)
    var enelNotificado by remember { mutableStateOf(false) }
    var vantiNotificado by remember { mutableStateOf(false) }
    var epzNotificado by remember { mutableStateOf(false) }

    // Evita envíos duplicados por taps repetidos.
    var enelEnviando by remember { mutableStateOf(false) }
    var vantiEnviando by remember { mutableStateOf(false) }
    var epzEnviando by remember { mutableStateOf(false) }
    
    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        if (usuarios.isEmpty()) {
            usuarioViewModel.obtenerTodos()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Botón volver
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
            Text("Recibos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        var expandirFiltroUsuarios by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandirFiltroUsuarios,
            onExpandedChange = { expandirFiltroUsuarios = !expandirFiltroUsuarios }
        ) {
            val etiquetaSeleccion = residentes.firstOrNull { it.id == residenteFiltroId }?.let {
                it.nombre.ifBlank { it.usuario }
            } ?: "Todos"
            OutlinedTextField(
                value = etiquetaSeleccion,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtrar residentes", color = GrisClaro) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirFiltroUsuarios) },
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
                expanded = expandirFiltroUsuarios,
                onDismissRequest = { expandirFiltroUsuarios = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todos") },
                    onClick = {
                        residenteFiltroId = null
                        expandirFiltroUsuarios = false
                    }
                )
                residentes.forEach { residente ->
                    DropdownMenuItem(
                        text = { Text(residente.nombre.ifBlank { residente.usuario }) },
                        onClick = {
                            residenteFiltroId = residente.id
                            expandirFiltroUsuarios = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recibo ENEL
        ReciboItemExpandible(
            logoResId = R.drawable.logoenel,
            nombreRecibo = "ENEL",
            expanded = enelExpanded,
            onExpandClick = { enelExpanded = !enelExpanded },
            residentes = usuariosDestino,
            seleccionados = enelSeleccionados,
            todosSeleccionado = enelTodosSeleccionado,
            onSeleccionarResidente = { id ->
                val nuevoSet = if (enelSeleccionados.contains(id)) {
                    enelSeleccionados - id
                } else {
                    enelSeleccionados + id
                }
                enelSeleccionados = nuevoSet
                // Si se desmarca un residente, desmarcar "TODOS"
                if (enelTodosSeleccionado) {
                    enelTodosSeleccionado = false
                }
                // El punto verde solo se activa después de enviar, no al seleccionar
            },
            onSeleccionarTodos = {
                enelTodosSeleccionado = !enelTodosSeleccionado
                if (enelTodosSeleccionado) {
                    enelSeleccionados = usuariosDestino.mapNotNull { it.id }.toSet()
                } else {
                    enelSeleccionados = emptySet()
                }
            },
            onEnviarNotificaciones = {
                scope.launch {
                    if (enelEnviando) return@launch
                    enelEnviando = true
                    try {
                        val destinatarios = if (enelTodosSeleccionado) {
                            usuariosDestino
                        } else {
                            usuariosDestino.filter { it.id in enelSeleccionados }
                        }.distinctBy { it.id }
                        
                        if (destinatarios.isEmpty()) {
                            Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        
                        android.util.Log.d("Recibos", "Enviando notificaciones de ENEL a ${destinatarios.size} usuario(s)")
                        val fechaActual = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                        var enviadas = 0
                        
                        // Enviar notificaciones de forma secuencial para evitar saturar el servidor
                        for (residente in destinatarios) {
                            if (residente.id != null) {
                                try {
                                    android.util.Log.d("Recibos", "=== CREANDO NOTIFICACIÓN ENEL ===")
                                    android.util.Log.d("Recibos", "Residente destino - ID: ${residente.id}, Nombre: ${residente.nombre}, Rol: ${residente.rol}, Documento: ${residente.documento}")
                                    
                                    val mensajeNotificacion = "Tienes un recibo de ENEL disponible en la portería. Por favor pasa a recogerlo."
                                    android.util.Log.d("Recibos", "Mensaje: $mensajeNotificacion")
                                    
                                    val notificacion = Notificacion(
                                        mensaje = mensajeNotificacion,
                                        fechaEnvio = fechaActual,
                                        usuario = residente  // Usuario completo con todos sus campos
                                    )
                                    
                                    android.util.Log.d("Recibos", "Notificación creada - Mensaje: ${notificacion.mensaje}, UsuarioId: ${notificacion.usuario?.id}, UsuarioNombre: ${notificacion.usuario?.nombre}")
                                    
                                    // Guardar la notificación usando el ViewModel en un try-catch para evitar crashes
                                    try {
                                        notificacionViewModel.guardar(notificacion)
                                        // Pequeña separación para estabilidad sin volver lento el envío.
                                        delay(350)
                                        enviadas++
                                        android.util.Log.d("Recibos", "=== NOTIFICACIÓN ENEL GUARDADA EXITOSAMENTE ===")
                                        android.util.Log.d("Recibos", "Notificación enviada a ${residente.nombre} (ID: ${residente.id})")
                                    } catch (e: Exception) {
                                        android.util.Log.e("Recibos", "Error al guardar notificación ENEL para ${residente.nombre}: ${e.message}", e)
                                        // Continuar con el siguiente residente aunque falle uno
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("Recibos", "=== ERROR AL CREAR NOTIFICACIÓN ENEL ===")
                                    android.util.Log.e("Recibos", "Error al crear notificación de ENEL para ${residente.nombre}", e)
                                    // Continuar con el siguiente residente
                                }
                            } else {
                                android.util.Log.e("Recibos", "ADVERTENCIA: Residente ${residente.nombre} no tiene ID válido")
                            }
                        }
                        
                        // Refrescar la lista de notificaciones después de enviar todas
                        notificacionViewModel.obtenerTodos()
                        
                        enelNotificado = true
                        android.util.Log.d("Recibos", "Total notificaciones de ENEL enviadas: $enviadas")
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas usuario(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Recibos", "Error al enviar notificaciones de ENEL", e)
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        enelEnviando = false
                    }
                }
            },
            tieneNotificacion = enelNotificado,
            enviando = enelEnviando
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recibo VANTI
        ReciboItemExpandible(
            logoResId = R.drawable.logovanti,
            nombreRecibo = "VANTI",
            expanded = vantiExpanded,
            onExpandClick = { vantiExpanded = !vantiExpanded },
            residentes = usuariosDestino,
            seleccionados = vantiSeleccionados,
            todosSeleccionado = vantiTodosSeleccionado,
            onSeleccionarResidente = { id ->
                val nuevoSet = if (vantiSeleccionados.contains(id)) {
                    vantiSeleccionados - id
                } else {
                    vantiSeleccionados + id
                }
                vantiSeleccionados = nuevoSet
                // Si se desmarca un residente, desmarcar "TODOS"
                if (vantiTodosSeleccionado) {
                    vantiTodosSeleccionado = false
                }
                // El punto verde solo se activa después de enviar, no al seleccionar
            },
            onSeleccionarTodos = {
                vantiTodosSeleccionado = !vantiTodosSeleccionado
                if (vantiTodosSeleccionado) {
                    vantiSeleccionados = usuariosDestino.mapNotNull { it.id }.toSet()
                } else {
                    vantiSeleccionados = emptySet()
                }
            },
            onEnviarNotificaciones = {
                scope.launch {
                    if (vantiEnviando) return@launch
                    vantiEnviando = true
                    try {
                        val destinatarios = if (vantiTodosSeleccionado) {
                            usuariosDestino
                        } else {
                            usuariosDestino.filter { it.id in vantiSeleccionados }
                        }.distinctBy { it.id }
                        
                        if (destinatarios.isEmpty()) {
                            Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        
                        android.util.Log.d("Recibos", "Enviando notificaciones de VANTI a ${destinatarios.size} usuario(s)")
                        val fechaActual = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                        var enviadas = 0
                        
                        // Enviar notificaciones de forma secuencial para evitar saturar el servidor
                        for (residente in destinatarios) {
                            if (residente.id != null) {
                                try {
                                    android.util.Log.d("Recibos", "=== CREANDO NOTIFICACIÓN VANTI ===")
                                    android.util.Log.d("Recibos", "Residente destino - ID: ${residente.id}, Nombre: ${residente.nombre}, Rol: ${residente.rol}, Documento: ${residente.documento}")
                                    
                                    val mensajeNotificacion = "Tienes un recibo de VANTI disponible en la portería. Por favor pasa a recogerlo."
                                    android.util.Log.d("Recibos", "Mensaje: $mensajeNotificacion")
                                    
                                    val notificacion = Notificacion(
                                        mensaje = mensajeNotificacion,
                                        fechaEnvio = fechaActual,
                                        usuario = residente  // Usuario completo con todos sus campos
                                    )
                                    
                                    android.util.Log.d("Recibos", "Notificación creada - Mensaje: ${notificacion.mensaje}, UsuarioId: ${notificacion.usuario?.id}, UsuarioNombre: ${notificacion.usuario?.nombre}")
                                    
                                    // Guardar la notificación usando el ViewModel en un try-catch para evitar crashes
                                    try {
                                        notificacionViewModel.guardar(notificacion)
                                        // Pequeña separación para estabilidad sin volver lento el envío.
                                        delay(350)
                                        enviadas++
                                        android.util.Log.d("Recibos", "=== NOTIFICACIÓN VANTI GUARDADA EXITOSAMENTE ===")
                                        android.util.Log.d("Recibos", "Notificación enviada a ${residente.nombre} (ID: ${residente.id})")
                                    } catch (e: Exception) {
                                        android.util.Log.e("Recibos", "Error al guardar notificación VANTI para ${residente.nombre}: ${e.message}", e)
                                        // Continuar con el siguiente residente aunque falle uno
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("Recibos", "=== ERROR AL CREAR NOTIFICACIÓN VANTI ===")
                                    android.util.Log.e("Recibos", "Error al crear notificación de VANTI para ${residente.nombre}", e)
                                    // Continuar con el siguiente residente
                                }
                            } else {
                                android.util.Log.e("Recibos", "ADVERTENCIA: Residente ${residente.nombre} no tiene ID válido")
                            }
                        }
                        
                        vantiNotificado = true
                        android.util.Log.d("Recibos", "Total notificaciones de VANTI enviadas: $enviadas")
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas usuario(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Recibos", "Error al enviar notificaciones de VANTI", e)
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        vantiEnviando = false
                    }
                }
            },
            tieneNotificacion = vantiNotificado,
            enviando = vantiEnviando
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recibo EPZ
        ReciboItemExpandible(
            logoResId = R.drawable.logoepz,
            nombreRecibo = "EPZ",
            expanded = epzExpanded,
            onExpandClick = { epzExpanded = !epzExpanded },
            residentes = usuariosDestino,
            seleccionados = epzSeleccionados,
            todosSeleccionado = epzTodosSeleccionado,
            onSeleccionarResidente = { id ->
                val nuevoSet = if (epzSeleccionados.contains(id)) {
                    epzSeleccionados - id
                } else {
                    epzSeleccionados + id
                }
                epzSeleccionados = nuevoSet
                // Si se desmarca un residente, desmarcar "TODOS"
                if (epzTodosSeleccionado) {
                    epzTodosSeleccionado = false
                }
                // El punto verde solo se activa después de enviar, no al seleccionar
            },
            onSeleccionarTodos = {
                epzTodosSeleccionado = !epzTodosSeleccionado
                if (epzTodosSeleccionado) {
                    epzSeleccionados = usuariosDestino.mapNotNull { it.id }.toSet()
                } else {
                    epzSeleccionados = emptySet()
                }
            },
            onEnviarNotificaciones = {
                scope.launch {
                    if (epzEnviando) return@launch
                    epzEnviando = true
                    try {
                        val destinatarios = if (epzTodosSeleccionado) {
                            usuariosDestino
                        } else {
                            usuariosDestino.filter { it.id in epzSeleccionados }
                        }.distinctBy { it.id }
                        
                        if (destinatarios.isEmpty()) {
                            Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        
                        android.util.Log.d("Recibos", "Enviando notificaciones de EPZ a ${destinatarios.size} usuario(s)")
                        val fechaActual = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                        var enviadas = 0
                        
                        // Enviar notificaciones de forma secuencial para evitar saturar el servidor
                        for (residente in destinatarios) {
                            if (residente.id != null) {
                                try {
                                    android.util.Log.d("Recibos", "=== CREANDO NOTIFICACIÓN EPZ ===")
                                    android.util.Log.d("Recibos", "Residente destino - ID: ${residente.id}, Nombre: ${residente.nombre}, Rol: ${residente.rol}, Documento: ${residente.documento}")
                                    
                                    val mensajeNotificacion = "Tienes un recibo de EPZ disponible en la portería. Por favor pasa a recogerlo."
                                    android.util.Log.d("Recibos", "Mensaje: $mensajeNotificacion")
                                    
                                    val notificacion = Notificacion(
                                        mensaje = mensajeNotificacion,
                                        fechaEnvio = fechaActual,
                                        usuario = residente  // Usuario completo con todos sus campos
                                    )
                                    
                                    android.util.Log.d("Recibos", "Notificación creada - Mensaje: ${notificacion.mensaje}, UsuarioId: ${notificacion.usuario?.id}, UsuarioNombre: ${notificacion.usuario?.nombre}")
                                    
                                    // Guardar la notificación usando el ViewModel en un try-catch para evitar crashes
                                    try {
                                        notificacionViewModel.guardar(notificacion)
                                        // Pequeña separación para estabilidad sin volver lento el envío.
                                        delay(350)
                                        enviadas++
                                        android.util.Log.d("Recibos", "=== NOTIFICACIÓN EPZ GUARDADA EXITOSAMENTE ===")
                                        android.util.Log.d("Recibos", "Notificación enviada a ${residente.nombre} (ID: ${residente.id})")
                                    } catch (e: Exception) {
                                        android.util.Log.e("Recibos", "Error al guardar notificación EPZ para ${residente.nombre}: ${e.message}", e)
                                        // Continuar con el siguiente residente aunque falle uno
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("Recibos", "=== ERROR AL CREAR NOTIFICACIÓN EPZ ===")
                                    android.util.Log.e("Recibos", "Error al crear notificación de EPZ para ${residente.nombre}", e)
                                    // Continuar con el siguiente residente
                                }
                            } else {
                                android.util.Log.e("Recibos", "ADVERTENCIA: Residente ${residente.nombre} no tiene ID válido")
                            }
                        }
                        
                        epzNotificado = true
                        android.util.Log.d("Recibos", "Total notificaciones de EPZ enviadas: $enviadas")
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas usuario(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Recibos", "Error al enviar notificaciones de EPZ", e)
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        epzEnviando = false
                    }
                }
            },
            tieneNotificacion = epzNotificado,
            enviando = epzEnviando
        )
    }
}

@Composable
fun ReciboItemExpandible(
    logoResId: Int,
    nombreRecibo: String,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    residentes: List<Usuario>,
    seleccionados: Set<Long>,
    todosSeleccionado: Boolean,
    onSeleccionarResidente: (Long) -> Unit,
    onSeleccionarTodos: () -> Unit,
    onEnviarNotificaciones: () -> Unit,
    tieneNotificacion: Boolean,
    enviando: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AzulOscuro.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con logo, nombre, punto verde y botón expandir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = logoResId),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreRecibo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (expanded) {
                        Text(
                            text = "${seleccionados.size} residente(s) seleccionado(s)",
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                    }
                }
                // Punto verde si hay notificación activa
                if (tieneNotificacion) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Green, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = DoradoElegante,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Contenido expandible con checklist
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Checkbox "TODOS"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeleccionarTodos() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = todosSeleccionado,
                        onCheckedChange = { onSeleccionarTodos() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DoradoElegante,
                            uncheckedColor = GrisClaro
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODOS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                HorizontalDivider(color = GrisClaro.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lista de residentes con scroll
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(residentes) { residente ->
                        if (residente.id != null) {
                            val isChecked = todosSeleccionado || residente.id in seleccionados
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        if (todosSeleccionado) {
                                            // Si "TODOS" está marcado, desmarcarlo y marcar solo este
                                            onSeleccionarTodos()
                                            onSeleccionarResidente(residente.id!!)
                                        } else {
                                            onSeleccionarResidente(residente.id!!)
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { 
                                        if (todosSeleccionado) {
                                            // Si "TODOS" está marcado, desmarcarlo y marcar solo este
                                            onSeleccionarTodos()
                                            onSeleccionarResidente(residente.id!!)
                                        } else {
                                            onSeleccionarResidente(residente.id!!)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = DoradoElegante,
                                        uncheckedColor = GrisClaro
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = residente.nombre ?: residente.usuario,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    if (!residente.torre.isNullOrBlank() || !residente.apartamento.isNullOrBlank()) {
                                        Text(
                                            text = "Torre ${residente.torre ?: ""} - Apt ${residente.apartamento ?: ""}",
                                            color = GrisClaro,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón Enviar Notificaciones (solo habilitado si hay residentes seleccionados)
                val haySeleccionados = todosSeleccionado || seleccionados.isNotEmpty()
                Button(
                    onClick = onEnviarNotificaciones,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoradoElegante,
                        disabledContainerColor = GrisClaro
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = haySeleccionados && !enviando
                ) {
                    Text(
                        text = if (enviando) {
                            "Enviando..."
                        } else if (haySeleccionados) {
                            "Enviar Notificaciones (${if (todosSeleccionado) residentes.size else seleccionados.size})"
                        } else {
                            "Selecciona residentes"
                        },
                        color = if (haySeleccionados) AzulOscuro else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}