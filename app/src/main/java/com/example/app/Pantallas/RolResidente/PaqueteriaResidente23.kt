package com.example.app.Pantallas.RolResidente

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Auth.AuthManager
import com.example.app.Model.Paqueteria
import com.example.app.ViewModel.PaqueteriaViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaPaqueteriaResidente(
    navController: NavController,
    paqueteriaViewModel: PaqueteriaViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuarioSesion by AuthManager.currentUser.collectAsState()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val paquetes by paqueteriaViewModel.paquetes.collectAsState()
    val isLoading by paqueteriaViewModel.isLoading.collectAsState()
    val usuarioId = usuarioActual?.id ?: usuarioSesion?.id

    LaunchedEffect(Unit) {
        paqueteriaViewModel.obtenerTodos()
    }

    val paquetesPendientes = paquetes.filter { paquete ->
        paquete.estado.equals("PENDIENTE", ignoreCase = true) &&
            paquete.usuario?.id == usuarioId
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
            Text("Paquetería", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else if (paquetesPendientes.isEmpty()) {
            Text(
                text = "No tienes paquetes pendientes por recibir.",
                color = GrisClaro
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paquetesPendientes, key = { it.id ?: it.hashCode().toLong() }) { paquete ->
                    PaquetePendienteCard(
                        paquete = paquete,
                        onConfirmar = {
                            val id = paquete.id
                            if (id == null) {
                                Toast.makeText(context, "No se pudo confirmar este paquete.", Toast.LENGTH_SHORT).show()
                                return@PaquetePendienteCard
                            }
                            paqueteriaViewModel.actualizarEstado(id, "ENTREGADO")
                            Toast.makeText(context, "Paquete confirmado como recibido.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaquetePendienteCard(
    paquete: Paqueteria,
    onConfirmar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Paquete #${paquete.id ?: "N/A"}",
                color = DoradoElegante,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Transportadora: ${paquete.transportadora}",
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Estado: ${paquete.estado}",
                color = GrisClaro,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onConfirmar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
            ) {
                Text("Confirmar recepción", color = AzulOscuro, fontWeight = FontWeight.Bold)
            }
        }
    }
}
