package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Queja
import com.example.app.ViewModel.QuejaViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.launch

@Composable
fun PantallaQuejas(
    navController: NavController,
    quejaViewModel: QuejaViewModel = hiltViewModel()
) {
    val quejas by quejaViewModel.quejas.collectAsState()
    val isLoading by quejaViewModel.isLoading.collectAsState()
    val error by quejaViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var quejaSeleccionada by remember { mutableStateOf<Queja?>(null) }

    LaunchedEffect(Unit) {
        quejaViewModel.obtenerTodos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
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
            Text(
                text = "Quejas",
                style = MaterialTheme.typography.headlineMedium,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "${quejas.size} Quejas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading && quejas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                if (quejas.isEmpty()) {
                    item {
                        Text("Sin quejas registradas", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    items(quejas, key = { it.id ?: it.hashCode().toLong() }) { queja ->
                        QuejaItem(queja = queja, onClick = { quejaSeleccionada = queja })
                    }
                }

                error?.let {
                    item {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    if (quejaSeleccionada != null) {
        val queja = quejaSeleccionada!!
        val finalizada =
            queja.estado.equals("finalizada", ignoreCase = true) ||
                queja.estado.equals("finalizado", ignoreCase = true) ||
                queja.estado.equals("resuelto", ignoreCase = true) ||
                queja.estado.equals("resuelta", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { quejaSeleccionada = null },
            title = { Text("Detalle de queja", color = Color.White) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = iconoPorCategoria(queja.categoriaVisual()),
                            contentDescription = null,
                            tint = DoradoElegante,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Categoría: ${queja.categoriaVisual()}", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Estado: ${estadoVisualAdmin(queja.estado)}", color = Color.White)
                    Text("Fecha: ${queja.fechaCreacion ?: "No disponible"}", color = Color.White)
                    Text("Usuario: ${queja.usuario?.nombre ?: queja.usuario?.usuario ?: "Sin usuario"}", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Detalle:", color = GrisClaro)
                    Text(queja.detalleVisual(), color = Color.White)
                }
            },
            confirmButton = {
                if (!finalizada && queja.id != null) {
                    TextButton(onClick = {
                        quejaViewModel.finalizarQueja(queja.id) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Queja finalizada correctamente.")
                            }
                        }
                        quejaSeleccionada = null
                    }) {
                        Text("Finalizar", color = DoradoElegante)
                    }
                } else {
                    TextButton(onClick = { quejaSeleccionada = null }) {
                        Text("Cerrar", color = DoradoElegante)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { quejaSeleccionada = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }
}

private fun normalizarCategoria(categoria: String): String {
    val valor = categoria.trim().lowercase()
    return when {
        valor.contains("ruido") -> "Ruido"
        valor.contains("mascota") -> "Mascota"
        valor.contains("violencia") -> "Violencia"
        else -> categoria
    }
}

@Composable
fun QuejaItem(
    queja: Queja,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3557)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = iconoPorCategoria(queja.categoriaVisual()),
                    contentDescription = "Tipo de queja",
                    tint = DoradoElegante,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = queja.categoriaVisual(),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "Estado:",
                color = GrisClaro,
                fontSize = 12.sp
            )
            Text(
                text = estadoVisualAdmin(queja.estado),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 2.dp)
                    .background(
                        color = colorEstadoQueja(queja.estado),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            Text(
                text = "Fecha: ${queja.fechaCreacion ?: "No disponible"}",
                color = GrisClaro,
                fontSize = 12.sp
            )
            Text(
                text = "Usuario: ${queja.usuario?.nombre ?: queja.usuario?.usuario ?: "Sin usuario"}",
                color = GrisClaro,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = queja.detalleVisual(),
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2
            )
        }
    }
}

private fun estadoVisualAdmin(estado: String?): String {
    val valor = estado?.trim().orEmpty()
    if (valor.isBlank()) return "Pendiente"
    return when {
        valor.equals("finalizada", ignoreCase = true) || valor.equals("finalizado", ignoreCase = true) -> "Resuelto"
        else -> valor
    }
}

private fun colorEstadoQueja(estado: String?): Color {
    return if (
        estado.equals("finalizada", ignoreCase = true) ||
        estado.equals("finalizado", ignoreCase = true)
    ) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFB26A00)
    }
}

private fun iconoPorCategoria(categoria: String): ImageVector {
    return when (normalizarCategoria(categoria)) {
        "Ruido" -> Icons.Default.RecordVoiceOver
        "Mascota" -> Icons.Default.Pets
        else -> Icons.Default.Warning
    }
}
