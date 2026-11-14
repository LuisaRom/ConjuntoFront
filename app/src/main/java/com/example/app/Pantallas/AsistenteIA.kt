package com.example.app.Pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Auth.AuthManager
import com.example.app.Model.MensajeIA
import com.example.app.ViewModel.AsistenteIaViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro

private fun sugerenciasPorRol(rol: String?): List<String> = when (rol?.uppercase()) {
    "ADMINISTRADOR", "ADMIN" -> listOf(
        "Dame un resumen del conjunto hoy",
        "¿Quiénes no han pagado la administración este mes?",
        "¿Qué quejas pendientes son prioritarias?",
        "¿Qué reservas hay esta semana?"
    )
    "CELADOR" -> listOf(
        "¿Qué paquetes están pendientes por entregar?",
        "¿Quién ha ingresado hoy?",
        "¿Qué reservas hay hoy?",
        "¿Hay quejas abiertas de ruido?"
    )
    else -> listOf(
        "¿Tengo paquetes pendientes?",
        "¿Cuál es el horario de la piscina?",
        "¿Estoy al día con la administración?",
        "¿En qué va mi queja?"
    )
}

@Composable
fun PantallaAsistenteIA(
    navController: NavController,
    viewModel: AsistenteIaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuario by AuthManager.currentUser.collectAsState()
    val mensajes by viewModel.mensajes.collectAsState()
    val conversaciones by viewModel.conversaciones.collectAsState()
    val escribiendo by viewModel.escribiendo.collectAsState()
    val error by viewModel.error.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    val estado by viewModel.estado.collectAsState()

    var texto by remember { mutableStateOf("") }
    var mostrarHistorial by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(mensajes.size, escribiendo) {
        val total = mensajes.size + if (escribiendo) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }
    LaunchedEffect(aviso) {
        aviso?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarAviso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = GrisClaro)
            }
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = DoradoElegante, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = estado?.nombreAsistente ?: "Asistente IA",
                    color = GrisClaro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (estado?.disponible == false) "Modo básico" else "En línea",
                    color = if (estado?.disponible == false) GrisClaro.copy(alpha = 0.6f) else DoradoElegante,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { viewModel.cargarConversaciones(); mostrarHistorial = true }) {
                Icon(Icons.Default.History, contentDescription = "Historial", tint = GrisClaro)
            }
            IconButton(onClick = { viewModel.nuevaConversacion() }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva conversación", tint = GrisClaro)
            }
        }

        if (estado?.disponible == false) {
            Text(
                text = "La IA no está configurada en el servidor. Respondo con la base de conocimiento del conjunto.",
                color = GrisClaro.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(8.dp)
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (mensajes.isEmpty() && !escribiendo) {
                Bienvenida(
                    nombre = usuario?.nombre,
                    sugerencias = sugerenciasPorRol(usuario?.rol),
                    onSugerencia = { viewModel.enviar(it) }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(mensajes) { mensaje ->
                        BurbujaMensaje(mensaje, onCalificar = { util ->
                            mensaje.id?.let { viewModel.calificar(it, util) }
                        })
                    }
                    if (escribiendo) {
                        item { IndicadorEscribiendo() }
                    }
                }
            }
        }

        error?.let {
            Text(
                text = it,
                color = Color(0xFFFF8A80),
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.limpiarError() }
                    .padding(vertical = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { if (it.length <= 1500) texto = it },
                placeholder = { Text("Escribe tu pregunta...", color = GrisClaro.copy(alpha = 0.5f)) },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GrisClaro,
                    unfocusedTextColor = GrisClaro,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro.copy(alpha = 0.3f),
                    cursorColor = DoradoElegante
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.enviar(texto)
                    texto = ""
                },
                enabled = texto.isNotBlank() && !escribiendo,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (texto.isNotBlank() && !escribiendo) DoradoElegante else Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = AzulOscuro)
            }
        }
    }

    if (mostrarHistorial) {
        AlertDialog(
            onDismissRequest = { mostrarHistorial = false },
            containerColor = GrisOscuro,
            title = { Text("Conversaciones", color = GrisClaro) },
            text = {
                if (conversaciones.isEmpty()) {
                    Text("Aún no tienes conversaciones.", color = GrisClaro.copy(alpha = 0.7f))
                } else {
                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        items(conversaciones) { c ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.abrirConversacion(c.id)
                                        mostrarHistorial = false
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(c.titulo ?: "Conversación", color = GrisClaro, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        c.fechaActualizacion?.take(16)?.replace("T", " ") ?: "",
                                        color = GrisClaro.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(onClick = { viewModel.eliminarConversacion(c.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = GrisClaro.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarHistorial = false }) { Text("Cerrar", color = DoradoElegante) }
            }
        )
    }
}

@Composable
private fun Bienvenida(nombre: String?, sugerencias: List<String>, onSugerencia: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = DoradoElegante, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Hola${nombre?.takeIf { it.isNotBlank() }?.let { ", ${it.substringBefore(" ")}" } ?: ""}",
            color = GrisClaro,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Pregúntame sobre pagos, paquetes, reservas, quejas o normas del conjunto.",
            color = GrisClaro.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )
        sugerencias.forEach { s ->
            Text(
                text = s,
                color = GrisClaro,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .clickable { onSugerencia(s) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun BurbujaMensaje(mensaje: MensajeIA, onCalificar: (Boolean) -> Unit) {
    val esUsuario = mensaje.esUsuario()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esUsuario) Alignment.End else Alignment.Start
    ) {
        Text(
            text = mensaje.contenido.orEmpty(),
            color = if (esUsuario) AzulOscuro else GrisClaro,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (esUsuario) 16.dp else 4.dp,
                        bottomEnd = if (esUsuario) 4.dp else 16.dp
                    )
                )
                .background(if (esUsuario) DoradoElegante else Color.White.copy(alpha = 0.09f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
        if (!esUsuario && mensaje.id != null) {
            Row(modifier = Modifier.padding(start = 4.dp)) {
                IconButton(onClick = { onCalificar(true) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (mensaje.util == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Útil",
                        tint = if (mensaje.util == true) DoradoElegante else GrisClaro.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = { onCalificar(false) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (mensaje.util == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "No útil",
                        tint = if (mensaje.util == false) DoradoElegante else GrisClaro.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicadorEscribiendo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.09f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        CircularProgressIndicator(color = DoradoElegante, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text("Consultando...", color = GrisClaro.copy(alpha = 0.7f), fontSize = 13.sp)
    }
}
