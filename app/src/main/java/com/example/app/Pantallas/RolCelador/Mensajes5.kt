package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaMensajesCelador(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    var busqueda by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (usuarios.isEmpty()) usuarioViewModel.obtenerTodos()
    }

    val usuariosMensajeria = remember(usuarios, busqueda) {
        usuarios.filter { usuario ->
            val rol = usuario.rol.uppercase()
            rol == "CELADOR" || rol == "ADMINISTRADOR" || rol == "ADMIN"
        }.filter { usuario ->
            val nombre = usuario.nombre.ifBlank { usuario.usuario }
            nombre.contains(busqueda, ignoreCase = true)
        }.sortedBy { it.nombre.ifBlank { it.usuario } }
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
                text = "Mensajes",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por nombre", color = GrisClaro) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedLabelColor = DoradoElegante,
                unfocusedLabelColor = GrisClaro,
                cursorColor = DoradoElegante,
                focusedContainerColor = AzulOscuro,
                unfocusedContainerColor = AzulOscuro
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (usuariosMensajeria.isEmpty()) {
            Text(
                text = "No hay usuarios para mostrar.",
                color = Color.White,
                fontSize = 16.sp
            )
        } else {
            LazyColumn {
                items(usuariosMensajeria, key = { it.id ?: it.usuario.hashCode().toLong() }) { usuario ->
                    val nombre = usuario.nombre.ifBlank { usuario.usuario }
                    val torreApto = "Torre ${usuario.torre.ifBlank { "-" }} - Apt ${usuario.apartamento.ifBlank { "-" }}"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(12.dp)
                    ) {
                        Text(nombre, color = Color.White)
                        Text(torreApto, color = GrisClaro, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}