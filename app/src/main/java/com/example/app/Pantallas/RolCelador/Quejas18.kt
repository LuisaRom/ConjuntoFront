package com.example.app.Pantallas.RolCelador

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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

@Composable
fun PantallaQuejasCelador(
    navController: NavController,
    quejaViewModel: QuejaViewModel = hiltViewModel()
) {
    val quejas by quejaViewModel.quejas.collectAsState()
    val isLoading by quejaViewModel.isLoading.collectAsState()
    val error by quejaViewModel.error.collectAsState()
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

        Spacer(modifier = Modifier.height(16.dp))

        Text("${quejas.size} Quejas", style = MaterialTheme.typography.bodyMedium, color = Color.White)

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading && quejas.isEmpty()) {
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
                    QuejaItem(
                        queja = queja,
                        icono = iconoPorCategoria(queja.categoriaVisual()),
                        onClick = { quejaSeleccionada = queja }
                    )
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

    quejaSeleccionada?.let { queja ->
        AlertDialog(
            onDismissRequest = { quejaSeleccionada = null },
            title = { Text("Detalle de queja", color = Color.White) },
            text = {
                Column {
                    Text("Categoría: ${queja.categoriaVisual()}", color = Color.White)
                    Text("Estado: ${queja.estado}", color = Color.White)
                    Text("Fecha: ${queja.fechaCreacion ?: "No disponible"}", color = Color.White)
                    Text("Usuario: ${queja.usuario?.nombre ?: queja.usuario?.usuario ?: "Sin usuario"}", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(queja.detalleVisual(), color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { quejaSeleccionada = null }) {
                    Text("Cerrar", color = DoradoElegante)
                }
            },
            containerColor = AzulOscuro
        )
    }
}


@Composable
fun QuejaItem(queja: Queja, icono: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3557))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
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
                text = queja.detalleVisual(),
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2
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
