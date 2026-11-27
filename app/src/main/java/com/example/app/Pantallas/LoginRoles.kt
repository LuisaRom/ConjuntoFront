package com.example.app.Pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.app.ViewModel.UsuarioViewModel

@Composable
fun PantallaLogin(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel
) {
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by usuarioViewModel.isLoading.collectAsState()
    val error by usuarioViewModel.error.collectAsState()
    val rolSeleccionado by usuarioViewModel.rolSeleccionado.collectAsState()

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var verTerminos by remember { mutableStateOf(false) }

    if (verTerminos) {
        DialogoTerminos(
            rol = rolSeleccionado,
            onCerrar = { verTerminos = false },
            onAceptar = {
                aceptaTerminos = true
                verTerminos = false
            }
        )
    }

    // Reacciona cuando cambie el rol autenticado o el rol seleccionado
    LaunchedEffect(usuarioActual?.rol, rolSeleccionado) {
        val user = usuarioActual ?: return@LaunchedEffect
        val rolBack = user.rol?.uppercase() ?: return@LaunchedEffect

        when (rolBack) {
            "ADMINISTRADOR" -> {
                if (rolSeleccionado == "ADMIN") {
                    navController.navigate("PantallaInicioAdmin") {
                        popUpTo("PantallaLogin") { inclusive = true }
                    }
                } else {
                    usuarioViewModel.setError("Usuario no autorizado para este rol")
                }
            }
            "CELADOR" -> {
                if (rolSeleccionado == "CELADOR") {
                    navController.navigate("PantallaDashboardCelador") {
                        popUpTo("PantallaLogin") { inclusive = true }
                    }
                } else {
                    usuarioViewModel.setError("Usuario no autorizado para este rol")
                }
            }
            "RESIDENTE" -> {
                if (rolSeleccionado == "RESIDENTE") {
                    navController.navigate("PantallaInicioResidentes") {
                        popUpTo("PantallaLogin") { inclusive = true }
                    }
                } else {
                    usuarioViewModel.setError("Usuario no autorizado para este rol")
                }
            }
            else -> {
                usuarioViewModel.setError("Rol de usuario no reconocido: ${user.rol}")
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1A1A2E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar Sesión (${rolSeleccionado ?: ""})",
                color = Color.White,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    usuarioViewModel.clearError()
                },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    usuarioViewModel.clearError() // limpiar error al escribir
                },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.White)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = {
                        aceptaTerminos = it
                        usuarioViewModel.clearError()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFDCB6E),
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.Black
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("Acepto los ")
                        withStyle(SpanStyle(color = Color(0xFFFDCB6E), fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                            append("Términos y condiciones")
                        }
                        append(", el tratamiento de mis datos personales y el aviso del asistente de IA")
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.clickable { verTerminos = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (usuario.isBlank() || password.isBlank()) {
                        usuarioViewModel.setError("Completa usuario y contraseña")
                        return@Button
                    }
                    if (!aceptaTerminos) {
                        usuarioViewModel.setError("Debes aceptar los términos y condiciones para ingresar")
                        return@Button
                    }
                    usuarioViewModel.clearError()
                    usuarioViewModel.login(usuario, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && aceptaTerminos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFDCB6E),
                    disabledContainerColor = Color(0xFFFDCB6E).copy(alpha = 0.35f)
                )
            ) {
                Text("Ingresar", color = Color.Black, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            }

            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x33FF0000) // Rojo semi-transparente
                    )
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
