package com.example.app.Pantallas.RolResidente

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val accesoLabelStyle = TextStyle(
    color = Color.LightGray,
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp
)

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
    val ultimoAccesoGuardado by accesoVehicularViewModel.ultimoAccesoGuardado.collectAsState()
    val accesoSeleccionado by accesoVehicularViewModel.accesoSeleccionado.collectAsState()

    var torre by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var tipoVehiculo by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var quienIngresa by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var accesoGuardado by remember { mutableStateOf<AccesoVehicular?>(null) }

    val opcionesVehiculo = listOf("Automóvil", "Camioneta", "Moto", "Eléctrico")
    var expanded by remember { mutableStateOf(false) }
    val torreApartamento = remember(torre, apartamento) {
        listOf(torre, apartamento)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            accesoVehicularViewModel.clearError()
        }
    }

    LaunchedEffect(usuarioActual?.id, usuarioActual?.torre, usuarioActual?.apartamento) {
        torre = usuarioActual?.torre.orEmpty()
        apartamento = usuarioActual?.apartamento.orEmpty()
    }

    LaunchedEffect(ultimoAccesoGuardado?.id, ultimoAccesoGuardado?.codigoQr) {
        ultimoAccesoGuardado?.let {
            accesoGuardado = it
            qrBitmap = null
            Toast.makeText(context, "Acceso vehicular creado", Toast.LENGTH_SHORT).show()
            accesoVehicularViewModel.clearUltimoAccesoGuardado()
        }
    }

    LaunchedEffect(accesoSeleccionado?.id, accesoSeleccionado?.qrBase64, accesoSeleccionado?.qrDataUrl) {
        accesoSeleccionado?.let { acceso ->
            val qrDesdeBackend = extraerBitmapQr(acceso)
            if (qrDesdeBackend != null) {
                qrBitmap = qrDesdeBackend
            } else {
                Toast.makeText(
                    context,
                    "El endpoint no devolvió un QR válido (qrBase64/qrDataUrl)",
                    Toast.LENGTH_LONG
                ).show()
            }
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

        CampoAcceso("Torre - Apartamento", torreApartamento, enabled = false)
        CampoFechaConCalendario(
            label = "Fecha *",
            valor = fecha,
            onDateSelected = { fecha = it }
        )

        Text("Tipo Vehículo *", style = accesoLabelStyle)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = tipoVehiculo,
                onValueChange = {},
                label = { Text("Selecciona el tipo", style = accesoLabelStyle) },
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
        CampoAcceso("Placa *", placa) { placa = it }
        CampoAcceso("Quién ingresa *", quienIngresa) { quienIngresa = it }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (fecha.isBlank() || tipoVehiculo.isBlank() || placa.isBlank() || quienIngresa.isBlank()) {
                    Toast.makeText(context, "Completa fecha, tipo de vehículo, placa y quién ingresa", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val codigoUnico = "VEH-${System.currentTimeMillis()}-${placa.uppercase()}"

                val acceso = AccesoVehicular(
                    placaVehiculo = placa.uppercase(),
                    tipoVehiculo = tipoVehiculo,
                    quienIngresa = quienIngresa,
                    fecha = fecha.toIsoDateTimeOrNow(),
                    torre = torre,
                    apartamento = apartamento,
                    codigoQr = codigoUnico,
                    autorizadoPor = usuarioActual,
                    horaAutorizada = fecha.toIsoDateTimeOrNow(),
                    horaEntrada = null,
                    horaSalida = null
                )
                accesoVehicularViewModel.guardarAccesoVehicular(acceso)
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
                    val accesoId = acceso.id
                    if (accesoId == null) {
                        Toast.makeText(context, "No se pudo obtener el id del acceso", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    accesoVehicularViewModel.obtenerAccesoVehicular(accesoId)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) "Consultando..." else "Generar QR",
                    color = AzulOscuro,
                    fontWeight = FontWeight.Bold
                )
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
fun CampoAcceso(
    label: String,
    valor: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = accesoLabelStyle)
        OutlinedTextField(
            value = valor,
            onValueChange = { if (enabled) onChange(it) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = !enabled,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                disabledBorderColor = GrisClaro,
                cursorColor = DoradoElegante,
                focusedLabelColor = GrisClaro,
                unfocusedLabelColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White
            )
        )
    }
}

@Composable
private fun CampoFechaConCalendario(
    label: String,
    valor: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = accesoLabelStyle)
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DoradoElegante,
                unfocusedBorderColor = GrisClaro,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = DoradoElegante
            )
        )
    }
}

private fun String.toIsoDateTimeOrNow(): String {
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

private fun extraerBitmapQr(acceso: AccesoVehicular): Bitmap? {
    val base64Raw = when {
        !acceso.qrDataUrl.isNullOrBlank() && acceso.qrDataUrl.contains(",") ->
            acceso.qrDataUrl.substringAfter(",")
        !acceso.qrBase64.isNullOrBlank() ->
            acceso.qrBase64
        else -> null
    } ?: return null

    return try {
        val bytes = Base64.decode(base64Raw, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: IllegalArgumentException) {
        null
    }
}