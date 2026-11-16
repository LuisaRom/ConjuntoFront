package com.example.app.Pantallas

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app.Model.CategoriaMapa
import com.example.app.Model.PUNTOS_MAPA
import com.example.app.Model.PuntoMapa
import com.example.app.R
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Zoom mínimo (todo el plano a la vista) y máximo.
private const val ESCALA_MIN = 1f
private const val ESCALA_MAX = 6f

// Inclinación del botón "3D" para una vista semi alta del conjunto.
private const val INCLINACION_GRADOS = 38f

private val FondoMapa = Color(0xFFD9DEE5)

/**
 * Mapa interactivo del conjunto.
 *
 * - Se mueve con un dedo y se acerca/aleja pellizcando. Doble toque: acerca o vuelve a la vista completa.
 * - Los botones de arriba filtran los puntos por categoría y llevan la cámara a esa zona.
 * - El botón "3D" inclina el plano para verlo desde una vista semi alta.
 * - Los puntos están en Model/PuntosMapa.kt y se pueden editar o ampliar ahí.
 */
@Composable
fun PantallaMapaConjunto(navController: NavController) {
    val scope = rememberCoroutineScope()
    val dens = LocalDensity.current
    val painter = painterResource(R.drawable.mapa_conjunto)
    val aspecto = painter.intrinsicSize.let {
        if (it.width > 0f && it.height > 0f) it.width / it.height else 1.36f
    }

    var categoria by remember { mutableStateOf<CategoriaMapa?>(null) } // null = Todo
    var seleccionado by remember { mutableStateOf<PuntoMapa?>(null) }
    var inclinado by remember { mutableStateOf(false) }

    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    var animacion by remember { mutableStateOf<Job?>(null) }
    var tam by remember { mutableStateOf(IntSize.Zero) }

    val inclinacion by animateFloatAsState(
        targetValue = if (inclinado) INCLINACION_GRADOS else 0f,
        animationSpec = tween(450),
        label = "inclinacion"
    )

    // Tamaño del plano dentro del visor: se ajusta para que se vea completo al inicio.
    val listo = tam.width > 0 && tam.height > 0
    val contenedorW = tam.width.toFloat()
    val contenedorH = tam.height.toFloat()
    val anchoContenido =
        if (!listo) 0f else if (contenedorW / contenedorH < aspecto) contenedorW else contenedorH * aspecto
    val altoContenido = if (!listo) 0f else anchoContenido / aspecto

    // Evita que el plano se salga demasiado de la pantalla.
    fun limitar(t: Offset, s: Float): Offset {
        val maxX = max(0f, (anchoContenido * s - contenedorW) / 2f) + contenedorW * 0.12f
        val maxY = max(0f, (altoContenido * s - contenedorH) / 2f) + contenedorH * 0.12f
        return Offset(t.x.coerceIn(-maxX, maxX), t.y.coerceIn(-maxY, maxY))
    }

    // Mueve la cámara con una animación suave.
    fun animarA(escalaDestino: Float, desplDestino: Offset) {
        animacion?.cancel()
        val e0 = escala
        val d0 = desplazamiento
        val eT = escalaDestino.coerceIn(ESCALA_MIN, ESCALA_MAX)
        val dT = limitar(desplDestino, eT)
        animacion = scope.launch {
            animate(0f, 1f, animationSpec = tween(380)) { t, _ ->
                escala = e0 + (eT - e0) * t
                desplazamiento = d0 + (dT - d0) * t
            }
        }
    }

    // Encuadra un grupo de puntos en pantalla.
    fun enfocar(puntos: List<PuntoMapa>) {
        if (!listo || puntos.isEmpty()) return
        val margen = with(dens) { 56.dp.toPx() }
        val minX = puntos.minOf { it.x }
        val maxX = puntos.maxOf { it.x }
        val minY = puntos.minOf { it.y }
        val maxY = puntos.maxOf { it.y }
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val cajaW = (maxX - minX) * anchoContenido + 2f * margen
        val cajaH = (maxY - minY) * altoContenido + 2f * margen
        val tope = if (puntos.size == 1) 3f else 4f
        val s = min(contenedorW / cajaW, contenedorH / cajaH).coerceIn(ESCALA_MIN, tope)
        animarA(
            s,
            Offset(-(cx - 0.5f) * anchoContenido * s, -(cy - 0.5f) * altoContenido * s)
        )
    }

    fun zoomPaso(factor: Float) {
        val nueva = (escala * factor).coerceIn(ESCALA_MIN, ESCALA_MAX)
        animarA(nueva, desplazamiento * (nueva / escala))
    }

    fun puntosDe(c: CategoriaMapa?): List<PuntoMapa> =
        if (c == null) PUNTOS_MAPA else PUNTOS_MAPA.filter { it.categoria == c }

    fun elegirCategoria(c: CategoriaMapa?) {
        categoria = c
        seleccionado = null
        enfocar(puntosDe(c))
    }

    fun elegirPunto(p: PuntoMapa) {
        seleccionado = p
        enfocar(listOf(p))
    }

    val visibles = remember(categoria) { puntosDe(categoria) }
    val panelAlto = (LocalConfiguration.current.screenHeightDp * 0.27f).dp.coerceIn(96.dp, 176.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = GrisClaro
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Mapa del conjunto",
                color = GrisClaro,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Botones de filtro
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FiltroChip("Todo", null, categoria == null) { elegirCategoria(null) }
            }
            items(CategoriaMapa.values().toList()) { c ->
                FiltroChip(c.etiqueta, c.icono, categoria == c) { elegirCategoria(c) }
            }
        }

        // Visor del mapa
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FondoMapa)
                .onSizeChanged { tam = it }
                .pointerInput(tam) {
                    detectTransformGestures { centroide, pan, zoom, _ ->
                        animacion?.cancel()
                        val centro = Offset(tam.width / 2f, tam.height / 2f)
                        val nueva = (escala * zoom).coerceIn(ESCALA_MIN, ESCALA_MAX)
                        val z = nueva / escala
                        val nuevo = desplazamiento * z + (centroide - centro) * (1f - z) + pan
                        escala = nueva
                        desplazamiento = limitar(nuevo, nueva)
                    }
                }
                .pointerInput(tam) {
                    detectTapGestures(
                        onDoubleTap = { punto ->
                            if (escala > 1.5f) {
                                animarA(1f, Offset.Zero)
                            } else {
                                val centro = Offset(tam.width / 2f, tam.height / 2f)
                                val objetivo = 2.5f
                                val z = objetivo / escala
                                animarA(objetivo, desplazamiento * z + (punto - centro) * (1f - z))
                            }
                        },
                        onTap = { seleccionado = null }
                    )
                }
        ) {
            if (listo) {
                val anchoDp = with(dens) { anchoContenido.toDp() }
                val altoDp = with(dens) { altoContenido.toDp() }

                // Capa de inclinación (vista semi alta)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = inclinacion
                            cameraDistance = 20f * dens.density
                        }
                ) {
                    // Capa de zoom y desplazamiento
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(anchoDp, altoDp)
                            .graphicsLayer {
                                scaleX = escala
                                scaleY = escala
                                translationX = desplazamiento.x
                                translationY = desplazamiento.y
                            }
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Plano del conjunto",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                        visibles.filter { it.id != seleccionado?.id }.forEach { p ->
                            MarcadorMapa(
                                punto = p,
                                anchoPx = anchoContenido,
                                altoPx = altoContenido,
                                seleccionado = false,
                                escala = { escala },
                                onClick = { elegirPunto(p) }
                            )
                        }
                        // El punto elegido se dibuja al final para que quede encima.
                        seleccionado?.let { p ->
                            MarcadorMapa(
                                punto = p,
                                anchoPx = anchoContenido,
                                altoPx = altoContenido,
                                seleccionado = true,
                                escala = { escala },
                                onClick = { seleccionado = null }
                            )
                        }
                    }
                }
            }

            // Controles del mapa
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonMapa(activo = inclinado, onClick = { inclinado = !inclinado }) {
                    Text("3D", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                BotonMapa(onClick = {
                    seleccionado = null
                    animarA(1f, Offset.Zero)
                }) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Ver todo el mapa")
                }
                BotonMapa(onClick = { zoomPaso(1.6f) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Acercar")
                }
                BotonMapa(onClick = { zoomPaso(1f / 1.6f) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Alejar")
                }
            }
        }

        // Panel inferior: información del punto elegido o lista de lugares
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(panelAlto)
                .padding(horizontal = 12.dp)
                .padding(bottom = 10.dp)
        ) {
            val sel = seleccionado
            if (sel != null) {
                PanelPunto(
                    punto = sel,
                    onAcercar = { enfocar(listOf(sel)) },
                    onCerrar = { seleccionado = null }
                )
            } else {
                val c = categoria
                val lista = if (c == null) PUNTOS_MAPA.filter { it.destacado } else visibles
                val titulo = if (c == null) "Lugares destacados" else "${c.etiqueta} (${visibles.size})"
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = titulo,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Arrastra el mapa, pellizca para acercar y toca un punto para ver su información.",
                        color = GrisClaro,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(lista, key = { it.id }) { p ->
                            PuntoChip(p) { elegirPunto(p) }
                        }
                    }
                }
            }
        }
    }
}

