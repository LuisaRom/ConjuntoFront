package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.AccesoPeatonal
import com.example.app.ViewModel.AccesoPeatonalViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaAccesoPeatonalCelador(
    navController: NavController,
    accesoPeatonalViewModel: AccesoPeatonalViewModel = hiltViewModel()
) {
    val accesos by accesoPeatonalViewModel.accesosPeatonales.collectAsState()
    val isLoading by accesoPeatonalViewModel.isLoading.collectAsState()
    val hoyIso = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val accesosDelDia = remember(accesos, hoyIso) {
        accesos.filter { it.horaAutorizada?.startsWith(hoyIso) == true }
    }
    var accesoSeleccionado by remember { mutableStateOf<AccesoPeatonal?>(null) }
    var mensajeEstado by remember { mutableStateOf("Escanea un QR para consultar un acceso peatonal de hoy.") }
    var accesoValido by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        accesoPeatonalViewModel.obtenerAccesosPeatonales()
    }

    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val codigo = result.contents?.trim().orEmpty()
        if (codigo.isBlank()) {
            mensajeEstado = "Escaneo cancelado."
            accesoValido = null
            return@rememberLauncherForActivityResult
        }
        accesoSeleccionado = accesosDelDia.firstOrNull { acceso ->
            acceso.codigoQr.equals(codigo, ignoreCase = true) ||
                codigo.contains(acceso.codigoQr ?: "", ignoreCase = true) ||
                acceso.id?.toString() == codigo
        }
        accesoSeleccionado?.id?.let { accesoPeatonalViewModel.obtenerAccesoPeatonal(it) }
        mensajeEstado = if (accesoSeleccionado != null) {
            "Acceso encontrado."
        } else {
            "No existe un acceso peatonal del día para ese QR."
        }
        accesoValido = accesoSeleccionado != null
    }

    Scaffold(
        containerColor = AzulOscuro
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(AzulOscuro)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = GrisClaro,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Acceso Peatonal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GrisClaro
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Detalles", color = GrisClaro)

            Spacer(modifier = Modifier.height(12.dp))
            EtiquetaCampo(
                label = "Torre - Apto",
                valor = "${accesoSeleccionado?.torre ?: "-"} - ${accesoSeleccionado?.apartamento ?: "-"}"
            )
            EtiquetaCampo(label = "Fecha", valor = accesoSeleccionado?.horaAutorizada?.take(10) ?: "-")
            EtiquetaCampo(
                label = "Hora",
                valor = accesoSeleccionado?.horaEntrada ?: accesoSeleccionado?.horaAutorizada?.drop(11)?.take(8) ?: "-"
            )
            EtiquetaCampo(label = "Residente", valor = accesoSeleccionado?.autorizadoPor?.nombre ?: "-")
            EtiquetaCampo(label = "Visitante", valor = accesoSeleccionado?.nombreVisitante ?: "-")
            EtiquetaCampo(label = "Código QR", valor = accesoSeleccionado?.codigoQr ?: "-")
            Text(
                text = mensajeEstado,
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            accesoValido?.let { valido ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (valido) "Aprobado" else "Rechazado",
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = if (valido) Color(0xFF2E7D32) else Color(0xFFC62828),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    // Refresca desde backend antes de escanear.
                    accesoPeatonalViewModel.obtenerAccesosPeatonales()
                    val options = ScanOptions().apply {
                        setPrompt("Escanea el código QR del acceso")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    qrLauncher.launch(options)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Escanear QR",
                    tint = AzulOscuro
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear QR", color = AzulOscuro)
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = GrisClaro, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
@Composable
fun EtiquetaCampo(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        Text(
            text = valor,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                .padding(12.dp)
        )
    }
}
