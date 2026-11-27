package com.example.app.Pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.app.Auth.AuthManager
import com.example.app.Model.SeccionTerminos
import com.example.app.Model.TerminosCondiciones
import com.example.app.ViewModel.TerminosViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaTerminos(navController: NavController, viewModel: TerminosViewModel = hiltViewModel()) {
    val usuario by AuthManager.currentUser.collectAsState()
    val fecha by viewModel.fechaAceptacion.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarEstado() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = GrisClaro)
            }
            Text("Términos y condiciones", color = GrisClaro, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        EncabezadoTerminos(
            usuario?.rol,
            fecha?.let { "Aceptados el ${it.take(16).replace("T", " a las ")}." } ?: "Versión vigente de los términos de uso."
        )
        ListaSecciones(TerminosCondiciones.secciones(usuario?.rol), Modifier.weight(1f))
    }
}

@Composable
fun DialogoTerminos(rol: String?, onCerrar: () -> Unit, onAceptar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AzulOscuro)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCerrar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar", tint = GrisClaro)
                }
                Text("Términos y condiciones", color = GrisClaro, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            EncabezadoTerminos(rol, "Uso de la plataforma, tratamiento de datos personales y aviso del asistente de IA.")
            ListaSecciones(TerminosCondiciones.secciones(rol), Modifier.weight(1f))
            Button(
                onClick = onAceptar,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante, contentColor = AzulOscuro),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(50.dp)
            ) {
                Text("Leí y acepto", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EnlaceTerminos(navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { navController.navigate("PantallaTerminos") }
            .padding(14.dp)
    ) {
        Icon(Icons.Default.Description, contentDescription = null, tint = DoradoElegante)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Términos y condiciones", color = GrisClaro, fontWeight = FontWeight.Bold)
            Text("Uso de la plataforma, tratamiento de datos y asistente IA", color = GrisClaro.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun EncabezadoTerminos(rol: String?, subtitulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(Icons.Default.Gavel, contentDescription = null, tint = DoradoElegante, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(TerminosCondiciones.CONJUNTO, color = GrisClaro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Rol: ${TerminosCondiciones.nombreRol(rol)} · Versión ${TerminosCondiciones.VERSION} del ${TerminosCondiciones.VIGENCIA}",
                color = DoradoElegante,
                fontSize = 12.sp
            )
        }
    }
    Text(subtitulo, color = GrisClaro.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun ListaSecciones(secciones: List<SeccionTerminos>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f)),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(secciones) { seccion ->
            Column {
                Text(seccion.titulo, color = DoradoElegante, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                seccion.parrafos.forEach { p ->
                    Row(Modifier.padding(bottom = 6.dp)) {
                        Text("•", color = GrisClaro.copy(alpha = 0.6f), fontSize = 13.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(p, color = GrisClaro, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }
    }
}
