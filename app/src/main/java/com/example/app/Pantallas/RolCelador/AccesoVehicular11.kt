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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.app.Model.AccesoVehicular
import com.example.app.ViewModel.AccesoVehicularViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.GrisClaro
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantallaAccesoVehicularCelador(
    navController: NavController,
    accesoVehicularViewModel: AccesoVehicularViewModel = hiltViewModel()
) {
    val accesos by accesoVehicularViewModel.accesosVehiculares.collectAsState()
    val isLoading by accesoVehicularViewModel.isLoading.collectAsState()
    val hoyIso = remember { LocalDate.now().format(DateTimeFormatter.ISO_DATE) }
    val accesosDelDia = remember(accesos, hoyIso) {
        accesos.filter { it.horaAutorizada?.startsWith(hoyIso) == true }
    }
    var accesoSeleccionado by remember { mutableStateOf<AccesoVehicular?>(null) }
    var mensajeEstado by remember { mutableStateOf("Escanea un QR para consultar un acceso vehicular de hoy.") }
    var accesoValido by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        accesoVehicularViewModel.obtenerAccesosVehiculares()
    }

    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val codigo = result.contents?.trim().orEmpty()
        if (codigo.isBlank()) {
            mensajeEstado = "Escaneo cancelado."
            accesoValido = null
            return@rememberLauncherForActivityResult
        }
        accesoSeleccionado = accesosDelDia.firstOrNull { acceso ->
            acceso.id?.toString() == codigo ||
                acceso.placaVehiculo.equals(codigo, ignoreCase = true) ||
                codigo.uppercase(Locale.ROOT).contains(acceso.placaVehiculo.uppercase(Locale.ROOT))
        }
        mensajeEstado = if (accesoSeleccionado != null) {
            "Acceso encontrado."
        } else {
            "No existe un acceso vehicular del día para ese código."
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
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = GrisClaro,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acceso Vehicular",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GrisClaro
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Detalles", color = GrisClaro)

            Spacer(modifier = Modifier.height(12.dp))
            DetalleCampo("Torre - Apto", "${accesoSeleccionado?.torre ?: "-"} - ${accesoSeleccionado?.apartamento ?: "-"}")
            DetalleCampo("Fecha", accesoSeleccionado?.horaAutorizada?.take(10) ?: "-")
            DetalleCampo(
                "Horario",
                accesoSeleccionado?.horaEntrada ?: accesoSeleccionado?.horaAutorizada?.drop(11)?.take(8) ?: "-"
            )
            DetalleCampo("Vehículo", "Vehicular")
            DetalleCampo("Placa", accesoSeleccionado?.placaVehiculo ?: "-")
            DetalleCampo("Residente", accesoSeleccionado?.autorizadoPor?.nombre ?: "-")
            DetalleCampo("Visitante", accesoSeleccionado?.visitante?.nombre ?: "-")
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

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val options = ScanOptions().apply {
                        setPrompt("Escanea el código QR del acceso")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    qrLauncher.launch(options)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Escanear QR", color = Color.White)
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = GrisClaro, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
@Composable
fun DetalleCampo(label: String, valor: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, color = Color.LightGray, fontSize = 12.sp)
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