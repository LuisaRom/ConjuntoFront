package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro
import com.example.app.ViewModel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(navController: NavController, usuarioViewModel: UsuarioViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val usuarioPerfil = remember(usuarioActual, usuarios) {
        val actual = usuarioActual
        if (actual?.id != null) {
            usuarios.find { it.id == actual.id } ?: actual
        } else {
            actual
        }
    }
    var mostrarContrasena by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerTodos()
    }

    Scaffold(
        containerColor = AzulOscuro,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisOscuro)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = usuarioPerfil?.usuario ?: "Usuario",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CampoPerfil("Nombre", usuarioPerfil?.nombre ?: "", enabled = false)
            CampoPasswordPerfil(
                valorReal = usuarioPerfil?.password.orEmpty(),
                visible = mostrarContrasena,
                onToggleVisible = { mostrarContrasena = !mostrarContrasena }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Usuarios", color = Color.White, fontWeight = FontWeight.Bold)

            InfoPerfil("Usuarios Creados", usuarios.size.toString())

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
            ) {
                Text("Cerrar Sesión", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
}
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
        Text(label, color = Color.LightGray, fontSize = 12.sp)
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

@Composable
fun CampoPasswordPerfil(
    valorReal: String,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Contraseña", color = Color.LightGray, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (visible) valorReal.ifBlank { "****" } else "****",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Mostrar/Ocultar contraseña",
                tint = DoradoElegante,
                modifier = Modifier.clickable { onToggleVisible() }
            )
        }
    }
}

@Composable
fun InfoPerfil(titulo: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(titulo, color = Color.LightGray, fontSize = 12.sp)
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