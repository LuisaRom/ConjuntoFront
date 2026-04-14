package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

@OptIn(ExperimentalLayoutApi::class)
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
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    var quejaAFinalizar by remember { mutableStateOf<Queja?>(null) }

    LaunchedEffect(Unit) {
        quejaViewModel.obtenerTodos()
    }

    val categoriasDisponibles = remember(quejas) {
        listOf("Todas") + quejas
            .map { it.categoriaVisual() }
            .distinct()
            .sorted()
    }
    val quejasFiltradas = remember(quejas, categoriaSeleccionada) {
        if (categoriaSeleccionada == "Todas") quejas
        else quejas.filter { it.categoriaVisual() == categoriaSeleccionada }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
    ) {
        SnackbarHost(hostState = snackbarHostState)
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
            Text(
                text = "Quejas",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "${quejasFiltradas.size} Quejas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriasDisponibles.forEach { categoria ->
                FilterChip(
                    selected = categoriaSeleccionada == categoria,
                    onClick = { categoriaSeleccionada = categoria },
                    label = { Text(categoria) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading && quejas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (quejasFiltradas.isEmpty()) {
                    Text("Sin quejas registradas", color = Color.Gray, fontSize = 14.sp)
                } else {
                    quejasFiltradas.forEach { queja ->
                        QuejaItem(
                            queja = queja,
                            onMarcarFinalizada = { quejaAFinalizar = queja }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                error?.let {
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

    if (quejaAFinalizar != null) {
        AlertDialog(
            onDismissRequest = { quejaAFinalizar = null },
            title = { Text("Finalizar queja", color = Color.White) },
            text = { Text("¿Deseas marcar esta queja como finalizada?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    val id = quejaAFinalizar?.id
                    if (id != null) {
                        quejaViewModel.finalizarQueja(id) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Queja finalizada correctamente.")
                            }
                        }
                    }
                    quejaAFinalizar = null
                }) {
                    Text("Finalizar", color = DoradoElegante)
                }
            },
            dismissButton = {
                TextButton(onClick = { quejaAFinalizar = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }
}

@Composable
fun QuejaItem(
    queja: Queja,
    onMarcarFinalizada: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Categoría: ${queja.categoriaVisual()}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Estado: ${queja.estado}",
                color = GrisClaro,
                fontSize = 12.sp
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
                text = "Detalle: ${queja.detalleVisual()}",
                color = Color.White,
                fontSize = 14.sp
            )

            val finalizada = queja.estado.equals("finalizada", ignoreCase = true)
            if (!finalizada && queja.id != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onMarcarFinalizada,
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Finalizar queja",
                        tint = AzulOscuro
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar como finalizada", color = AzulOscuro)
                }
            }
        }
    }
}