/** Círculo que marca un lugar en el plano. Mantiene su tamaño en pantalla aunque se haga zoom. */
@Composable
private fun MarcadorMapa(
    punto: PuntoMapa,
    anchoPx: Float,
    altoPx: Float,
    seleccionado: Boolean,
    escala: () -> Float,
    onClick: () -> Unit
) {
    val dens = LocalDensity.current
    val tam = when {
        seleccionado -> 36.dp
        punto.categoria == CategoriaMapa.TORRES -> 26.dp
        punto.destacado -> 28.dp
        else -> 22.dp
    }
    val tamPx = with(dens) { tam.toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (punto.x * anchoPx - tamPx / 2f).roundToInt(),
                    (punto.y * altoPx - tamPx / 2f).roundToInt()
                )
            }
            .size(tam)
            .graphicsLayer {
                val s = 1f / escala()
                scaleX = s
                scaleY = s
            }
            .background(punto.categoria.color, CircleShape)
            .border(
                width = if (seleccionado) 3.dp else 1.5.dp,
                color = if (seleccionado) DoradoElegante else Color.White,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (punto.categoria == CategoriaMapa.TORRES) {
            Text(
                text = "T" + punto.nombre.substringAfter(' '),
                color = Color.White,
                fontSize = if (seleccionado) 12.sp else 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        } else {
            Icon(
                imageVector = punto.categoria.icono,
                contentDescription = punto.nombre,
                tint = Color.White,
                modifier = Modifier.size(tam * 0.58f)
            )
        }
    }
}

