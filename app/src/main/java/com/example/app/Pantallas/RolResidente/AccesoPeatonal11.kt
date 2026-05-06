package com.example.app.Pantallas.RolResidente

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@Composable
fun PantallaAccesoPeatonalResidente(
    navController: NavController,
    accesoPeatonalViewModel: AccesoPeatonalViewModel = hiltViewModel(),
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by accesoPeatonalViewModel.isLoading.collectAsState()
    val error by accesoPeatonalViewModel.error.collectAsState()
    val ultimoAccesoGuardado by accesoPeatonalViewModel.ultimoAccesoGuardado.collectAsState()
    
    var nombreVisitante by remember { mutableStateOf("") }
    var torre by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var accesoGuardado by remember { mutableStateOf<AccesoPeatonal?>(null) }
    val torreApartamento = remember(torre, apartamento) {
        listOf(torre, apartamento)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
    }

    val context = LocalContext.current

    // Mostrar errores
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            accesoPeatonalViewModel.clearError()
        }
    }

    // Cargar torre/apartamento automáticamente del usuario autenticado
    LaunchedEffect(usuarioActual?.id, usuarioActual?.torre, usuarioActual?.apartamento) {
        torre = usuarioActual?.torre.orEmpty()
        apartamento = usuarioActual?.apartamento.orEmpty()
    }

    // Solo marcar éxito cuando el endpoint responde correctamente
    LaunchedEffect(ultimoAccesoGuardado?.id, ultimoAccesoGuardado?.codigoQr) {
        ultimoAccesoGuardado?.let {
            accesoGuardado = it
            qrBitmap = null
            Toast.makeText(context, "Acceso peatonal creado", Toast.LENGTH_SHORT).show()
            accesoPeatonalViewModel.clearUltimoAccesoGuardado()
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
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Acceso Peatonal", fontSize = 20.sp, color = GrisClaro)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CampoAccesoP("Torre - Apartamento", torreApartamento, enabled = false)
        CampoAccesoP("Nombre del Visitante *", nombreVisitante) { nombreVisitante = it }
        CampoFechaConCalendarioPeatonal(
            label = "Fecha *",
            valor = fecha,
            onDateSelected = { fecha = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nombreVisitante.isBlank() || torre.isBlank() || apartamento.isBlank() || fecha.isBlank()) {
                    Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Generar código QR único
                val codigoQR = "PEAT-${System.currentTimeMillis()}-${torre}-${apartamento}"
                
                // Crear el acceso peatonal
                val acceso = AccesoPeatonal(
                    nombreVisitante = nombreVisitante,
                    fecha = fecha.toIsoDateTimeOrNowPeatonal(),
                    torre = torre,
                    apartamento = apartamento,
                    codigoQr = codigoQR,
                    autorizadoPor = usuarioActual,
                    horaAutorizada = fecha.toIsoDateTimeOrNowPeatonal(),
                    horaEntrada = null,
                    horaSalida = null
                )

                // Guardar en el backend
                accesoPeatonalViewModel.guardarAccesoPeatonal(acceso)
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
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante)
            ) {
                Text("Generar QR", color = AzulOscuro)
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

@Composable
private fun CampoFechaConCalendarioPeatonal(
    label: String,
    valor: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = onDateSelected,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val fechaSeleccionada = String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                            )
                            onDateSelected(fechaSeleccionada)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Abrir calendario",
                        tint = GrisClaro
                    )
                }
            },
            readOnly = false,
            singleLine = true,
            textStyle = TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                disabledBorderColor = GrisClaro,
                disabledTextColor = Color.White,
                cursorColor = DoradoElegante
            )
        )
    }
}

private fun String.toIsoDateTimeOrNowPeatonal(): String {
    if (isBlank()) {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    }
    return try {
        val parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(this)
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(parsedDate ?: Date())
    } catch (_: Exception) {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
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
fun CampoAccesoP(
    label: String,
    valor: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = LightGray, fontSize = 12.sp)
        OutlinedTextField(
            value = valor,
            onValueChange = { if (enabled) onChange(it) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = !enabled,
            textStyle = TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                disabledBorderColor = GrisClaro,
                disabledTextColor = Color.White,
                cursorColor = DoradoElegante
            )
        )
    }
}
