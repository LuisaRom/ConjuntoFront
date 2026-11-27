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
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.app.Auth.AuthManager
import com.example.app.Auth.TerminosSesion
import com.example.app.Model.SeccionTerminos
import com.example.app.Model.TerminosCondiciones
import com.example.app.ViewModel.TerminosViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

object CierreSesion {
    var accion: () -> Unit = { AuthManager.logout() }
}

@Composable
fun PuertaTerminos(navController: NavController, contenido: @Composable () -> Unit) {
    val usuario by AuthManager.currentUser.collectAsState()
    val aceptados by TerminosSesion.aceptados.collectAsState()
    val clave = TerminosSesion.clave(usuario?.id, TerminosCondiciones.VERSION)

    if (usuario == null || clave in aceptados) {
        contenido()
        return
    }

    val viewModel: TerminosViewModel = hiltViewModel()
    val verificando by viewModel.verificando.collectAsState()
    val enviando by viewModel.enviando.collectAsState()
    val error by viewModel.error.collectAsState()
    var primeraVerificacion by remember(usuario?.id) { mutableStateOf(true) }

    LaunchedEffect(usuario?.id) {
        viewModel.verificar()
        primeraVerificacion = false
    }

    if (verificando || primeraVerificacion) {
        Box(Modifier.fillMaxSize().background(AzulOscuro), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DoradoElegante)
        }
        return
    }

    var aceptaTerminos by remember { mutableStateOf(false) }
    var autorizaDatos by remember { mutableStateOf(false) }
    var aceptaAvisoIA by remember { mutableStateOf(false) }
    val completo = aceptaTerminos && autorizaDatos && aceptaAvisoIA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        EncabezadoTerminos(usuario?.rol, "Antes de continuar, lee y acepta los términos de uso de la aplicación.")

        ListaSecciones(
            secciones = TerminosCondiciones.secciones(usuario?.rol),
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(8.dp)
        ) {
            Casilla(aceptaTerminos, { aceptaTerminos = it },
                "He leído y acepto los Términos y Condiciones de uso, incluido el uso adecuado de la plataforma para mi rol.")
            Casilla(autorizaDatos, { autorizaDatos = it },
                "Autorizo de manera previa, expresa e informada el tratamiento de mis datos personales conforme a la Ley 1581 de 2012.")
            Casilla(aceptaAvisoIA, { aceptaAvisoIA = it },
                "Entiendo que el asistente de IA responde según la información de mi rol y es de uso exclusivo para asuntos del conjunto.")
        }

        error?.let {
            Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    CierreSesion.accion()
                    navController.navigate("PantallaSeleccionRol") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                enabled = !enviando,
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Rechazar y salir", color = GrisClaro)
            }
            Button(
                onClick = { viewModel.aceptar() },
                enabled = completo && !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante, contentColor = AzulOscuro),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                if (enviando) {
                    CircularProgressIndicator(color = AzulOscuro, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Aceptar y continuar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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

@Composable
private fun Casilla(marcada: Boolean, onCambio: (Boolean) -> Unit, texto: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCambio(!marcada) }
            .padding(vertical = 2.dp)
    ) {
        Checkbox(
            checked = marcada,
            onCheckedChange = onCambio,
            colors = CheckboxDefaults.colors(
                checkedColor = DoradoElegante,
                uncheckedColor = GrisClaro.copy(alpha = 0.7f),
                checkmarkColor = AzulOscuro
            )
        )
        Text(texto, color = GrisClaro, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 12.dp))
    }
}
