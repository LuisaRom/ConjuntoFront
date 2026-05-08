package com.example.app.Pantallas.RolCelador

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PantallaRecibosCelador(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel(),
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val isLoadingUsuarios by usuarioViewModel.isLoading.collectAsState()
    val errorUsuarios by usuarioViewModel.error.collectAsState()
    var intentoFallbackUsuarios by remember { mutableStateOf(false) }
    val residentes = remember(usuarios) {
        usuarios
            .filter { esRolResidente(it.rol) && it.id != null }
            .distinctBy { it.id }
            .sortedBy { it.nombre.ifBlank { it.usuario } }
    }
    val residentesIds = remember(residentes) { residentes.mapNotNull { it.id }.toSet() }

    var enelExpanded by remember { mutableStateOf(false) }
    var vantiExpanded by remember { mutableStateOf(false) }
    var epzExpanded by remember { mutableStateOf(false) }

    var enelSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var vantiSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var epzSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }

    var enelTodosSeleccionado by remember { mutableStateOf(false) }
    var vantiTodosSeleccionado by remember { mutableStateOf(false) }
    var epzTodosSeleccionado by remember { mutableStateOf(false) }

    var enelNotificado by remember { mutableStateOf(false) }
    var vantiNotificado by remember { mutableStateOf(false) }
    var epzNotificado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (usuarios.isEmpty()) {
            usuarioViewModel.obtenerResidentes()
        }
    }

    LaunchedEffect(isLoadingUsuarios, usuarios) {
        if (!isLoadingUsuarios && usuarios.isEmpty() && !intentoFallbackUsuarios) {
            intentoFallbackUsuarios = true
            usuarioViewModel.obtenerContactosMensajeria()
            delay(300)
            usuarioViewModel.obtenerResidentes()
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
                imageVector = Icons.Default.ArrowBack,
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

        if (residentes.isNotEmpty()) {
            Text(
                text = "Residentes disponibles: ${residentes.size}",
                color = GrisClaro,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!errorUsuarios.isNullOrBlank() && residentes.isEmpty()) {
            Text(
                text = errorUsuarios ?: "",
                color = Color(0xFFFFB4AB),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        ReciboItemExpandible(
            logoResId = R.drawable.logoenel,
            nombreRecibo = "ENEL",
            expanded = enelExpanded,
            onExpandClick = { enelExpanded = !enelExpanded },
            residentes = residentes,
            seleccionados = enelSeleccionados,
            todosSeleccionado = enelTodosSeleccionado,
            onSeleccionarResidente = { id ->
                val nuevoSet = if (enelSeleccionados.contains(id)) enelSeleccionados - id else enelSeleccionados + id
                enelSeleccionados = nuevoSet
                enelTodosSeleccionado = residentesIds.isNotEmpty() && nuevoSet.containsAll(residentesIds)
            },
            onSeleccionarTodos = {
                val seleccionarTodos = !enelTodosSeleccionado
                enelTodosSeleccionado = seleccionarTodos
                enelSeleccionados = if (seleccionarTodos) residentesIds else emptySet()
            },
            onEnviarNotificaciones = {
                scope.launch {
                    try {
                        val destinatarios = if (enelTodosSeleccionado) residentes else residentes.filter { it.id in enelSeleccionados }
                        if (destinatarios.isEmpty()) {
                            Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val fechaActual = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        var enviadas = 0
                        for (residente in destinatarios) {
                            if (residente.id != null) {
                                try {
                                    val notificacion = Notificacion(
                                        mensaje = "Tienes un recibo de ENEL disponible en la portería. Por favor pasa a recogerlo.",
                                        fechaEnvio = fechaActual,
                                        usuario = residente
                                    )
                                    notificacionViewModel.guardar(notificacion)
                                    delay(1500)
                                    enviadas++
                                } catch (_: Exception) {
                                }
                            }
                        }
                        notificacionViewModel.obtenerTodos()
                        enelNotificado = true
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tieneNotificacion = enelNotificado
        )

        Spacer(modifier = Modifier.height(12.dp))

        ReciboItemExpandible(
            logoResId = R.drawable.logovanti,
            nombreRecibo = "VANTI",
            expanded = vantiExpanded,
            onExpandClick = { vantiExpanded = !vantiExpanded },
            residentes = residentes,
            seleccionados = vantiSeleccionados,
            todosSeleccionado = vantiTodosSeleccionado,
            onSeleccionarResidente = { id ->
                val nuevoSet = if (vantiSeleccionados.contains(id)) vantiSeleccionados - id else vantiSeleccionados + id
                vantiSeleccionados = nuevoSet
                vantiTodosSeleccionado = residentesIds.isNotEmpty() && nuevoSet.containsAll(residentesIds)
            },
            onSeleccionarTodos = {
                val seleccionarTodos = !vantiTodosSeleccionado
                vantiTodosSeleccionado = seleccionarTodos
                vantiSeleccionados = if (seleccionarTodos) residentesIds else emptySet()
            },
            onEnviarNotificaciones = {
                scope.launch {
                    try {
                        val destinatarios = if (vantiTodosSeleccionado) residentes else residentes.filter { it.id in vantiSeleccionados }
                        if (destinatarios.isEmpty()) {
                            Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val fechaActual = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        var enviadas = 0
                        for (residente in destinatarios) {
                            if (residente.id != null) {
                                try {
                                    val notificacion = Notificacion(
                                        mensaje = "Tienes un recibo de VANTI disponible en la portería. Por favor pasa a recogerlo.",
                                        fechaEnvio = fechaActual,
                                        usuario = residente
                                    )
                                    notificacionViewModel.guardar(notificacion)
                                    delay(1500)
                                    enviadas++
                                } catch (_: Exception) {
                                }
                            }
                        }
                        delay(500)
                        vantiNotificado = true
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tieneNotificacion = vantiNotificado
        )

        Spacer(modifier = Modifier.height(12.dp))

        ReciboItemExpandible(
            logoResId = R.drawable.logoepz,
            nombreRecibo = "EPZ",
            expanded = epzExpanded,
            onExpandClick = { epzExpanded = !epzExpanded },
            residentes = residentes,
            seleccionados = epzSeleccionados,
            todosSeleccionado = epzTodosSeleccionado,
            onSeleccionarResidente = { id ->
                val nuevoSet = if (epzSeleccionados.contains(id)) epzSeleccionados - id else epzSeleccionados + id
                epzSeleccionados = nuevoSet
                epzTodosSeleccionado = residentesIds.isNotEmpty() && nuevoSet.containsAll(residentesIds)
            },
            onSeleccionarTodos = {
                val seleccionarTodos = !epzTodosSeleccionado
                epzTodosSeleccionado = seleccionarTodos
                epzSeleccionados = if (seleccionarTodos) residentesIds else emptySet()
            },
            onEnviarNotificaciones = {
                scope.launch {
                    try {
                        val destinatarios = if (epzTodosSeleccionado) residentes else residentes.filter { it.id in epzSeleccionados }
                        if (destinatarios.isEmpty()) {
                            Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val fechaActual = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        var enviadas = 0
                        for (residente in destinatarios) {
                            if (residente.id != null) {
                                try {
                                    val notificacion = Notificacion(
                                        mensaje = "Tienes un recibo de EPZ disponible en la portería. Por favor pasa a recogerlo.",
                                        fechaEnvio = fechaActual,
                                        usuario = residente
                                    )
                                    notificacionViewModel.guardar(notificacion)
                                    delay(1500)
                                    enviadas++
                                } catch (_: Exception) {
                                }
                            }
                        }
                        delay(500)
                        epzNotificado = true
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tieneNotificacion = epzNotificado
        )
    }
}

private fun esRolResidente(rol: String?): Boolean {
    val valor = rol.orEmpty().trim().uppercase()
    return valor == "RESIDENTE" || valor.contains("RESIDENTE")
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
    tieneNotificacion: Boolean
) {
    var filtroResidente by remember { mutableStateOf("") }
    val residentesFiltrados = remember(residentes, filtroResidente) {
        val criterio = filtroResidente.trim().lowercase()
        if (criterio.isBlank()) {
            residentes
        } else {
            residentes.filter { residente ->
                val nombre = residente.nombre.ifBlank { residente.usuario }.lowercase()
                val torre = residente.torre.lowercase()
                val apto = residente.apartamento.lowercase()
                val torreApto = "$torre $apto"
                nombre.contains(criterio) ||
                    torre.contains(criterio) ||
                    apto.contains(criterio) ||
                    torreApto.contains(criterio)
            }
        }
    }

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

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
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

                Divider(color = GrisClaro.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = filtroResidente,
                    onValueChange = { filtroResidente = it },
                    label = { Text("Buscar por nombre, torre o apto", color = GrisClaro) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(residentesFiltrados) { residente ->
                        if (residente.id != null) {
                            val id = residente.id
                            val isChecked = todosSeleccionado || id in seleccionados
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (todosSeleccionado) {
                                            onSeleccionarTodos()
                                            onSeleccionarResidente(id)
                                        } else {
                                            onSeleccionarResidente(id)
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (todosSeleccionado) {
                                            onSeleccionarTodos()
                                            onSeleccionarResidente(id)
                                        } else {
                                            onSeleccionarResidente(id)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = DoradoElegante,
                                        uncheckedColor = GrisClaro
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = residente.nombre.ifBlank { residente.usuario }, color = Color.White, fontSize = 14.sp)
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
                    enabled = haySeleccionados
                ) {
                    Text(
                        text = if (haySeleccionados) {
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