@Composable
private fun FiltroChip(texto: String, icono: ImageVector?, activo: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(forma)
            .background(if (activo) DoradoElegante else Color.White.copy(alpha = 0.08f))
            .border(1.dp, if (activo) DoradoElegante else GrisClaro.copy(alpha = 0.4f), forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (activo) AzulOscuro else GrisClaro,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = texto,
            color = if (activo) AzulOscuro else Color.White,
            fontSize = 13.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PuntoChip(punto: PuntoMapa, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(punto.categoria.color, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = punto.nombre, color = Color.White, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
private fun BotonMapa(
    activo: Boolean = false,
    onClick: () -> Unit,
    contenido: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (activo) DoradoElegante else AzulOscuro.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (activo) AzulOscuro else Color.White
        ) {
            contenido()
        }
    }
}

@Composable
private fun PanelPunto(punto: PuntoMapa, onAcercar: () -> Unit, onCerrar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(punto.categoria.color, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = punto.categoria.icono,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = punto.nombre,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = punto.categoria.etiqueta, color = DoradoElegante, fontSize = 12.sp)
            }
            IconButton(onClick = onCerrar) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = GrisClaro)
            }
        }
        Text(
            text = punto.descripcion,
            color = GrisClaro,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, end = 10.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(50))
                .background(DoradoElegante)
                .clickable(onClick = onAcercar)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Acercar en el mapa",
                color = AzulOscuro,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
