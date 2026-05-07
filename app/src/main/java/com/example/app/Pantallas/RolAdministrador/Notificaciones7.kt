package com.example.app.Pantallas.RolAdministrador

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.Pantallas.RolAdministrador.PantallaPagos
import com.example.app.ViewModel.UsuarioViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaNotificaciones(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf("Mensajes") }
    var searchQuery by remember { mutableStateOf("") }
    val usuarios by usuarioViewModel.usuarios.collectAsState()

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerTodos()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulOscuro
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { selectedTab = "Mensajes" },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "Mensajes") DoradoElegante else GrisClaro
                    )
                ) {
                    Text("Mensajes", color = AzulOscuro)
                }

                Button(
                    onClick = { selectedTab = "Pagos" },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == "Pagos") DoradoElegante else GrisClaro
                    )
                ) {
                    Text("Pagos", color = AzulOscuro)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == "Mensajes") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar", color = GrisClaro) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoradoElegante,
                        unfocusedBorderColor = GrisClaro,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = DoradoElegante
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                val celadores = usuarios.filter {
                    it.rol.equals("CELADOR", ignoreCase = true)
                }.filter {
                    val nombre = it.nombre.ifBlank { it.usuario }
                    nombre.contains(searchQuery, ignoreCase = true)
                }

                if (celadores.isEmpty()) {
                    Text(
                        text = "No hay celadores disponibles para chatear.",
                        color = GrisClaro,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(celadores, key = { it.id ?: it.usuario.hashCode().toLong() }) { celador ->
                            val nombre = celador.nombre.ifBlank { celador.usuario }
                            val torreApto = "Torre ${celador.torre.ifBlank { "-" }} - Apt ${celador.apartamento.ifBlank { "-" }}"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("PantallaMensajes/${Uri.encode(nombre)}") },
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(nombre, color = Color.White)
                                    Text(torreApto, color = GrisClaro)
                                }
                            }
                        }
                    }
                }
            } else {
                runCatching { PantallaPagos(navController) }
                    .onFailure {
                        Text(
                            text = "No fue posible abrir pagos en este momento.",
                            color = Color(0xFFFFB4AB)
                        )
                    }
            }
        }
    }
}

