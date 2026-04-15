package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Mascota
import com.example.app.ViewModel.MascotaViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMascotasResidente(
    navController: NavController,
    mascotaViewModel: MascotaViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val mascotas by mascotaViewModel.mascotas.collectAsState()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by mascotaViewModel.isLoading.collectAsState()
    val error by mascotaViewModel.error.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSheet by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<String?>(null) }
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var vacunacionCompleta by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    val tiposMascota = listOf("Perro", "Gato", "Ave", "Otro")

    val seleccionarFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fotoUri = uri?.toString()
    }

    LaunchedEffect(Unit) {
        mascotaViewModel.obtenerTodos()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            mascotaViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = AzulOscuro,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = DoradoElegante
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva publicación", tint = AzulOscuro)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AzulOscuro)
                .padding(innerPadding)
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
                    text = "Mascotas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GrisClaro
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && mascotas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DoradoElegante)
                }
            } else if (mascotas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay publicaciones de mascotas.", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mascotas, key = { it.id ?: it.hashCode().toLong() }) { mascota ->
                        PublicacionMascotaFeed(mascota)
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState,
            containerColor = AzulOscuro
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Nueva publicación", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { seleccionarFotoLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (fotoUri == null) "Subir foto" else "Foto seleccionada",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la mascota") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = camposDark()
                )

                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = !expandedTipo }
                ) {
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de mascota") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTipo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                        colors = camposDark()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        tiposMascota.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    tipo = opcion
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = camposDark()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vacunación completa", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = vacunacionCompleta,
                        onCheckedChange = { vacunacionCompleta = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DoradoElegante)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (nombre.isBlank() || tipo.isBlank() || descripcion.isBlank()) {
                            Toast.makeText(context, "Completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val razaPayload = buildString {
                            append(descripcion.trim())
                            append(" | Vacunación: ")
                            append(if (vacunacionCompleta) "Sí" else "No")
                            if (!fotoUri.isNullOrBlank()) {
                                append(" | Foto: ")
                                append(fotoUri)
                            }
                        }
                        val mascota = Mascota(
                            nombre = nombre.trim(),
                            tipo = tipo.trim(),
                            raza = razaPayload,
                            usuario = usuarioActual
                        )
                        mascotaViewModel.guardar(mascota) {
                            scope.launch {
                                showSheet = false
                                nombre = ""
                                tipo = ""
                                descripcion = ""
                                vacunacionCompleta = false
                                fotoUri = null
                                navController.navigate("PantallaMenuResidente") {
                                    popUpTo("PantallaMenuResidente") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Compartir", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PublicacionMascotaFeed(mascota: Mascota) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Mascota",
                    tint = DoradoElegante
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(mascota.nombre, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Tipo: ${mascota.tipo}", color = GrisClaro, fontSize = 12.sp)
            Text("Descripción: ${mascota.raza}", color = Color.White, fontSize = 13.sp)
            Text(
                "Publicado por: ${mascota.usuario?.nombre ?: mascota.usuario?.usuario ?: "Residente"}",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun camposDark() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = AzulOscuro,
    focusedBorderColor = DoradoElegante,
    unfocusedBorderColor = GrisClaro,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)