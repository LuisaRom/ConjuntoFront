package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaQuejasCelador(
    navController: NavController,
    quejaViewModel: QuejaViewModel = hiltViewModel()
) {
    val quejas by quejaViewModel.quejas.collectAsState()
    val isLoading by quejaViewModel.isLoading.collectAsState()
    val error by quejaViewModel.error.collectAsState()
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }

    LaunchedEffect(categoriaSeleccionada) {
        if (categoriaSeleccionada == "Todas") {
            quejaViewModel.obtenerTodos()
        } else {
            // Requerimiento: consumir con ?categoria=
            quejaViewModel.obtenerPorCategoria(categoriaSeleccionada)
        }
    }

    val categorias = listOf("Todas", "Ruido", "Mascota", "Violencia")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
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
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("${quejas.size} Quejas", style = MaterialTheme.typography.bodyMedium, color = Color.White)

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            categorias.forEach { categoria ->
                FilterChip(
                    selected = categoriaSeleccionada == categoria,
                    onClick = { categoriaSeleccionada = categoria },
                    label = { Text(categoria) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else if (quejas.isEmpty()) {
            Text("Sin quejas registradas", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                items(quejas, key = { it.id ?: it.hashCode().toLong() }) { queja ->
                    QuejaItem(queja = queja, icono = iconoPorCategoria(queja.categoriaVisual()))
                }
            }
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
fun QuejaItem(queja: Queja, icono: ImageVector) {
    val enProcesoActivo = queja.estado.equals("en proceso", ignoreCase = true)
    val finalizadoActivo = queja.estado.equals("finalizado", ignoreCase = true) ||
        queja.estado.equals("finalizada", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = queja.categoriaVisual(),
                    tint = DoradoElegante,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = queja.categoriaVisual(),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Detalle: ${queja.detalleVisual()}",
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Usuario: ${queja.usuario?.nombre ?: queja.usuario?.usuario ?: "Sin usuario"}",
                color = GrisClaro,
                fontSize = 12.sp
            )
            Text(
                text = "Fecha: ${queja.fechaCreacion ?: "No disponible"}",
                color = GrisClaro,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enProcesoActivo) DoradoElegante else GrisClaro
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "En proceso",
                        color = if (enProcesoActivo) AzulOscuro else Color.DarkGray,
                        fontWeight = if (enProcesoActivo) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (finalizadoActivo) DoradoElegante else GrisClaro
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Finalizado",
                        color = if (finalizadoActivo) AzulOscuro else Color.DarkGray,
                        fontWeight = if (finalizadoActivo) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun iconoPorCategoria(categoria: String): ImageVector {
    return when (categoria.trim().lowercase()) {
        "ruido" -> Icons.AutoMirrored.Filled.VolumeUp
        "mascota" -> Icons.Default.Pets
        "violencia" -> Icons.Default.ReportProblem
        else -> Icons.Default.ReportProblem
    }
}
