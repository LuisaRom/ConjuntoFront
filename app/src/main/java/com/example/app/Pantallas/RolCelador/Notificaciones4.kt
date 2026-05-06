package com.example.app.Pantallas.RolCelador

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaNotificacionesCelador(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Mensajes") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulOscuro
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                        .padding(end = 8.dp)
                )
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BotonNotificacionGrande(
                    titulo = "Mensajes",
                    seleccionado = selectedTab == "Mensajes"
                ) {
                    selectedTab = "Mensajes"
                    navController.navigate("PantallaMensajesCelador")
                }
                BotonNotificacionGrande(
                    titulo = "Paquetería",
                    seleccionado = selectedTab == "Paqueteria"
                ) {
                    selectedTab = "Paqueteria"
                    navController.navigate("PantallaPaqueteriaCelador")
                }
                BotonNotificacionGrande(
                    titulo = "Recibos",
                    seleccionado = selectedTab == "Recibos"
                ) {
                    selectedTab = "Recibos"
                    navController.navigate("PantallaRecibosCelador")
                }
            }
        }
    }
}

@Composable
private fun BotonNotificacionGrande(
    titulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) DoradoElegante else GrisClaro
        )
    ) {
        Text(
            text = titulo,
            color = AzulOscuro,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

