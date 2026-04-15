package com.example.app.Pantallas.RolResidente

import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.R
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaPagos(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    var metodoSeleccionado by remember { mutableStateOf("EFECTIVO") }
    
    // Estado para los campos del formulario
    val torreApartamento = remember(usuarioActual) {
        if (usuarioActual?.torre != null && usuarioActual?.apartamento != null) {
            "${usuarioActual?.torre} - ${usuarioActual?.apartamento}"
        } else {
            ""
        }
    }
    val nombre = remember(usuarioActual) { usuarioActual?.nombre ?: "" }
    val id = remember(usuarioActual) { usuarioActual?.id?.toString() ?: "" }
    
    // Fecha actual formateada
    val fechaActual = remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date())
    }
    
    // Mes actual en español
    val mesActual = remember {
        val meses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        meses[Calendar.getInstance().get(Calendar.MONTH)]
    }
    
    // Monto por defecto
    val monto = remember { "200.000 COP" }
    // Se incluirá en el PDF y en pago PSE cuando el flujo de reservas lo alimente.
    val totalServiciosAdicionales = remember { "0 COP" }
    
    // Cargar usuario si no está disponible
    LaunchedEffect(Unit) {
        if (usuarioActual == null) {
            usuarioViewModel.obtenerTodos()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pagos", color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Campo("Torre - Apartamento", torreApartamento)
        Campo("Nombre", nombre)
        Campo("Id", id)
        Campo("Fecha", "$fechaActual - $mesActual")
        Campo("Monto", monto)
        Campo("Servicios adicionales", totalServiciosAdicionales)

        Spacer(modifier = Modifier.height(24.dp))
        Text("Método de Pago", color = Color.White)

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetodoPagoItem(
                tipo = "EFECTIVO",
                seleccionado = metodoSeleccionado == "EFECTIVO",
                modifier = Modifier.weight(1f)
            ) {
                metodoSeleccionado = "EFECTIVO"
                scope.launch {
                    val resultado = generarPDF(
                        context = context,
                        torreApartamento = torreApartamento,
                        nombre = nombre,
                        id = id,
                        fecha = "$fechaActual - $mesActual",
                        monto = monto,
                        totalServiciosAdicionales = totalServiciosAdicionales,
                        concepto = "Pago Administración"
                    )
                    if (resultado != null) {
                        val nombreArchivo = resultado.split("/").lastOrNull() ?: "Recibo.pdf"
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, "PDF generado: $nombreArchivo", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, "Error al generar PDF. Revisa los logs.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            MetodoPagoItem(
                tipo = "EN LINEA",
                seleccionado = metodoSeleccionado == "EN LINEA",
                modifier = Modifier.weight(1f)
            ) {
                metodoSeleccionado = "EN LINEA"
                Toast.makeText(context, "Redirigiendo a PSE. Servicios adicionales: $totalServiciosAdicionales", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MetodoPagoItem(
    tipo: String,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fondo = if (seleccionado) DoradoElegante else DoradoElegante
    Column(
        modifier = modifier
            .height(115.dp)
            .background(fondo, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = if (tipo == "EFECTIVO") R.drawable.money else R.drawable.pay),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
        Text(tipo, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            if (tipo == "EFECTIVO") "Opcion 1" else "Opcion 2",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun Campo(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = GrisClaro, fontSize = 12.sp)
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

suspend fun generarPDF(
    context: android.content.Context,
    torreApartamento: String,
    nombre: String,
    id: String,
    fecha: String,
    monto: String,
    totalServiciosAdicionales: String,
    concepto: String
): String? = withContext(Dispatchers.IO) {
    var pdfDocument: PdfDocument? = null
    var fileOutputStream: FileOutputStream? = null
    
    try {
        android.util.Log.d("PDF", "=== INICIANDO GENERACIÓN DE PDF ===")
        android.util.Log.d("PDF", "Nombre: $nombre, Torre-Apto: $torreApartamento, Fecha: $fecha")
        
        pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size (72 DPI)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Marca de agua
        val paintWatermark = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(40, 60, 60, 60)
            textSize = 52f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.save()
        canvas.rotate(-30f, pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f)
        canvas.drawText("Hacienda San Rafael", pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f, paintWatermark)
        canvas.restore()
        
        // Configurar pintura
        val paint = android.graphics.Paint()
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        
        val paintTitle = android.graphics.Paint()
        paintTitle.color = android.graphics.Color.BLACK
        paintTitle.textSize = 24f
        paintTitle.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        
        val paintLabel = android.graphics.Paint()
        paintLabel.color = android.graphics.Color.BLACK
        paintLabel.textSize = 14f
        paintLabel.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        
        var yPos = 80f

        // Logo en encabezado
        try {
            val logoBitmapOriginal = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.conjunto)
            val logoBitmap = android.graphics.Bitmap.createScaledBitmap(logoBitmapOriginal, 72, 72, true)
            canvas.drawBitmap(logoBitmap, 50f, 35f, null)
        } catch (e: Exception) {
            android.util.Log.w("PDF", "No se pudo cargar logo para PDF: ${e.message}")
        }

        // Título
        canvas.drawText("RECIBO DE PAGO - ADMINISTRACIÓN", 140f, yPos, paintTitle)
        yPos += 70f
        
        // Línea separadora
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 40f
        
        // Información del formulario
        canvas.drawText("Torre - Apartamento:", 50f, yPos, paintLabel)
        canvas.drawText(torreApartamento.ifEmpty { "N/A" }, 250f, yPos, paint)
        yPos += 40f
        
        canvas.drawText("Nombre:", 50f, yPos, paintLabel)
        canvas.drawText(nombre.ifEmpty { "N/A" }, 250f, yPos, paint)
        yPos += 40f
        
        canvas.drawText("ID:", 50f, yPos, paintLabel)
        canvas.drawText(id.ifEmpty { "N/A" }, 250f, yPos, paint)
        yPos += 40f
        
        canvas.drawText("Fecha:", 50f, yPos, paintLabel)
        canvas.drawText(fecha, 250f, yPos, paint)
        yPos += 40f
        
        // Línea separadora
        yPos += 20f
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 40f
        
        // Monto destacado
        val paintMonto = android.graphics.Paint()
        paintMonto.color = android.graphics.Color.BLACK
        paintMonto.textSize = 20f
        paintMonto.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        
        canvas.drawText("Monto a Pagar:", 50f, yPos, paintLabel)
        canvas.drawText(monto, 250f, yPos, paintMonto)
        yPos += 40f
        canvas.drawText("Servicios Adicionales:", 50f, yPos, paintLabel)
        canvas.drawText(totalServiciosAdicionales, 250f, yPos, paintMonto)
        yPos += 60f

        canvas.drawText("Concepto:", 50f, yPos, paintLabel)
        canvas.drawText(concepto, 250f, yPos, paint)
        yPos += 50f
        
        // Línea separadora final
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 40f
        
        // Método de pago
        canvas.drawText("Método de Pago: EFECTIVO", 50f, yPos, paintLabel)
        yPos += 60f
        
        // Nota
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
        canvas.drawText("Este es un comprobante de pago generado automáticamente.", 50f, yPos, paint)
        yPos += 30f
        canvas.drawText("Por favor, conserve este documento como comprobante.", 50f, yPos, paint)

        // QR con información del pago
        val torre = torreApartamento.split("-").getOrNull(0)?.trim().orEmpty()
        val apto = torreApartamento.split("-").getOrNull(1)?.trim().orEmpty()
        val qrPayload = listOf(
            "residente=$nombre",
            "apartamento=$apto",
            "torre=$torre",
            "monto=$monto",
            "fecha=$fecha",
            "concepto=$concepto"
        ).joinToString("|")
        try {
            val qrBitmap = generarQrBitmapParaPdf(qrPayload, 170)
            canvas.drawBitmap(qrBitmap, 370f, 620f, null)
            paint.textSize = 10f
            canvas.drawText("QR de validación de pago", 375f, 805f, paint)
        } catch (e: Exception) {
            android.util.Log.e("PDF", "Error generando QR para PDF: ${e.message}", e)
        }
        
        pdfDocument.finishPage(page)
        
        android.util.Log.d("PDF", "Página de PDF creada exitosamente")
        
        // Crear directorio si no existe
        val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir != null && !downloadsDir.exists()) {
            downloadsDir.mkdirs()
            android.util.Log.d("PDF", "Directorio creado: ${downloadsDir.absolutePath}")
        }
        
        // Guardar PDF
        val destino = downloadsDir ?: context.filesDir
        val nombreArchivo = "Recibo_Pago_${nombre.replace(" ", "_").take(20)}_${System.currentTimeMillis()}.pdf"
        val file = File(destino, nombreArchivo)
        
        android.util.Log.d("PDF", "Intentando guardar PDF en: ${file.absolutePath}")
        
        fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        fileOutputStream = null
        
        pdfDocument.close()
        pdfDocument = null
        
        android.util.Log.d("PDF", "=== PDF GENERADO EXITOSAMENTE ===")
        android.util.Log.d("PDF", "Ruta: ${file.absolutePath}")
        android.util.Log.d("PDF", "Tamaño: ${file.length()} bytes")
        android.util.Log.d("PDF", "Existe: ${file.exists()}")
        
        // Intentar abrir el PDF
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                setDataAndType(uri, "application/pdf")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            android.util.Log.d("PDF", "Intent de abrir PDF enviado")
        } catch (e: Exception) {
            android.util.Log.w("PDF", "No se pudo abrir el PDF automáticamente: ${e.message}")
        }
        
        file.absolutePath
    } catch (e: Exception) {
        android.util.Log.e("PDF", "=== ERROR AL GENERAR PDF ===", e)
        android.util.Log.e("PDF", "Mensaje: ${e.message}")
        android.util.Log.e("PDF", "StackTrace: ${e.stackTraceToString()}")
        
        try {
            fileOutputStream?.close()
        } catch (closeE: Exception) {
            android.util.Log.e("PDF", "Error al cerrar FileOutputStream: ${closeE.message}")
        }
        
        try {
            pdfDocument?.close()
        } catch (closeE: Exception) {
            android.util.Log.e("PDF", "Error al cerrar PdfDocument: ${closeE.message}")
        }
        
        null
    }
}

private fun generarQrBitmapParaPdf(contenido: String, size: Int): android.graphics.Bitmap {
    val bitMatrix = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, size, size)
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}