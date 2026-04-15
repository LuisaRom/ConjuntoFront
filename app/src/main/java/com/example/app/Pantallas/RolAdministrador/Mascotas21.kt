package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Mascota
import com.example.app.ViewModel.MascotaViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import kotlinx.coroutines.launch

@Composable
fun PantallaMascotas(
    navController: NavController,
    mascotaViewModel: MascotaViewModel = hiltViewModel()
) {
    val mascotas by mascotaViewModel.mascotas.collectAsState()
    val isLoading by mascotaViewModel.isLoading.collectAsState()
    val error by mascotaViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mascotaAEliminar by remember { mutableStateOf<Mascota?>(null) }

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

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${mascotas.size} publicaciones",
            color = Color.LightGray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && mascotas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else if (mascotas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aún no hay publicaciones de mascotas creadas.",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                mascotas.forEach { mascota ->
                    PublicacionMascotaItem(
                        mascota = mascota,
                        onEliminar = { mascotaAEliminar = mascota }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 13.sp)
            }
        }
    }

    if (mascotaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mascotaAEliminar = null },
            title = { Text("Eliminar publicación", color = Color.White) },
            text = { Text("¿Deseas eliminar esta publicación de mascota?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    mascotaAEliminar?.id?.let { id ->
                        mascotaViewModel.eliminar(id)
                        scope.launch {
                            snackbarHostState.showSnackbar("Publicación eliminada correctamente.")
                        }
                    }
                    mascotaAEliminar = null
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mascotaAEliminar = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }
}

@Composable
private fun PublicacionMascotaItem(
    mascota: Mascota,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Nombre", color = Color.LightGray, fontSize = 12.sp)
            Text(mascota.nombre, color = Color.White, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(6.dp))
            Text("Tipo", color = Color.LightGray, fontSize = 12.sp)
            Text(mascota.tipo, color = Color.White)

            Spacer(modifier = Modifier.height(6.dp))
            Text("Raza", color = Color.LightGray, fontSize = 12.sp)
            Text(mascota.raza, color = Color.White)

            Spacer(modifier = Modifier.height(6.dp))
            Text("Residente", color = Color.LightGray, fontSize = 12.sp)
            Text(
                mascota.usuario?.nombre ?: mascota.usuario?.usuario ?: "Sin usuario",
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEliminar,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar publicación",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar publicación", color = Color.White)
            }
        }
    }
}
