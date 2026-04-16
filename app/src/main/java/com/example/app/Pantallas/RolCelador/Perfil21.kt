package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfilCelador(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerTodos()
    }

    val usuarioPerfil = remember(usuarioActual, usuarios) {
        val actual = usuarioActual
        if (actual?.id != null) {
            usuarios.find { it.id == actual.id } ?: actual
        } else {
            actual
        }
    }

    val rolVisual = when (usuarioPerfil?.rol?.uppercase()) {
        "CELADOR" -> "CELADOR"
        null, "" -> "SIN ROL"
        else -> "${usuarioPerfil?.rol} (usuario no celador)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
        // Botón volver
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ícono de perfil
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Perfil",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally),
            tint = Color.White
        )

        Text(
            text = usuarioPerfil?.usuario ?: "Usuario",
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CampoPerfil("Nombre", usuarioPerfil?.nombre ?: "", enabled = false)
        CampoPerfil("Usuario", usuarioPerfil?.usuario ?: "", enabled = false)
        CampoPerfilTexto("Contraseña", if (usuarioPerfil?.password.isNullOrBlank()) "" else "****")
        CampoPerfil("Rol", rolVisual, enabled = false)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
        ) {
            Text("Cerrar Sesión", color = AzulOscuro, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirmación") },
                text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        usuarioViewModel.logout()
                        navController.navigate("PantallaSeleccionRol") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                containerColor = AzulOscuro
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoPerfil(label: String, valor: String, enabled: Boolean) {
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        enabled = enabled,
        label = { Text(label, color = Color.LightGray) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DoradoElegante,
            unfocusedBorderColor = GrisClaro,
            disabledBorderColor = GrisClaro,
            cursorColor = DoradoElegante,
            focusedLabelColor = GrisClaro,
            unfocusedLabelColor = GrisClaro,
            disabledLabelColor = GrisClaro,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White
        )
    )
}

@Composable
fun CampoPerfilTexto(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        Text(
            text = valor,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                .padding(12.dp)
        )
    }
}