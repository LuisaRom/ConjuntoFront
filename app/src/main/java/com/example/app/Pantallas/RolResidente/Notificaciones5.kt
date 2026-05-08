package com.example.app.Pantallas.RolResidente

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaNotificacionesResidente(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf("Paqueteria") }

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
            Text("Notificaciones", color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { selectedTab = "Paqueteria" },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "Paqueteria") DoradoElegante else Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Paquetería",
                    tint = if (selectedTab == "Paqueteria") AzulOscuro else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paquetería", color = if (selectedTab == "Paqueteria") AzulOscuro else Color.White)
            }

            Button(
                onClick = {
                    selectedTab = "Recibos"
                    navController.navigate("PantallaRecibos")
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "Recibos") DoradoElegante else Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Recibos",
                    tint = if (selectedTab == "Recibos") AzulOscuro else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recibos", color = if (selectedTab == "Recibos") AzulOscuro else Color.White)
            }

            Button(
                onClick = {
                    selectedTab = "Pagos"
                    navController.navigate("PantallaPagos")
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "Pagos") DoradoElegante else Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = "Pagos",
                    tint = if (selectedTab == "Pagos") AzulOscuro else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pagos", color = if (selectedTab == "Pagos") AzulOscuro else Color.White)
            }
        }
    }
}