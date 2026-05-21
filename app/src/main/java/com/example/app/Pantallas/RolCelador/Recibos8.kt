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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.app.Model.Usuario
import com.example.app.R
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun PantallaRecibosCelador(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel(),
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val residentes = usuarios.filter { usuario ->
        val rolNormalizado = usuario.rol.trim().uppercase()
        rolNormalizado == "RESIDENTE" || rolNormalizado.contains("RESIDENTE")
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

    // Cargar residentes al iniciar siempre para evitar usar listas de otras pantallas.
    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerResidentes()
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

        // Recibo ENEL
        ReciboItemExpandible(
            logoResId = R.drawable.logoenel,
            nombreRecibo = "ENEL",
            expanded = enelExpanded,
            onExpandClick = { enelExpanded = !enelExpanded },
            residentes = residentes,
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
                    enelSeleccionados = residentes.mapNotNull { it.id }.toSet()
                } else {
                    enelSeleccionados = emptySet()
                }
            },
            onEnviarNotificaciones = {
                scope.launch {
                    try {
                        val destinatarios = if (enelTodosSeleccionado) residentes
                        else residentes.filter { it.id in enelSeleccionados }
                        val enviadas = enviarRecibosCelador(context, notificacionViewModel, destinatarios, "ENEL")
                        if (enviadas < 0) return@launch
                        enelNotificado = enviadas > 0
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Recibos", "Error al enviar notificaciones de ENEL", e)
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tieneNotificacion = enelNotificado
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recibo VANTI
        ReciboItemExpandible(
            logoResId = R.drawable.logovanti,
            nombreRecibo = "VANTI",
            expanded = vantiExpanded,
            onExpandClick = { vantiExpanded = !vantiExpanded },
            residentes = residentes,
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
                    vantiSeleccionados = residentes.mapNotNull { it.id }.toSet()
                } else {
                    vantiSeleccionados = emptySet()
                }
            },
            onEnviarNotificaciones = {
                scope.launch {
                    try {
                        val destinatarios = if (vantiTodosSeleccionado) residentes
                        else residentes.filter { it.id in vantiSeleccionados }
                        val enviadas = enviarRecibosCelador(context, notificacionViewModel, destinatarios, "VANTI")
                        if (enviadas < 0) return@launch
                        vantiNotificado = enviadas > 0
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Recibos", "Error al enviar notificaciones de VANTI", e)
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tieneNotificacion = vantiNotificado
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recibo EPZ
        ReciboItemExpandible(
            logoResId = R.drawable.logoepz,
            nombreRecibo = "EPZ",
            expanded = epzExpanded,
            onExpandClick = { epzExpanded = !epzExpanded },
            residentes = residentes,
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
                    epzSeleccionados = residentes.mapNotNull { it.id }.toSet()
                } else {
                    epzSeleccionados = emptySet()
                }
            },
            onEnviarNotificaciones = {
                scope.launch {
                    try {
                        val destinatarios = if (epzTodosSeleccionado) residentes
                        else residentes.filter { it.id in epzSeleccionados }
                        val enviadas = enviarRecibosCelador(context, notificacionViewModel, destinatarios, "EPZ")
                        if (enviadas < 0) return@launch
                        epzNotificado = enviadas > 0
                        Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("Recibos", "Error al enviar notificaciones de EPZ", e)
                        Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tieneNotificacion = epzNotificado
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
    tieneNotificacion: Boolean
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

                Divider(color = GrisClaro.copy(alpha = 0.3f), thickness = 1.dp)
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

private suspend fun enviarRecibosCelador(
    context: android.content.Context,
    notificacionViewModel: NotificacionViewModel,
    destinatarios: List<Usuario>,
    tipoRecibo: String
): Int {
    val validos = destinatarios.filter { it.id != null }
    if (validos.isEmpty()) {
        Toast.makeText(context, "Por favor selecciona al menos un residente", Toast.LENGTH_SHORT).show()
        return -1
    }
    var enviadas = 0
    for (residente in validos) {
        try {
            notificacionViewModel.enviarRecibo(residente.id!!, tipoRecibo)
            delay(800)
            enviadas++
            android.util.Log.d("Recibos", "Recibo $tipoRecibo enviado a ${residente.nombre} (ID: ${residente.id})")
        } catch (e: Exception) {
            android.util.Log.e("Recibos", "Error enviando $tipoRecibo a ${residente.nombre}", e)
        }
    }
    notificacionViewModel.obtenerTodos()
    return enviadas
}