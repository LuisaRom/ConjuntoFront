package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.AccesoAdmin
import com.example.app.ViewModel.AccesoAdminViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaAccesos(
    navController: NavController,
    accesoAdminViewModel: AccesoAdminViewModel = hiltViewModel()
) {
    val accesos by accesoAdminViewModel.accesos.collectAsState()
    val isLoading by accesoAdminViewModel.isLoading.collectAsState()
    val error by accesoAdminViewModel.error.collectAsState()
    var accesoAEliminar by remember { mutableStateOf<AccesoAdmin?>(null) }
    var accesoDetalle by remember { mutableStateOf<AccesoAdmin?>(null) }

    LaunchedEffect(Unit) {
        accesoAdminViewModel.obtenerTodos()
    }

    val accesosVehiculares = accesos.filter { it.tipoVisual() == "Vehicular" }
    val accesosPeatonales = accesos.filter { it.tipoVisual() == "Peatonal" }

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
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Accesos",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Acceso Vehicular", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = DoradoElegante,
                modifier = Modifier.size(35.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${accesosVehiculares.size} accesos", color = Color.LightGray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading && accesos.isEmpty()) {
            CircularProgressIndicator(color = DoradoElegante)
        } else if (accesosVehiculares.isEmpty()) {
            Text("No hay accesos vehiculares", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(accesosVehiculares, key = { it.id ?: it.hashCode().toLong() }) { acceso ->
                    AccesoItem(
                        acceso = acceso,
                        onEliminar = { accesoAEliminar = acceso },
                        onVerDetalle = { accesoDetalle = acceso }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Acceso Peatonal", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                contentDescription = null,
                tint = DoradoElegante,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${accesosPeatonales.size} accesos", color = Color.LightGray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (accesosPeatonales.isEmpty()) {
            Text("No hay accesos peatonales", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(accesosPeatonales, key = { it.id ?: it.hashCode().toLong() }) { acceso ->
                    AccesoItem(
                        acceso = acceso,
                        onEliminar = { accesoAEliminar = acceso },
                        onVerDetalle = { accesoDetalle = acceso }
                    )
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red, fontSize = 13.sp)
        }
    }

    if (accesoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { accesoAEliminar = null },
            title = { Text("Confirmar eliminación", color = Color.White) },
            text = {
                Text(
                    text = "¿Deseas eliminar este acceso?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    accesoAEliminar?.id?.let { id ->
                        accesoAdminViewModel.eliminar(id)
                    }
                    accesoAEliminar = null
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { accesoAEliminar = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }

    accesoDetalle?.let { acceso ->
        AlertDialog(
            onDismissRequest = { accesoDetalle = null },
            title = { Text("Detalle del acceso", color = Color.White) },
            text = {
                Column {
                    Text("Tipo: ${acceso.tipoVisual()}", color = Color.White)
                    Text("Visitante: ${acceso.nombreVisitante ?: "-"}", color = Color.White)
                    Text("Placa: ${acceso.placaVehiculo ?: "-"}", color = Color.White)
                    Text("Torre: ${acceso.torre ?: "-"}", color = Color.White)
                    Text("Apartamento: ${acceso.apartamento ?: "-"}", color = Color.White)
                    Text("Fecha: ${acceso.horaAutorizada ?: "-"}", color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { accesoDetalle = null }) {
                    Text("Cerrar", color = DoradoElegante)
                }
            },
            containerColor = AzulOscuro
        )
    }
}

@Composable
fun AccesoItem(acceso: AccesoAdmin, onEliminar: () -> Unit, onVerDetalle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onVerDetalle() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = acceso.tituloVisual(),
                    color = GrisClaro,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = acceso.detalleVisual(),
                    color = GrisClaro,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onEliminar,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Eliminar", color = Color.White)
            }
        }
    }
}