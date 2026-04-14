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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LocalTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.AccesoPeatonal
import com.example.app.ViewModel.AccesoPeatonalViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PantallaAccesoPeatonalResidente(
    navController: NavController,
    accesoPeatonalViewModel: AccesoPeatonalViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by accesoPeatonalViewModel.isLoading.collectAsState()
    val error by accesoPeatonalViewModel.error.collectAsState()
    
    var nombreVisitante by remember { mutableStateOf("") }
    var torre by remember { mutableStateOf(usuarioActual?.torre ?: "") }
    var apartamento by remember { mutableStateOf(usuarioActual?.apartamento ?: "") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var accesoGuardado by remember { mutableStateOf<AccesoPeatonal?>(null) }

    val context = LocalContext.current

    // Mostrar errores
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            accesoPeatonalViewModel.clearError()
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Acceso Peatonal", fontSize = 20.sp, color = GrisClaro)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoAccesoP("Nombre del Visitante", nombreVisitante) { nombreVisitante = it }
        CampoAccesoP("Torre", torre) { torre = it }
        CampoAccesoP("Apartamento", apartamento) { apartamento = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nombreVisitante.isBlank() || torre.isBlank() || apartamento.isBlank()) {
                    Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Generar código QR único
                val codigoQR = "PEAT-${System.currentTimeMillis()}-${torre}-${apartamento}"
                
                // Crear el acceso peatonal
                val acceso = AccesoPeatonal(
                    nombreVisitante = nombreVisitante,
                    torre = torre,
                    apartamento = apartamento,
                    codigoQr = codigoQR,
                    autorizadoPor = usuarioActual,
                    horaAutorizada = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    horaEntrada = null,
                    horaSalida = null
                )

                // Guardar en el backend
                accesoPeatonalViewModel.guardarAccesoPeatonal(acceso)
                accesoGuardado = acceso
                qrBitmap = null
                Toast.makeText(context, "Acceso peatonal creado", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AzulOscuro
                )
            } else {
                Text("Crear Acceso", color = AzulOscuro)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (accesoGuardado != null) {
            Button(
                onClick = {
                    val acceso = accesoGuardado ?: return@Button
                    val payload = construirPayloadQrPeatonal(acceso)
                    qrBitmap = generarCodigoQR(payload)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Generar QR", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        qrBitmap?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Código QR de Acceso",
                    color = GrisClaro,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Código QR",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Muestre este código al celador",
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

fun generarCodigoQR(texto: String): Bitmap {
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

private fun construirPayloadQrPeatonal(acceso: AccesoPeatonal): String {
    return listOf(
        "tipo=PEATONAL",
        "codigo=${acceso.codigoQr}",
        "visitante=${acceso.nombreVisitante}",
        "torre=${acceso.torre}",
        "apartamento=${acceso.apartamento}",
        "autoriza=${acceso.autorizadoPor?.nombre ?: acceso.autorizadoPor?.usuario ?: ""}",
        "fecha=${acceso.horaAutorizada ?: ""}"
    ).joinToString("|")
}

@Composable
fun CampoAccesoP(label: String, valor: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                cursorColor = DoradoElegante
            )
        )
    }
}
