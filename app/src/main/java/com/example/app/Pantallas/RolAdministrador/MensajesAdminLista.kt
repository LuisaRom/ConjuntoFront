package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaMensajesAdmin(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val usuarios by usuarioViewModel.usuarios.collectAsState()

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerContactosMensajeria()
    }

    val contactos = usuarios.filter {
        val nombre = it.nombre.ifBlank { it.usuario }
        val rol = it.rol.uppercase()
        val esRolPermitido = rol == "ADMINISTRADOR" || rol == "ADMIN" || rol == "CELADOR"
        esRolPermitido && nombre.contains(searchQuery, ignoreCase = true)
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
            androidx.compose.foundation.layout.Row(
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
                    text = "Mensajes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            if (contactos.isEmpty()) {
                Text(
                    text = "No hay administradores o celadores disponibles para chatear.",
                    color = GrisClaro,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(contactos, key = { it.id ?: it.usuario.hashCode().toLong() }) { contacto ->
                        val nombre = contacto.nombre.ifBlank { contacto.usuario }
                        val detalle = contacto.rol.ifBlank { "Usuario" }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { contacto.id?.let { navController.navigate("PantallaMensajes/$it") } },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(nombre, color = Color.White)
                                Text(detalle, color = GrisClaro)
                            }
                        }
                    }
                }
            }
        }
    }
}
