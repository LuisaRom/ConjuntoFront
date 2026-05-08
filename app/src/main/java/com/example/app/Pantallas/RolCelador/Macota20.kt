package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.Model.Mascota
import com.example.app.ViewModel.MascotaViewModel
import com.example.app.ui.theme.AzulOscuro

@Composable
fun PantallaMascotasCelador(
    navController: NavController,
    mascotaViewModel: MascotaViewModel = hiltViewModel()
) {
    val mascotas by mascotaViewModel.mascotas.collectAsState()
    val isLoading by mascotaViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        mascotaViewModel.obtenerTodos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mascotas",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading && mascotas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (mascotas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay publicaciones de mascotas creadas.",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mascotas, key = { it.id ?: it.hashCode().toLong() }) { mascota ->
                    PublicacionMascotaSoloLectura(mascota)
                }
            }
        }
    }
}

@Composable
private fun PublicacionMascotaSoloLectura(mascota: Mascota) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mascota.usuario?.nombre ?: mascota.usuario?.usuario ?: "Residente",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nombre: ${mascota.nombre}", color = Color.White)
            Text("Tipo: ${mascota.tipo}", color = Color.LightGray, fontSize = 13.sp)
            Text("Detalle: ${mascota.raza}", color = Color.White, fontSize = 13.sp)

            val imagenUrl = resolverUrlImagenMascota(mascota.imagenUrl)
            if (!imagenUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = imagenUrl,
                    contentDescription = "Imagen de mascota",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

private fun resolverUrlImagenMascota(path: String?): String? {
    if (path.isNullOrBlank()) return null
    val limpio = path.trim()
    if (limpio.startsWith("http://") || limpio.startsWith("https://") || limpio.startsWith("content://")) {
        return limpio
    }
    return "https://conjuntoback.onrender.com/${limpio.removePrefix("/")}"
}
