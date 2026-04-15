package com.example.app.Pantallas.RolResidente

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.AccesoVehicular
import com.example.app.ViewModel.AccesoVehicularViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAccesoVehicularResidente(
    navController: NavController,
    accesoVehicularViewModel: AccesoVehicularViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by accesoVehicularViewModel.isLoading.collectAsState()
    val error by accesoVehicularViewModel.error.collectAsState()

    var torreApto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var tipoVehiculo by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var quienIngresa by remember { mutableStateOf("") }
    var quienAutoriza by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var accesoGuardado by remember { mutableStateOf<AccesoVehicular?>(null) }

    val opcionesVehiculo = listOf("Automóvil", "Camioneta", "Moto", "Eléctrico")
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            accesoVehicularViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Acceso Vehicular", color = GrisClaro, style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoAcceso("Torre - Apartamento", torreApto) { torreApto = it }
        CampoAcceso("Fecha", fecha) { fecha = it }
        CampoAcceso("Horario", horario) { horario = it }

        Text("Tipo de Vehículo", color = Color.White)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = tipoVehiculo,
                onValueChange = {},
                label = { Text("Selecciona el tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AzulOscuro,
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                opcionesVehiculo.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            tipoVehiculo = opcion
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        CampoAcceso("Placa", placa) { placa = it }
        CampoAcceso("¿Quién ingresa?", quienIngresa) { quienIngresa = it }
        CampoAcceso("¿Quién autoriza?", quienAutoriza) {
            quienAutoriza = it
            torreApto = if (torreApto.isBlank() && usuarioActual != null) {
                "${usuarioActual?.torre ?: ""}-${usuarioActual?.apartamento ?: ""}"
            } else torreApto
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (placa.isBlank() || torreApto.isBlank() || quienIngresa.isBlank()) {
                    Toast.makeText(context, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val partes = torreApto.split("-", " ").filter { it.isNotBlank() }
                val torre = partes.firstOrNull() ?: (usuarioActual?.torre ?: "")
                val apto = partes.getOrNull(1) ?: (usuarioActual?.apartamento ?: "")
                val codigoUnico = "VEH-${System.currentTimeMillis()}-${placa.uppercase()}"

                val acceso = AccesoVehicular(
                    placaVehiculo = placa.uppercase(),
                    torre = torre,
                    apartamento = apto,
                    codigoQr = codigoUnico,
                    autorizadoPor = usuarioActual,
                    horaAutorizada = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date()),
                    horaEntrada = null,
                    horaSalida = null
                )
                accesoVehicularViewModel.guardarAccesoVehicular(acceso)
                accesoGuardado = acceso
                qrBitmap = null
                Toast.makeText(context, "Acceso vehicular creado", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoading) "Guardando..." else "Crear Acceso",
                color = AzulOscuro,
                fontWeight = FontWeight.Bold
            )
        }

        if (accesoGuardado != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val acceso = accesoGuardado ?: return@Button
                    val payload = construirPayloadQrVehicular(acceso, tipoVehiculo, quienIngresa, quienAutoriza)
                    qrBitmap = generarCodigoQRVehicular(payload)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Generar QR", color = Color.White)
            }
        }

        qrBitmap?.let {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Código QR de Acceso", color = GrisClaro, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Código QR Vehicular",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Muéstralo al celador para escaneo", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoAcceso(label: String, valor: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
    }
}

private fun construirPayloadQrVehicular(
    acceso: AccesoVehicular,
    tipoVehiculo: String,
    quienIngresa: String,
    quienAutoriza: String
): String {
    return listOf(
        "tipo=VEHICULAR",
        "codigo=${acceso.codigoQr ?: ""}",
        "placa=${acceso.placaVehiculo}",
        "torre=${acceso.torre}",
        "apartamento=${acceso.apartamento}",
        "vehiculo=$tipoVehiculo",
        "ingresa=$quienIngresa",
        "autoriza=$quienAutoriza",
        "fecha=${acceso.horaAutorizada ?: ""}"
    ).joinToString("|")
}

fun generarCodigoQRVehicular(texto: String): Bitmap {
    val size = 512
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            val color = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
            bitmap.setPixel(x, y, color)
        }
    }
    return bitmap
}