@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.Pantallas.RolAdministrador

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.ConocimientoIA
import com.example.app.ViewModel.GestionIaViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro

private val CATEGORIAS_IA = listOf("GENERAL", "RESERVAS", "PAGOS", "QUEJAS", "PAQUETERIA", "ACCESOS", "MASCOTAS", "CONVIVENCIA")

@Composable
fun PantallaGestionIA(
    navController: NavController,
    viewModel: GestionIaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val aviso by viewModel.aviso.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    var pestana by remember { mutableIntStateOf(0) }
    var dialogoConocimiento by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(aviso) {
        aviso?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarAviso()
        }
    }
    LaunchedEffect(pestana) {
        when (pestana) {
            0 -> if (viewModel.resumen.value == null) viewModel.cargarResumen()
            1 -> viewModel.cargarConocimiento()
            3 -> viewModel.cargarNegativos()
        }
    }

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
            Text("Gestión con IA", style = MaterialTheme.typography.headlineSmall.copy(color = GrisClaro), modifier = Modifier.weight(1f))
            if (cargando) {
                CircularProgressIndicator(color = DoradoElegante, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            }
        }

        ScrollableTabRow(
            selectedTabIndex = pestana,
            containerColor = AzulOscuro,
            contentColor = DoradoElegante,
            edgePadding = 0.dp
        ) {
            listOf("Resumen", "Conocimiento", "Comunicados", "Mejorar").forEachIndexed { i, titulo ->
                Tab(
                    selected = pestana == i,
                    onClick = { pestana = i },
                    text = { Text(titulo, color = if (pestana == i) DoradoElegante else GrisClaro) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Box(Modifier.weight(1f)) {
            when (pestana) {
                0 -> TabResumen(viewModel)
                1 -> TabConocimiento(viewModel, onAgregar = { dialogoConocimiento = "" })
                2 -> TabComunicados(viewModel)
                3 -> TabMejorar(viewModel, onEnsenar = { dialogoConocimiento = it })
            }
        }
    }

    dialogoConocimiento?.let { tituloInicial ->
        DialogoConocimiento(
            tituloInicial = tituloInicial,
            onCerrar = { dialogoConocimiento = null },
            onGuardar = { titulo, contenido, categoria ->
                viewModel.crearConocimiento(titulo, contenido, categoria)
                dialogoConocimiento = null
            }
        )
    }
}

@Composable
private fun TabResumen(viewModel: GestionIaViewModel) {
    val resumen by viewModel.resumen.collectAsState()
    val m = resumen?.metricas.orEmpty()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metrica("Residentes", numero(m["residentes"]), Modifier.weight(1f))
            Metrica("Quejas pendientes", numero(subvalor(m["quejasPorEstado"], "PENDIENTE")), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metrica("Prioridad alta", numero(m["quejasAbiertasPrioridadAlta"]), Modifier.weight(1f))
            Metrica("Paquetes pendientes", numero(m["paquetesPendientes"]), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metrica("Sin pago del mes", numero(m["residentesEnMoraMes"]), Modifier.weight(1f))
            Metrica("Recaudo del mes", "$" + numero(m["recaudoMes"]), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DoradoElegante)
            Spacer(Modifier.width(6.dp))
            Text("Análisis", color = GrisClaro, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.cargarResumen() }) { Text("Actualizar", color = DoradoElegante) }
        }
        Tarjeta {
            SelectionContainer {
                Text(
                    text = resumen?.analisis ?: "Generando análisis del conjunto...",
                    color = GrisClaro,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
        if (resumen?.generadoPorIA == false) {
            Text(
                "Resumen automático básico. Configure la clave de IA en el servidor para un análisis completo.",
                color = GrisClaro.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun TabConocimiento(viewModel: GestionIaViewModel, onAgregar: () -> Unit) {
    val lista by viewModel.conocimiento.collectAsState()
    val filtro by viewModel.filtroEstado.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Text(
            "Lo que el asistente sabe del conjunto. Lo aprendido de residentes queda pendiente hasta que lo apruebes.",
            color = GrisClaro.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("PENDIENTE", "APROBADO", "TODOS").forEach { estado ->
                FilterChip(
                    selected = filtro == estado,
                    onClick = { viewModel.cambiarFiltro(estado) },
                    label = { Text(estado.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = GrisClaro,
                        selectedContainerColor = DoradoElegante,
                        selectedLabelColor = AzulOscuro
                    )
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onAgregar) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = DoradoElegante)
            }
        }
        if (lista.isEmpty()) {
            Text("No hay registros en este estado.", color = GrisClaro.copy(alpha = 0.6f), modifier = Modifier.padding(16.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lista, key = { it.id ?: 0L }) { c -> ItemConocimiento(c, viewModel) }
        }
    }
}

@Composable
private fun ItemConocimiento(c: ConocimientoIA, viewModel: GestionIaViewModel) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(c.titulo.orEmpty(), color = GrisClaro, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                c.estado.orEmpty(),
                color = when (c.estado) {
                    "APROBADO" -> Color(0xFF81C784)
                    "RECHAZADO" -> Color(0xFFFF8A80)
                    else -> DoradoElegante
                },
                fontSize = 11.sp
            )
        }
        Text(c.contenido.orEmpty(), color = GrisClaro.copy(alpha = 0.85f), fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                listOfNotNull(c.categoria, c.origen?.lowercase(), c.creadoPorNombre, "usos: ${c.usos ?: 0}").joinToString(" · "),
                color = GrisClaro.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            val id = c.id ?: return@Row
            if (c.estado == "PENDIENTE") {
                IconButton(onClick = { viewModel.aprobar(id) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Check, contentDescription = "Aprobar", tint = Color(0xFF81C784))
                }
                IconButton(onClick = { viewModel.rechazar(id) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = Color(0xFFFF8A80))
                }
            }
            IconButton(onClick = { viewModel.eliminar(id) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = GrisClaro.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun TabComunicados(viewModel: GestionIaViewModel) {
    val comunicado by viewModel.comunicado.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var tema by remember { mutableStateOf("") }
    var detalles by remember { mutableStateOf("") }
    var tono by remember { mutableStateOf("cordial") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CampoTexto(tema, { tema = it }, "Tema (ej. corte de agua el sábado)")
        Spacer(Modifier.height(8.dp))
        CampoTexto(detalles, { detalles = it }, "Detalles opcionales (horario, lugar, recomendaciones)", minLineas = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("cordial", "formal", "urgente").forEach { t ->
                FilterChip(
                    selected = tono == t,
                    onClick = { tono = t },
                    label = { Text(t.replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = GrisClaro,
                        selectedContainerColor = DoradoElegante,
                        selectedLabelColor = AzulOscuro
                    )
                )
            }
        }
        Button(
            onClick = { viewModel.redactarComunicado(tema, tono, detalles) },
            enabled = tema.isNotBlank() && !cargando,
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante, contentColor = AzulOscuro),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Redactar con IA")
        }
        comunicado?.let { texto ->
            Spacer(Modifier.height(12.dp))
            Tarjeta {
                SelectionContainer { Text(texto, color = GrisClaro, fontSize = 14.sp, lineHeight = 20.sp) }
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(texto))
                    Toast.makeText(context, "Copiado. Pégalo en una nueva publicación.", Toast.LENGTH_SHORT).show()
                }) { Text("Copiar texto", color = DoradoElegante) }
            }
        }
    }
}

@Composable
private fun TabMejorar(viewModel: GestionIaViewModel, onEnsenar: (String) -> Unit) {
    val negativos by viewModel.negativos.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Text(
            "Preguntas cuyas respuestas fueron calificadas como no útiles. Enséñale al asistente la respuesta correcta.",
            color = GrisClaro.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (negativos.isEmpty()) {
            Text("No hay respuestas mal calificadas.", color = GrisClaro.copy(alpha = 0.6f), modifier = Modifier.padding(16.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(negativos) { n ->
                Tarjeta {
                    Text(n.pregunta.orEmpty(), color = GrisClaro, fontWeight = FontWeight.Bold)
                    Text(n.respuesta.orEmpty(), color = GrisClaro.copy(alpha = 0.7f), fontSize = 13.sp, maxLines = 4,
                        modifier = Modifier.padding(vertical = 4.dp))
                    TextButton(onClick = { onEnsenar(n.pregunta.orEmpty()) }) {
                        Text("Enseñar respuesta correcta", color = DoradoElegante)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoConocimiento(
    tituloInicial: String,
    onCerrar: () -> Unit,
    onGuardar: (String, String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(tituloInicial) }
    var contenido by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("GENERAL") }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = GrisOscuro,
        title = { Text("Enseñar al asistente", color = GrisClaro) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CampoTexto(titulo, { titulo = it }, "Título o pregunta")
                Spacer(Modifier.height(8.dp))
                CampoTexto(contenido, { contenido = it }, "Respuesta o norma", minLineas = 4)
                Spacer(Modifier.height(8.dp))
                Text("Categoría", color = GrisClaro.copy(alpha = 0.7f), fontSize = 12.sp)
                CATEGORIAS_IA.chunked(3).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        fila.forEach { cat ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = { categoria = cat },
                                label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    labelColor = GrisClaro,
                                    selectedContainerColor = DoradoElegante,
                                    selectedLabelColor = AzulOscuro
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(titulo.trim(), contenido.trim(), categoria) },
                enabled = titulo.isNotBlank() && contenido.isNotBlank()
            ) { Text("Guardar", color = DoradoElegante) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar", color = GrisClaro) }
        }
    )
}

@Composable
private fun Metrica(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(12.dp)
    ) {
        Text(valor, color = DoradoElegante, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(titulo, color = GrisClaro.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun Tarjeta(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(12.dp),
        content = contenido
    )
}

@Composable
private fun CampoTexto(valor: String, onCambio: (String) -> Unit, etiqueta: String, minLineas: Int = 1) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        minLines = minLineas,
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
}

private fun subvalor(mapa: Any?, clave: String): Any? = (mapa as? Map<*, *>)?.get(clave)

private fun numero(valor: Any?): String = when (valor) {
    null -> "0"
    is Number -> {
        val d = valor.toDouble()
        if (d == Math.floor(d)) String.format("%,d", d.toLong()).replace(',', '.') else d.toString()
    }
    else -> valor.toString()
}
