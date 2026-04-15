package com.example.app.Pantallas.RolAdministrador

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.example.app.Model.Notificacion
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaCreacionPublicacionAdmin(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel(),
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by notificacionViewModel.isLoading.collectAsState()
    
    var descripcion by remember { mutableStateOf("") }
    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var videoSeleccionado by remember { mutableStateOf<Uri?>(null) }
    var videoFile by remember { mutableStateOf<File?>(null) }
    var usuariosEtiquetados by remember { mutableStateOf<List<com.example.app.Model.Usuario>>(emptyList()) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showVideoOptions by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var publicacionGuardada by remember { mutableStateOf(false) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    
    val usuarios by usuarioViewModel.usuarios.collectAsState()

    val nombreUsuario = usuarioActual?.nombre ?: usuarioActual?.usuario ?: "Usuario"

    // Permisos
    val cameraPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.CAMERA
    } else {
        Manifest.permission.CAMERA
    }

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Función para crear archivo de imagen (debe definirse antes de usarse)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            imageFile = this
        }
    }

    // ActivityResultLauncher para tomar foto (debe definirse primero)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageFile != null) {
            imagenSeleccionada = imageFile!!.absolutePath
            imagenUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile!!
            )
            mensajeError = null
        } else {
            mensajeError = "Error al tomar la foto"
        }
    }
    
    // ActivityResultLauncher para seleccionar imagen de galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            try {
                // Copiar la imagen a un archivo temporal
                val inputStream = context.contentResolver.openInputStream(it)
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_${timeStamp}_"
                val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                val file = File.createTempFile(imageFileName, ".jpg", storageDir)
                
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                imagenSeleccionada = file.absolutePath
                imageFile = file
                mensajeError = null
            } catch (e: Exception) {
                mensajeError = "Error al procesar la imagen: ${e.message}"
            }
        }
    }
    
    // ActivityResultLauncher para seleccionar video de galería
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            videoSeleccionado = it
            // Intentar copiar el video a un archivo temporal para tener la ruta
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val videoFileName = "VIDEO_${timeStamp}_"
                val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
                val file = File.createTempFile(videoFileName, ".mp4", storageDir)
                
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                videoFile = file
                mensajeError = null
                mensajeExito = "Video seleccionado correctamente"
            } catch (e: Exception) {
                // Si falla la copia, usar el URI directamente
                android.util.Log.e("CreacionPublicacion", "Error al copiar video: ${e.message}")
                videoSeleccionado = it
                mensajeError = null
                mensajeExito = "Video seleccionado correctamente"
            }
        }
    }
    
    // ActivityResultLauncher para grabar video
    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && videoFile != null) {
            // El video se guarda en videoFile, guardar la ruta
            videoSeleccionado = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                videoFile!!
            )
            mensajeError = null
            mensajeExito = "Video grabado correctamente"
        } else {
            mensajeError = "Error al grabar el video"
        }
    }
    
    // ActivityResultLauncher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission.value = isGranted
        if (isGranted) {
            // Ejecutar la acción pendiente si existe
            pendingAction?.invoke()
            pendingAction = null
        } else {
            mensajeError = "Permiso de cámara denegado"
            pendingAction = null
        }
    }
    
    // Función para crear archivo de video
    fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
        return File.createTempFile(videoFileName, ".mp4", storageDir).apply {
            videoFile = this
        }
    }
    
    // Función para grabar video
    fun recordVideo() {
        if (hasCameraPermission.value) {
            val file = createVideoFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            recordVideoLauncher.launch(uri)
        } else {
            pendingAction = {
                val file = createVideoFile()
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                recordVideoLauncher.launch(uri)
            }
            permissionLauncher.launch(cameraPermission)
        }
    }
    
    // Cargar usuarios cuando se abre el diálogo de etiquetas
    LaunchedEffect(showTagDialog) {
        if (showTagDialog && usuarios.isEmpty()) {
            try {
                usuarioViewModel.obtenerTodos()
            } catch (e: Exception) {
                mensajeError = "Error al cargar usuarios: ${e.message}"
            }
        }
    }

    // Función para abrir cámara
    fun openCamera() {
        if (hasCameraPermission.value) {
            val file = createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        } else {
            pendingAction = {
                val file = createImageFile()
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                takePictureLauncher.launch(uri)
            }
            permissionLauncher.launch(cameraPermission)
        }
    }

    LaunchedEffect(publicacionGuardada) {
        if (publicacionGuardada) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Creación Publicación",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Foto de usuario con icono por defecto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(nombreUsuario, color = Color.White, fontWeight = FontWeight.Bold)
                    if (usuarioActual?.torre != null && usuarioActual?.apartamento != null) {
                        Text(
                            "${usuarioActual?.torre} - ${usuarioActual?.apartamento}",
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar usuarios etiquetados
            if (usuariosEtiquetados.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Etiquetados: ",
                        color = GrisClaro,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    usuariosEtiquetados.forEach { usuario ->
                        Card(
                            modifier = Modifier.padding(end = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DoradoElegante.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "@${usuario.nombre}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Imagen seleccionada (si hay)
            imagenSeleccionada?.let { imagenPath ->
                val file = File(imagenPath)
                if (file.exists()) {
                    Image(
                        painter = rememberAsyncImagePainter(file),
                        contentDescription = "Imagen Seleccionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } ?: imagenUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Imagen Seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Video seleccionado (si hay)
            videoSeleccionado?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisOscuro)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Video seleccionado",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Campo de descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción de la publicación") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    cursorColor = DoradoElegante,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Icons.Default.Person to "Etiqueta",
                    Icons.Default.Image to "Galería",
                    Icons.Default.VideoLibrary to "Video",
                    Icons.Default.PhotoCamera to "Cámara"
                ).forEach { (icon, desc) ->
                    IconButton(
                        onClick = {
                            when (desc) {
                                "Cámara" -> {
                                    openCamera()
                                }
                                "Galería" -> {
                                    pickImageLauncher.launch("image/*")
                                }
                                "Video" -> {
                                    showVideoOptions = true
                                }
                                "Etiqueta" -> {
                                    showTagDialog = true
                                }
                                else -> {
                                    // Otras opciones pendientes
                                }
                            }
                        }
                    ) {
                        Icon(icon, contentDescription = desc, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mensajes de éxito/error
            mensajeExito?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = Color.Green,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            mensajeError?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Botón compartir
            Button(
                onClick = {
                    if (descripcion.isBlank()) {
                        mensajeError = "Por favor ingresa una descripción"
                        mensajeExito = null
                    } else if (usuarioActual == null) {
                        mensajeError = "No hay usuario autenticado"
                        mensajeExito = null
                    } else {
                        mensajeError = null
                        // Formato ISO compatible con backend y minSdk 24
                        val fechaActual = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                        
                        // Convertir lista de usuarios etiquetados a JSON
                        val usuariosEtiquetadosJson = if (usuariosEtiquetados.isNotEmpty()) {
                            val ids = usuariosEtiquetados.mapNotNull { it.id }
                            com.google.gson.Gson().toJson(ids)
                        } else {
                            null
                        }
                        
                        // Obtener ruta del video si existe
                        val videoPath = when {
                            videoFile != null -> {
                                // Video grabado - usar la ruta del archivo
                                videoFile!!.absolutePath
                            }
                            videoSeleccionado != null -> {
                                // Video seleccionado de galería - intentar obtener la ruta
                                val uri = videoSeleccionado!!
                                try {
                                    if (uri.scheme == "file") {
                                        uri.path
                                    } else if (uri.scheme == "content") {
                                        // Intentar obtener la ruta real del content provider
                                        val cursor = context.contentResolver.query(
                                            uri,
                                            arrayOf(android.provider.MediaStore.Video.Media.DATA),
                                            null,
                                            null,
                                            null
                                        )
                                        cursor?.use {
                                            if (it.moveToFirst()) {
                                                val index = it.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DATA)
                                                it.getString(index)
                                            } else {
                                                uri.toString()
                                            }
                                        } ?: uri.toString()
                                    } else {
                                        uri.toString()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("CreacionPublicacion", "Error al obtener ruta del video: ${e.message}")
                                    uri.toString()
                                }
                            }
                            else -> null
                        }
                        
                        // Solo enviar imagenUrl si hay una imagen seleccionada
                        val notificacion = Notificacion(
                            mensaje = descripcion,
                            fechaEnvio = fechaActual,
                            usuario = usuarioActual,
                            imagenUrl = imagenSeleccionada?.takeIf { it.isNotBlank() },
                            videoUrl = videoPath,
                            usuariosEtiquetados = usuariosEtiquetadosJson
                        )
                        
                        // Log para debugging
                        android.util.Log.d("CreacionPublicacion", "Guardando notificación: mensaje=$descripcion, usuario=${usuarioActual?.id}, imagenUrl=${imagenSeleccionada}, videoUrl=$videoPath, etiquetas=${usuariosEtiquetados.size}")
                        
                        coroutineScope.launch {
                            try {
                                notificacionViewModel.guardar(notificacion)
                                mensajeExito = "Publicación creada exitosamente"
                                mensajeError = null
                                
                        // Limpiar campos después de guardar
                        descripcion = ""
                        imagenSeleccionada = null
                        imagenUri = null
                        videoSeleccionado = null
                        videoFile = null
                        usuariosEtiquetados = emptyList()
                        imageFile = null
                                
                                // Marcar como guardado y refrescar
                                publicacionGuardada = true
                                // Esperar un momento antes de refrescar para asegurar que el servidor procesó
                                kotlinx.coroutines.delay(500)
                                notificacionViewModel.obtenerTodos()
                            } catch (e: Exception) {
                                mensajeError = "Error al guardar publicación: ${e.message}"
                                mensajeExito = null
                                e.printStackTrace() // Para debugging
                            }
                        }
                    }
                },
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
                    Text("Compartir", color = AzulOscuro, fontSize = 16.sp)
                }
            }
        }
    }
    
    // Diálogo para seleccionar usuarios a etiquetar
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Etiquetar Usuarios", color = Color.White) },
            text = {
                if (usuarios.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cargando usuarios...", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(usuarios) { usuario ->
                            val isSelected = usuariosEtiquetados.any { it.id == usuario.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            usuariosEtiquetados = usuariosEtiquetados.filter { it.id != usuario.id }
                                        } else {
                                            usuariosEtiquetados = usuariosEtiquetados + usuario
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            usuariosEtiquetados = usuariosEtiquetados + usuario
                                        } else {
                                            usuariosEtiquetados = usuariosEtiquetados.filter { it.id != usuario.id }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = DoradoElegante)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${usuario.nombre} (${usuario.rol})",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Aceptar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }
    
    // Diálogo para opciones de video
    if (showVideoOptions) {
        AlertDialog(
            onDismissRequest = { showVideoOptions = false },
            title = { Text("Seleccionar Video", color = Color.White) },
            text = {
                Column {
                    TextButton(onClick = {
                        showVideoOptions = false
                        recordVideo()
                    }) {
                        Text("Grabar Video", color = Color.White)
                    }
                    TextButton(onClick = {
                        showVideoOptions = false
                        pickVideoLauncher.launch("video/*")
                    }) {
                        Text("Seleccionar de Galería", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVideoOptions = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }
}
