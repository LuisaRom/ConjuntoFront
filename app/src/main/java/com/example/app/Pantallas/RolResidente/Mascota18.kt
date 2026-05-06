package com.example.app.Pantallas.RolResidente

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
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
import coil.compose.AsyncImage
import java.io.File

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
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSheet by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<String?>(null) }
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var vacunacionCompleta by remember { mutableStateOf<Boolean?>(null) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedVacunacion by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
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
            mensajeError = it
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
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Nueva publicación", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { seleccionarFotoLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (fotoUri == null) "Subir foto" else "Foto seleccionada",
                        color = AzulOscuro
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                fotoUri?.let { uriPreview ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = GrisClaro
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vista previa", color = GrisClaro, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = uriPreview,
                                contentDescription = "Vista previa de mascota",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

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
                ExposedDropdownMenuBox(
                    expanded = expandedVacunacion,
                    onExpandedChange = { expandedVacunacion = !expandedVacunacion }
                ) {
                    OutlinedTextField(
                        value = when (vacunacionCompleta) {
                            true -> "Sí"
                            false -> "No"
                            null -> ""
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vacunación completa *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedVacunacion) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                        colors = camposDark()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedVacunacion,
                        onDismissRequest = { expandedVacunacion = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sí") },
                            onClick = {
                                vacunacionCompleta = true
                                expandedVacunacion = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("No") },
                            onClick = {
                                vacunacionCompleta = false
                                expandedVacunacion = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Campos obligatorios *", color = Color.LightGray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                mensajeExito?.let {
                    Text(text = it, color = Color.Green, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                mensajeError?.let {
                    Text(text = it, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        if (fotoUri == null) {
                            Toast.makeText(context, "La foto es obligatoria", Toast.LENGTH_SHORT).show()
                            mensajeError = "La foto es obligatoria"
                            return@Button
                        }
                        if (nombre.isBlank() || tipo.isBlank() || descripcion.isBlank() || vacunacionCompleta == null) {
                            Toast.makeText(context, "Completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                            mensajeError = "Completa todos los campos obligatorios (*)"
                            return@Button
                        }
                        if (fotoUri != null && fotoUri?.startsWith("content://") != true) {
                            Toast.makeText(context, "Formato de imagen no válido", Toast.LENGTH_SHORT).show()
                            mensajeError = "Formato de imagen no válido"
                            return@Button
                        }
                        val razaPayload = buildString {
                            append(descripcion.trim())
                            append(" | Vacunación: ")
                            append(if (vacunacionCompleta == true) "Sí" else "No")
                        }
                        val mascota = Mascota(
                            nombre = nombre.trim(),
                            tipo = tipo.trim(),
                            raza = razaPayload,
                            usuario = usuarioActual
                        )
                        val fotoFile = fotoUri?.let { guardarUriComoArchivoTemporal(context, it) }
                        val onSuccess = {
                            showSheet = false
                            nombre = ""
                            tipo = ""
                            descripcion = ""
                            vacunacionCompleta = null
                            fotoUri = null
                            mensajeError = null
                            mensajeExito = "Publicación creada con éxito"
                            Toast.makeText(context, "Publicación creada con éxito", Toast.LENGTH_SHORT).show()
                            mascotaViewModel.obtenerTodos()
                            Unit
                        }
                        if (fotoUri != null && fotoFile == null) {
                            Toast.makeText(context, "No se pudo procesar la imagen seleccionada", Toast.LENGTH_SHORT).show()
                            mensajeError = "No se pudo procesar la imagen seleccionada"
                            return@Button
                        }
                        if (fotoFile != null) {
                            mascotaViewModel.guardarConFoto(mascota, fotoFile, onSuccess)
                        } else {
                            mascotaViewModel.guardar(mascota, onSuccess)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Publicar", color = AzulOscuro, fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
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

private fun guardarUriComoArchivoTemporal(
    context: android.content.Context,
    uriString: String
): File? {
    return try {
        val uri = android.net.Uri.parse(uriString)
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val file = File.createTempFile("mascota_", ".jpg", context.cacheDir)
        input.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file
    } catch (_: Exception) {
        null
    }
}