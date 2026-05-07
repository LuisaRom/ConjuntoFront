package com.example.app.Pantallas.RolResidente

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app.Model.Notificacion
import com.example.app.R
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val REFRESCO_NOVEDADES_RESIDENTE_MS = 3000L

@Composable
fun PantallaInicioResidentes(
    navController: NavController,
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val notificaciones by notificacionViewModel.notificaciones.collectAsState()
    val isLoading by notificacionViewModel.isLoading.collectAsState()
    val error by notificacionViewModel.error.collectAsState()
    val notificacionesRender = remember { mutableStateListOf<Notificacion>() }

    // Refrescar notificaciones cuando se entra a la pantalla
    LaunchedEffect(Unit) {
        try {
            notificacionViewModel.obtenerTodos()
        } catch (e: Exception) {
            // Error manejado por el ViewModel
        }
    }
    
    // Refrescar periódicamente para mantener sincronizado con otros dashboards
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(REFRESCO_NOVEDADES_RESIDENTE_MS)
            try {
                notificacionViewModel.obtenerTodosSilencioso()
            } catch (e: Exception) {
                // Error manejado por el ViewModel
            }
        }
    }

    // Actualiza por cambios de items para evitar recrear toda la vista.
    LaunchedEffect(notificaciones) {
        val nuevas = notificaciones.filter { notificacion ->
            if (!notificacion.esValida()) {
                return@filter false
            }
            if (esMensajeChatParaNovedadesResidente(notificacion.mensaje)) {
                return@filter false
            }
            val mensaje = notificacion.mensaje?.lowercase() ?: ""
            val esPaqueteria = (mensaje.contains("paquete") || mensaje.contains("paquetería") || mensaje.contains("transportadora")) &&
                (mensaje.contains("recoger") || mensaje.contains("portería") || mensaje.contains("disponible"))
            val tieneRecibo = mensaje.contains("recibo")
            val tieneTipoRecibo = mensaje.contains("enel") || mensaje.contains("vanti") || mensaje.contains("epz")
            val esRecibo = tieneRecibo && tieneTipoRecibo
            val esActualizacionQueja = (
                mensaje.contains("queja") &&
                    (mensaje.contains("estado") || mensaje.contains("proceso") || mensaje.contains("finalizada") || mensaje.contains("finalizado"))
                )
            !esPaqueteria && !esRecibo && !esActualizacionQueja
        }.reversed()

        val actualesPorId = notificacionesRender.withIndex()
            .associate { (index, item) -> (item.id ?: Long.MIN_VALUE - index.toLong()) to index }
        val nuevasClaves = mutableSetOf<Long>()

        nuevas.forEachIndexed { nuevoIndex, itemNuevo ->
            val clave = itemNuevo.id ?: Long.MIN_VALUE - nuevoIndex.toLong()
            nuevasClaves.add(clave)
            val indiceActual = actualesPorId[clave]

            if (indiceActual == null) {
                if (nuevoIndex <= notificacionesRender.lastIndex + 1) {
                    notificacionesRender.add(nuevoIndex, itemNuevo)
                } else {
                    notificacionesRender.add(itemNuevo)
                }
            } else {
                if (indiceActual != nuevoIndex) {
                    val movido = notificacionesRender.removeAt(indiceActual)
                    if (nuevoIndex <= notificacionesRender.lastIndex + 1) {
                        notificacionesRender.add(nuevoIndex, movido)
                    } else {
                        notificacionesRender.add(movido)
                    }
                }
                if (notificacionesRender[nuevoIndex] != itemNuevo) {
                    notificacionesRender[nuevoIndex] = itemNuevo
                }
            }
        }

        for (i in notificacionesRender.lastIndex downTo 0) {
            val clave = notificacionesRender[i].id ?: Long.MIN_VALUE - i.toLong()
            if (clave !in nuevasClaves) {
                notificacionesRender.removeAt(i)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisOscuro)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menú",
                tint = DoradoElegante,
                modifier = Modifier.clickable {
                    navController.navigate("PantallaMenuResidente")
                }
            )
            Text("Rafael 24H", color = Color.White, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = DoradoElegante,
                modifier = Modifier.clickable {
                    navController.navigate("PantallaNotificacionesResidente")
                }
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.conjunto),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Text(
                text = "Hacienda San Rafael",
                modifier = Modifier.padding(8.dp),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NOVEDADES", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("+", color = DoradoElegante, fontSize = 28.sp, modifier = Modifier.clickable {
                navController.navigate("PantallaNuevaPublicacion")
            })
        }

        // Mensaje de error si existe
        error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }

        // Lista de publicaciones/notificaciones
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else {
            if (notificacionesRender.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay publicaciones disponibles.",
                        color = GrisClaro,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = notificacionesRender.size,
                        key = { index -> 
                            val notificacion = notificacionesRender[index]
                            notificacion.id ?: run {
                                val mensajeHash = notificacion.mensajeSeguro().hashCode().toLong()
                                val fechaHash = notificacion.fechaEnvio?.hashCode()?.toLong() ?: 0L
                                index.toLong() * 1000000000L + mensajeHash * 1000L + fechaHash + index
                            }
                        }
                    ) { index ->
                        val notificacion = notificacionesRender[index]
                        PublicacionItemResidente(
                            notificacion = notificacion,
                            usuarioViewModel = hiltViewModel()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublicacionItemResidente(
    notificacion: Notificacion,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    
    // Obtener usuarios etiquetados
    val usuariosEtiquetadosIds = notificacion.obtenerUsuariosEtiquetados()
    val usuariosEtiquetadosNombres = remember(usuariosEtiquetadosIds, usuarios) {
        usuariosEtiquetadosIds.mapNotNull { id ->
            usuarios.find { it.id == id }?.nombre
        }
    }
    
    // Cargar usuarios si no están cargados
    LaunchedEffect(Unit) {
        if (usuarios.isEmpty() && usuariosEtiquetadosIds.isNotEmpty()) {
            try {
                usuarioViewModel.obtenerTodos()
            } catch (e: Exception) {
                // Error manejado por el ViewModel
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header con usuario y fecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DoradoElegante.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notificacion.usuario?.nombre ?: notificacion.usuario?.usuario ?: "Usuario",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    notificacion.fechaEnvio?.let { fecha ->
                        Text(
                            text = formatearFechaResidente(fecha),
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Mensaje/descripción
            val mensaje = notificacion.mensajeSeguro()
            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Mostrar usuarios etiquetados si existen
            if (usuariosEtiquetadosNombres.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Etiquetados: ",
                        color = GrisClaro,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    usuariosEtiquetadosNombres.forEachIndexed { index, nombre ->
                        Card(
                            modifier = Modifier.padding(end = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DoradoElegante.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "@$nombre",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Imagen si existe
            val imagenPath = notificacion.imagenUrl
            if (imagenPath != null && imagenPath.isNotBlank()) {
                val file = remember(imagenPath) { File(imagenPath) }
                val fileExists = remember(file) {
                    try {
                        file.exists() && file.canRead()
                    } catch (e: Exception) {
                        false
                    }
                }

                if (fileExists) {
                    Image(
                        painter = rememberAsyncImagePainter(file),
                        contentDescription = "Imagen de la publicación",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                    )
                }
            }
            
            // Video si existe
            val videoPath = notificacion.videoUrl
            if (videoPath != null && videoPath.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            try {
                                val videoUri: Uri = when {
                                    videoPath.startsWith("content://") -> {
                                        // URI de content provider - usar directamente
                                        Uri.parse(videoPath)
                                    }
                                    videoPath.startsWith("file://") -> {
                                        // URI de archivo - extraer la ruta y usar FileProvider
                                        val filePath = videoPath.removePrefix("file://")
                                        val file = File(filePath)
                                        if (file.exists()) {
                                            androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                        } else {
                                            Uri.parse(videoPath)
                                        }
                                    }
                                    videoPath.startsWith("/") -> {
                                        // Ruta absoluta de archivo
                                        val file = File(videoPath)
                                        if (file.exists() && file.canRead()) {
                                            // Usar FileProvider para archivos locales (requerido en Android 7+)
                                            androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                        } else {
                                            // Si el archivo no existe, intentar con URI desde archivo
                                            android.util.Log.w("DashboardResidente", "Video file no existe: $videoPath")
                                            Uri.fromFile(file)
                                        }
                                    }
                                    else -> {
                                        // Intentar como URI directo
                                        Uri.parse(videoPath)
                                    }
                                }
                                
                                android.util.Log.d("DashboardResidente", "Intentando reproducir video: $videoUri")
                                
                                // Crear Intent para reproducir video
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(videoUri, "video/*")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    // Otorgar permisos a todas las aplicaciones que manejen el intent
                                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                }
                                
                                // Verificar que haya una app que pueda reproducir el video
                                val resolveInfo = intent.resolveActivity(context.packageManager)
                                if (resolveInfo != null) {
                                    android.util.Log.d("DashboardResidente", "Iniciando reproductor de video")
                                    context.startActivity(intent)
                                } else {
                                    android.util.Log.e("DashboardResidente", "No hay aplicación para reproducir videos")
                                    // Intentar con un tipo MIME más genérico
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setData(videoUri)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(fallbackIntent)
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("DashboardResidente", "Error al abrir video: ${e.message}", e)
                                e.printStackTrace()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = GrisOscuro)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir video",
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tocar para reproducir video",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun formatearFechaResidente(fechaISO: String?): String {
    if (fechaISO == null || fechaISO.isBlank()) {
        return "Fecha no disponible"
    }
    return try {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        var date: Date? = null
        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                date = inputFormat.parse(fechaISO)
                if (date != null) break
            } catch (e: Exception) {
                // Continuar con el siguiente formato
            }
        }
        if (date != null) {
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            fechaISO
        }
    } catch (e: Exception) {
        fechaISO
    }
}

// Función para identificar notificaciones de paquetería
fun esNotificacionPaqueteria(notificacion: Notificacion): Boolean {
    val mensaje = notificacion.mensaje?.lowercase() ?: return false
    return (mensaje.contains("paquete") || mensaje.contains("paquetería") || mensaje.contains("transportadora")) && 
           (mensaje.contains("recoger") || mensaje.contains("portería") || mensaje.contains("disponible"))
}

// Función para identificar notificaciones de recibos
fun esNotificacionRecibo(notificacion: Notificacion): Boolean {
    val mensaje = notificacion.mensaje?.lowercase() ?: return false
    // Verificar que contenga "recibo" y uno de los tipos de recibo (ENEL, VANTI, EPZ)
    val tieneRecibo = mensaje.contains("recibo")
    val tieneEnel = mensaje.contains("enel")
    val tieneVanti = mensaje.contains("vanti")
    val tieneEpz = mensaje.contains("epz")
    val tieneTipoRecibo = tieneEnel || tieneVanti || tieneEpz
    
    // El mensaje debe contener "recibo" y alguno de los tipos
    return tieneRecibo && tieneTipoRecibo
}

private fun esMensajeChatParaNovedadesResidente(payload: String?): Boolean {
    return payload?.startsWith("CHAT|") == true
}
