package com.example.app.Pantallas.RolCelador

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    var enelExpanded by remember { mutableStateOf(false) }
    var vantiExpanded by remember { mutableStateOf(false) }
    var epzExpanded by remember { mutableStateOf(false) }

    var enelModo by remember { mutableStateOf("TODOS") }
    var vantiModo by remember { mutableStateOf("TODOS") }
    var epzModo by remember { mutableStateOf("TODOS") }

    var enelResidenteId by remember { mutableStateOf<Long?>(null) }
    var vantiResidenteId by remember { mutableStateOf<Long?>(null) }
    var epzResidenteId by remember { mutableStateOf<Long?>(null) }

    var enelEnviando by remember { mutableStateOf(false) }
    var vantiEnviando by remember { mutableStateOf(false) }
    var epzEnviando by remember { mutableStateOf(false) }

    var enelEnviado by remember { mutableStateOf(false) }
    var vantiEnviado by remember { mutableStateOf(false) }
    var epzEnviado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (usuarios.isEmpty()) usuarioViewModel.obtenerTodos()
    }

    fun enviar(tipo: String, modo: String, residenteId: Long?, setEnviando: (Boolean) -> Unit, setEnviado: (Boolean) -> Unit) {
        scope.launch {
            if (modo == "INDIVIDUAL" && residenteId == null) {
                Toast.makeText(context, "Selecciona un residente", Toast.LENGTH_SHORT).show()
                return@launch
            }
            setEnviando(true)
            try {
                val destinatarios = if (modo == "TODOS") residentes else residentes.filter { it.id == residenteId }
                if (destinatarios.isEmpty()) {
                    Toast.makeText(context, "No hay residentes para notificar", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fechaActual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                val mensaje = "Tienes un recibo de $tipo disponible en la portería. Por favor pasa a recogerlo."
                var enviadas = 0
                destinatarios.forEach { residente ->
                    if (residente.id != null) {
                        try {
                            notificacionViewModel.guardar(
                                Notificacion(
                                    mensaje = mensaje,
                                    fechaEnvio = fechaActual,
                                    usuario = residente
                                )
                            )
                            delay(250)
                            enviadas++
                        } catch (_: Exception) {
                        }
                    }
                }
                notificacionViewModel.obtenerTodos()
                setEnviado(true)
                Toast.makeText(context, "Notificaciones enviadas a $enviadas residente(s)", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al enviar notificaciones: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setEnviando(false)
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
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recibos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        ReciboItemExpandible(
            logoResId = R.drawable.logoenel,
            nombreRecibo = "ENEL",
            expanded = enelExpanded,
            onExpandClick = { enelExpanded = !enelExpanded },
            residentes = residentes,
            modo = enelModo,
            onModoChange = { enelModo = it },
            residenteSeleccionadoId = enelResidenteId,
            onResidenteSeleccionado = { enelResidenteId = it },
            onEnviarNotificaciones = {
                enviar("ENEL", enelModo, enelResidenteId, { enelEnviando = it }, { enelEnviado = it })
            },
            enviado = enelEnviado,
            enviando = enelEnviando
        )

        Spacer(modifier = Modifier.height(12.dp))

        ReciboItemExpandible(
            logoResId = R.drawable.logovanti,
            nombreRecibo = "VANTI",
            expanded = vantiExpanded,
            onExpandClick = { vantiExpanded = !vantiExpanded },
            residentes = residentes,
            modo = vantiModo,
            onModoChange = { vantiModo = it },
            residenteSeleccionadoId = vantiResidenteId,
            onResidenteSeleccionado = { vantiResidenteId = it },
            onEnviarNotificaciones = {
                enviar("VANTI", vantiModo, vantiResidenteId, { vantiEnviando = it }, { vantiEnviado = it })
            },
            enviado = vantiEnviado,
            enviando = vantiEnviando
        )

        Spacer(modifier = Modifier.height(12.dp))

        ReciboItemExpandible(
            logoResId = R.drawable.logoepz,
            nombreRecibo = "EPZ",
            expanded = epzExpanded,
            onExpandClick = { epzExpanded = !epzExpanded },
            residentes = residentes,
            modo = epzModo,
            onModoChange = { epzModo = it },
            residenteSeleccionadoId = epzResidenteId,
            onResidenteSeleccionado = { epzResidenteId = it },
            onEnviarNotificaciones = {
                enviar("EPZ", epzModo, epzResidenteId, { epzEnviando = it }, { epzEnviado = it })
            },
            enviado = epzEnviado,
            enviando = epzEnviando
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciboItemExpandible(
    logoResId: Int,
    nombreRecibo: String,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    residentes: List<Usuario>,
    modo: String,
    onModoChange: (String) -> Unit,
    residenteSeleccionadoId: Long?,
    onResidenteSeleccionado: (Long?) -> Unit,
    onEnviarNotificaciones: () -> Unit,
    enviado: Boolean,
    enviando: Boolean
) {
    var expandirResidentes by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onExpandClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = logoResId),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombreRecibo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (expanded) {
                        Text("Modo: $modo", color = GrisClaro, fontSize = 12.sp)
                    }
                }
                if (enviado) {
                    Text(
                        "Enviado",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DoradoElegante
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = modo == "TODOS",
                        onClick = { onModoChange("TODOS") },
                        label = { Text("CHECKLIST TODOS") }
                    )
                    FilterChip(
                        selected = modo == "INDIVIDUAL",
                        onClick = { onModoChange("INDIVIDUAL") },
                        label = { Text("CHECKLIST INDIVIDUAL") }
                    )
                }

                if (modo == "INDIVIDUAL") {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandirResidentes,
                        onExpandedChange = { expandirResidentes = !expandirResidentes }
                    ) {
                        val etiqueta = residentes.firstOrNull { it.id == residenteSeleccionadoId }?.let {
                            "${it.nombre.ifBlank { it.usuario }} - ${it.torre} - ${it.apartamento}"
                        } ?: ""
                        OutlinedTextField(
                            value = etiqueta,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Residente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirResidentes) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DoradoElegante,
                                unfocusedBorderColor = GrisClaro
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandirResidentes,
                            onDismissRequest = { expandirResidentes = false }
                        ) {
                            residentes.forEach { residente ->
                                DropdownMenuItem(
                                    text = { Text("${residente.nombre.ifBlank { residente.usuario }} - ${residente.torre} - ${residente.apartamento}") },
                                    onClick = {
                                        onResidenteSeleccionado(residente.id)
                                        expandirResidentes = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEnviarNotificaciones,
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !enviando && (modo == "TODOS" || residenteSeleccionadoId != null)
                ) {
                    Text(if (enviando) "Enviando..." else "Enviar notificación", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
