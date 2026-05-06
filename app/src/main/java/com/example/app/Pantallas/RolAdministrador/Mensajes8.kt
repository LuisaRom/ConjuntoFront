package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.Auth.AuthManager
import com.example.app.Model.Notificacion
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMensajes(
    nombre: String,
    navController: NavController,
    notificacionViewModel: NotificacionViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuarioActual by AuthManager.currentUser.collectAsState()
    val usuarioActualVm by usuarioViewModel.usuarioActual.collectAsState()
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val notificaciones by notificacionViewModel.notificaciones.collectAsState()
    var textoMensaje by remember { mutableStateOf("") }

    val receptor = remember(usuarios, nombre) {
        usuarios.firstOrNull { it.nombre.equals(nombre, true) || it.usuario.equals(nombre, true) }
    }

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerTodos()
        notificacionViewModel.obtenerTodos()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            notificacionViewModel.obtenerTodosSilencioso()
        }
    }

    val usuarioActualId = usuarioActual?.id ?: usuarioActualVm?.id

    val mensajesChat = remember(notificaciones, usuarioActualId, receptor?.id, usuarios) {
        filtrarMensajesChat(
            notificaciones = notificaciones,
            emisorActualId = usuarioActualId,
            receptorId = receptor?.id,
            usuarios = usuarios
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = nombre, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AzulOscuro
                )
            )
        },
        containerColor = AzulOscuro
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(AzulOscuro)
        ) {
            if (receptor == null || usuarioActualId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se pudo cargar el chat.",
                        color = GrisClaro,
                        textAlign = TextAlign.Center
                    )
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mensajesChat, key = { it.id ?: it.hashCode().toLong() }) { mensaje ->
                    val esMio = extraerEmisorId(mensaje.mensaje) == usuarioActualId
                    val texto = extraerTextoChat(mensaje.mensaje)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
                    ) {
                        Text(
                            text = texto,
                            color = if (esMio) AzulOscuro else Color.White,
                            modifier = Modifier
                                .background(
                                    if (esMio) DoradoElegante else Color.White.copy(alpha = 0.10f),
                                    androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoMensaje,
                    onValueChange = { textoMensaje = it },
                    placeholder = { Text("Escribe un mensaje...", color = GrisClaro) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = GrisClaro,
                        focusedBorderColor = DoradoElegante,
                        cursorColor = DoradoElegante,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (textoMensaje.isBlank()) return@IconButton
                    val payload = construirPayloadChat(
                        emisorId = usuarioActualId,
                        receptorId = receptor.id ?: return@IconButton,
                        texto = textoMensaje.trim()
                    )
                    notificacionViewModel.guardar(
                        Notificacion(
                            mensaje = payload,
                            fechaEnvio = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                            usuario = receptor
                        )
                    )
                    textoMensaje = ""
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = DoradoElegante)
                }
            }
        }
    }
}

private fun construirPayloadChat(emisorId: Long, receptorId: Long, texto: String): String {
    return "CHAT|from=$emisorId|to=$receptorId|msg=${texto.replace("|", "/")}"
}

private fun esMensajeChat(payload: String?): Boolean {
    return payload?.startsWith("CHAT|") == true
}

private fun extraerEmisorId(payload: String?): Long? {
    if (!esMensajeChat(payload)) return null
    return payload
        ?.split("|")
        ?.firstOrNull { it.startsWith("from=") }
        ?.substringAfter("from=")
        ?.toLongOrNull()
}

private fun extraerReceptorId(payload: String?): Long? {
    if (!esMensajeChat(payload)) return null
    return payload
        ?.split("|")
        ?.firstOrNull { it.startsWith("to=") }
        ?.substringAfter("to=")
        ?.toLongOrNull()
}

private fun extraerTextoChat(payload: String?): String {
    if (!esMensajeChat(payload)) return payload.orEmpty()
    return payload
        ?.split("|")
        ?.firstOrNull { it.startsWith("msg=") }
        ?.substringAfter("msg=")
        .orEmpty()
}

private fun filtrarMensajesChat(
    notificaciones: List<Notificacion>,
    emisorActualId: Long?,
    receptorId: Long?,
    usuarios: List<com.example.app.Model.Usuario>
): List<Notificacion> {
    if (emisorActualId == null || receptorId == null) return emptyList()
    val usuarioActual = usuarios.firstOrNull { it.id == emisorActualId }
    val usuarioReceptor = usuarios.firstOrNull { it.id == receptorId }
    val rolesValidos = setOf("ADMINISTRADOR", "CELADOR")
    val chatPermitido = usuarioActual?.rol?.uppercase() in rolesValidos &&
        usuarioReceptor?.rol?.uppercase() in rolesValidos
    if (!chatPermitido) return emptyList()

    return notificaciones.filter { noti ->
        val from = extraerEmisorId(noti.mensaje)
        val to = extraerReceptorId(noti.mensaje)
        val participantesCorrectos =
            (from == emisorActualId && to == receptorId) || (from == receptorId && to == emisorActualId)
        if (!participantesCorrectos) return@filter false
        val rolFrom = usuarios.firstOrNull { it.id == from }?.rol?.uppercase()
        val rolTo = usuarios.firstOrNull { it.id == to }?.rol?.uppercase()
        rolFrom in rolesValidos && rolTo in rolesValidos
    }
}
