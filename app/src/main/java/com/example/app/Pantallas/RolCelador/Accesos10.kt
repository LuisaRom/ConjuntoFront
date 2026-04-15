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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaAccesosCelador(navController: NavController) {
    val accesosVehiculares = emptyList<String>()  // Lista vacía
    val accesosPeatonales = emptyList<String>()   // Lista vacía

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
                style = MaterialTheme.typography.headlineMedium,
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
        if (accesosVehiculares.isEmpty()) {
            Text("No hay accesos creados", color = Color.Gray, fontSize = 14.sp)
        } else {
            accesosVehiculares.forEach { acceso ->
                AccesoItem(acceso) {
                    navController.navigate("PantallaAccesoVehicularCelador")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
            Text("No hay accesos creados", color = Color.Gray, fontSize = 14.sp)
        } else {
            accesosPeatonales.forEach { acceso ->
                AccesoItem(acceso) {
                    navController.navigate("PantallaAccesoPeatonalCelador")
                }
            }
        }
    }
}

@Composable
fun AccesoItem(titulo: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = titulo, color = GrisClaro)
    }
}