package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app.Model.Usuario
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ViewModel.UsuarioViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@Composable
fun PantallaCrearUsuario(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("RESIDENTE") }
    var usuario by remember { mutableStateOf("") }
    var torre by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorDocumento by remember { mutableStateOf<String?>(null) }
    var errorTelefono by remember { mutableStateOf<String?>(null) }
    var errorRol by remember { mutableStateOf<String?>(null) }
    var errorUsuario by remember { mutableStateOf<String?>(null) }
    var errorTorre by remember { mutableStateOf<String?>(null) }
    var errorApartamento by remember { mutableStateOf<String?>(null) }
    var errorPassword by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    val isLoading by usuarioViewModel.isLoading.collectAsState()
    val error by usuarioViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Validar email
    fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
            Pattern.CASE_INSENSITIVE
        )
        return pattern.matcher(email).matches()
    }
    
    // Función para validar formulario
    fun validarFormulario(): Boolean {
        errorNombre = null
        errorEmail = null
        errorDocumento = null
        errorTelefono = null
        errorRol = null
        errorUsuario = null
        errorTorre = null
        errorApartamento = null
        errorPassword = null
        errorGeneral = null
        
        var esValido = true
        
        if (nombre.isBlank()) {
            errorNombre = "El nombre es obligatorio"
            esValido = false
        }
        
        if (email.isBlank()) {
            errorEmail = "El email es obligatorio"
            esValido = false
        } else if (!isValidEmail(email)) {
            errorEmail = "El formato del email no es válido"
            esValido = false
        }
        
        if (telefono.isBlank()) {
            errorTelefono = "El teléfono es obligatorio"
            esValido = false
        }

        if (documento.isBlank()) {
            errorDocumento = "El documento es obligatorio"
            esValido = false
        }
        
        if (rolSeleccionado.isBlank()) {
            errorRol = "El rol es obligatorio"
            esValido = false
        }
        
        if (usuario.isBlank()) {
            errorUsuario = "El usuario es obligatorio"
            esValido = false
        }

        if (password.isBlank()) {
            errorPassword = "La contraseña es obligatoria"
            esValido = false
        }

        if (rolSeleccionado == "RESIDENTE" && torre.isBlank()) {
            errorTorre = "La torre es obligatoria para residentes"
            esValido = false
        }

        if (rolSeleccionado == "RESIDENTE" && apartamento.isBlank()) {
            errorApartamento = "El apartamento es obligatorio para residentes"
            esValido = false
        }
        
        return esValido
    }
    
    // Función para crear usuario
    fun crearUsuario() {
        if (!validarFormulario()) {
            return
        }
        
        val nuevoUsuario = Usuario(
            nombre = nombre,
            documento = documento,
            telefono = telefono,
            email = email,
            usuario = usuario,
            password = password,
            rol = rolSeleccionado.uppercase(),
            torre = if (rolSeleccionado == "RESIDENTE") torre else "",
            apartamento = if (rolSeleccionado == "RESIDENTE") apartamento else ""
        )
        
        scope.launch {
            try {
                val usuarioGuardado = usuarioViewModel.guardarUsuario(nuevoUsuario)
                snackbarHostState.showSnackbar(
                    message = "Usuario creado exitosamente",
                    duration = SnackbarDuration.Short
                )
                // Limpiar formulario
                nombre = ""
                email = ""
                telefono = ""
                documento = ""
                usuario = ""
                torre = ""
                apartamento = ""
                password = ""
                // Opcional: volver atrás después de un delay
                kotlinx.coroutines.delay(1500)
                navController.popBackStack()
            } catch (e: Exception) {
                errorGeneral = e.message ?: "Error al crear usuario"
                snackbarHostState.showSnackbar(
                    message = errorGeneral ?: "Error desconocido",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }
    
    // Observar errores del ViewModel
    LaunchedEffect(error) {
        error?.let {
            errorGeneral = it
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AzulOscuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Usuario",
                color = GrisClaro,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Mensaje de error general
            errorGeneral?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x33FF0000)
                    )
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
            }

            // Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { 
                    nombre = it
                    errorNombre = null
                },
                label = { Text("Nombre completo", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorNombre != null,
                supportingText = errorNombre?.let { { Text(it, color = Color.Red) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorEmail = null
                },
                label = { Text("Email", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errorEmail != null,
                supportingText = errorEmail?.let { { Text(it, color = Color.Red) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Teléfono
            OutlinedTextField(
                value = telefono,
                onValueChange = { 
                    telefono = it
                    errorTelefono = null
                },
                label = { Text("Teléfono", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errorTelefono != null,
                supportingText = errorTelefono?.let { { Text(it, color = Color.Red) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = documento,
                onValueChange = {
                    documento = it
                    errorDocumento = null
                },
                label = { Text("Documento *", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorDocumento != null,
                supportingText = errorDocumento?.let { { Text(it, color = Color.Red) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Rol
            Text(
                text = "Rol",
                color = GrisClaro,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("RESIDENTE", "CELADOR").forEach { rol ->
                    FilterChip(
                        selected = rolSeleccionado == rol,
                        onClick = {
                            rolSeleccionado = rol
                            errorRol = null
                            errorTorre = null
                            errorApartamento = null
                        },
                        label = { Text(rol) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DoradoElegante,
                            selectedLabelColor = AzulOscuro
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            errorRol?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Usuario
            OutlinedTextField(
                value = usuario,
                onValueChange = { 
                    usuario = it
                    errorUsuario = null
                },
                label = { Text("Usuario *", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorUsuario != null,
                supportingText = errorUsuario?.let { { Text(it, color = Color.Red) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (rolSeleccionado == "RESIDENTE") {
                OutlinedTextField(
                    value = torre,
                    onValueChange = {
                        torre = it
                        errorTorre = null
                    },
                    label = { Text("Torre *", color = GrisClaro) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorTorre != null,
                    supportingText = errorTorre?.let { { Text(it, color = Color.Red) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DoradoElegante,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = GrisClaro,
                        unfocusedLabelColor = GrisClaro
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apartamento,
                    onValueChange = {
                        apartamento = it
                        errorApartamento = null
                    },
                    label = { Text("Apartamento *", color = GrisClaro) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorApartamento != null,
                    supportingText = errorApartamento?.let { { Text(it, color = Color.Red) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DoradoElegante,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = GrisClaro,
                        unfocusedLabelColor = GrisClaro
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Campo Password (obligatoria para ambos roles)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorPassword = null
                },
                label = { Text("Contraseña *", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorPassword != null,
                supportingText = errorPassword?.let { { Text(it, color = Color.Red) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Crear
            Button(
                onClick = { crearUsuario() },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AzulOscuro
                    )
                } else {
                    Text(
                        text = "Crear Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro
                    )
                }
            }
        }
    }
}
