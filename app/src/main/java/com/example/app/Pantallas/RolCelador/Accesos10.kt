package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.AccesoPeatonal
import com.example.app.Model.AccesoVehicular
import com.example.app.ViewModel.AccesoPeatonalViewModel
import com.example.app.ViewModel.AccesoVehicularViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaAccesosCelador(
    navController: NavController,
    accesoVehicularViewModel: AccesoVehicularViewModel = hiltViewModel(),
    accesoPeatonalViewModel: AccesoPeatonalViewModel = hiltViewModel()
) {
    val accesosVehiculares by accesoVehicularViewModel.accesosVehiculares.collectAsState()
    val accesosPeatonales by accesoPeatonalViewModel.accesosPeatonales.collectAsState()
    var vehicularSeleccionado by remember { mutableStateOf<AccesoVehicular?>(null) }
    var peatonalSeleccionado by remember { mutableStateOf<AccesoPeatonal?>(null) }

    LaunchedEffect(Unit) {
        accesoVehicularViewModel.obtenerAccesosVehiculares()
        accesoPeatonalViewModel.obtenerAccesosPeatonales()
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Vehicular",
                    tint = DoradoElegante,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Acceso Vehicular", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("autoScanVehicular", true)
                navController.navigate("PantallaAccesoVehicularCelador")
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Escanear vehicular", tint = DoradoElegante)
            }
        }
        Text("${accesosVehiculares.size} accesos", color = Color.LightGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (accesosVehiculares.isEmpty()) {
            Text("No hay accesos creados", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(modifier = Modifier.height(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accesosVehiculares, key = { it.id ?: it.hashCode().toLong() }) { acceso ->
                    val nombreUsuario = acceso.autorizadoPor?.nombre ?: acceso.autorizadoPor?.usuario ?: "Sin nombre"
                    val torre = acceso.autorizadoPor?.torre ?: acceso.torre ?: "-"
                    val apto = acceso.autorizadoPor?.apartamento ?: acceso.apartamento ?: "-"
                    AccesoItem(
                        titulo = nombreUsuario,
                        subtitulo = "Torre $torre - Apt $apto"
                    ) { vehicularSeleccionado = acceso }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = "Peatonal",
                    tint = DoradoElegante,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Acceso Peatonal", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("autoScanPeatonal", true)
                navController.navigate("PantallaAccesoPeatonalCelador")
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Escanear peatonal", tint = DoradoElegante)
            }
        }
        Text("${accesosPeatonales.size} accesos", color = Color.LightGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (accesosPeatonales.isEmpty()) {
            Text("No hay accesos creados", color = Color.Gray, fontSize = 14.sp)
        } else {
            LazyColumn(modifier = Modifier.height(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accesosPeatonales, key = { it.id ?: it.hashCode().toLong() }) { acceso ->
                    val nombreUsuario = acceso.autorizadoPor?.nombre ?: acceso.autorizadoPor?.usuario ?: "Sin nombre"
                    val torre = acceso.autorizadoPor?.torre ?: acceso.torre ?: "-"
                    val apto = acceso.autorizadoPor?.apartamento ?: acceso.apartamento ?: "-"
                    AccesoItem(
                        titulo = nombreUsuario,
                        subtitulo = "Torre $torre - Apt $apto"
                    ) { peatonalSeleccionado = acceso }
                }
            }
        }
    }

    vehicularSeleccionado?.let { acceso ->
        AlertDialog(
            onDismissRequest = { vehicularSeleccionado = null },
            title = { Text("Acceso Vehicular", color = Color.White) },
            text = {
                Column {
                    Text("Residente: ${acceso.autorizadoPor?.nombre ?: acceso.autorizadoPor?.usuario ?: "-"}", color = Color.White)
                    Text("Placa: ${acceso.placaVehiculo}", color = Color.White)
                    Text("Torre/Apto: ${acceso.torre} - ${acceso.apartamento}", color = Color.White)
                    Text("Código QR: ${acceso.codigoQr ?: "-"}", color = Color.White)
                    Text("Fecha: ${acceso.horaAutorizada ?: "-"}", color = Color.White)
                }
            },
            confirmButton = {
                Button(
                    onClick = { vehicularSeleccionado = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
                ) { Text("Cerrar", color = AzulOscuro, fontWeight = FontWeight.Bold) }
            },
            containerColor = AzulOscuro
        )
    }

    peatonalSeleccionado?.let { acceso ->
        AlertDialog(
            onDismissRequest = { peatonalSeleccionado = null },
            title = { Text("Acceso Peatonal", color = Color.White) },
            text = {
                Column {
                    Text("Residente: ${acceso.autorizadoPor?.nombre ?: acceso.autorizadoPor?.usuario ?: "-"}", color = Color.White)
                    Text("Visitante: ${acceso.nombreVisitante}", color = Color.White)
                    Text("Torre/Apto: ${acceso.torre} - ${acceso.apartamento}", color = Color.White)
                    Text("Código QR: ${acceso.codigoQr}", color = Color.White)
                    Text("Fecha: ${acceso.horaAutorizada ?: "-"}", color = Color.White)
                }
            },
            confirmButton = {
                Button(
                    onClick = { peatonalSeleccionado = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
                ) { Text("Cerrar", color = AzulOscuro, fontWeight = FontWeight.Bold) }
            },
            containerColor = AzulOscuro
        )
    }
}

@Composable
fun AccesoItem(titulo: String, subtitulo: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(text = titulo, color = Color.White, fontSize = 13.sp)
            Text(text = subtitulo, color = GrisClaro, fontSize = 12.sp)
        }
    }
}