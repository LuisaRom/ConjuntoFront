package com.example.app.Model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categorías del mapa. Cada una es un botón de filtro en la pantalla.
 * Los colores siguen el código del plano de evacuación: verde = zonas verdes,
 * azul = parqueaderos, rojo = emergencias.
 */
enum class CategoriaMapa(val etiqueta: String, val color: Color, val icono: ImageVector) {
    TORRES("Torres", Color(0xFF0D1B2A), Icons.Filled.Apartment),
    SALON("Salón comunal", Color(0xFFC9A227), Icons.Filled.Groups),
    SERVICIOS("Servicios", Color(0xFF546E7A), Icons.Filled.Info),
    PARQUES("Parques", Color(0xFFEF6C00), Icons.Filled.ChildCare),
    CANCHA("Cancha", Color(0xFF6A1B9A), Icons.Filled.SportsSoccer),
    ZONAS_VERDES("Zonas verdes", Color(0xFF2E7D32), Icons.Filled.Park),
    PARQUEADEROS("Parqueaderos", Color(0xFF1565C0), Icons.Filled.LocalParking),
    PORTERIA("Portería", Color(0xFF00838F), Icons.Filled.Security),
    EMERGENCIAS("Emergencias", Color(0xFFC62828), Icons.Filled.LocalHospital)
}

/**
 * Un punto sobre el plano.
 * [x] e [y] van de 0 a 1: 0,0 es la esquina superior izquierda de la imagen y 1,1 la inferior derecha.
 * Si un punto queda desplazado, solo hay que ajustar estos dos números.
 */
data class PuntoMapa(
    val id: String,
    val nombre: String,
    val categoria: CategoriaMapa,
    val x: Float,
    val y: Float,
    val descripcion: String,
    val destacado: Boolean = false
)

/** Todos los puntos del conjunto. Para agregar uno nuevo, copia una línea y cambia sus datos. */
val PUNTOS_MAPA: List<PuntoMapa> = listOf(
    PuntoMapa("t1", "Torre 1", CategoriaMapa.TORRES, 0.810f, 0.865f, "Torre 1. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t2", "Torre 2", CategoriaMapa.TORRES, 0.699f, 0.865f, "Torre 2. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t3", "Torre 3", CategoriaMapa.TORRES, 0.586f, 0.865f, "Torre 3. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t4", "Torre 4", CategoriaMapa.TORRES, 0.475f, 0.865f, "Torre 4. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t5", "Torre 5", CategoriaMapa.TORRES, 0.365f, 0.865f, "Torre 5. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t6", "Torre 6", CategoriaMapa.TORRES, 0.256f, 0.865f, "Torre 6. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t7", "Torre 7", CategoriaMapa.TORRES, 0.086f, 0.830f, "Torre 7. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t8", "Torre 8", CategoriaMapa.TORRES, 0.098f, 0.681f, "Torre 8. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t9", "Torre 9", CategoriaMapa.TORRES, 0.109f, 0.537f, "Torre 9. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t10", "Torre 10", CategoriaMapa.TORRES, 0.117f, 0.394f, "Torre 10. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t11", "Torre 11", CategoriaMapa.TORRES, 0.126f, 0.245f, "Torre 11. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t12", "Torre 12", CategoriaMapa.TORRES, 0.132f, 0.105f, "Torre 12. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t13", "Torre 13", CategoriaMapa.TORRES, 0.307f, 0.192f, "Torre 13. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t14", "Torre 14", CategoriaMapa.TORRES, 0.414f, 0.192f, "Torre 14. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t15", "Torre 15", CategoriaMapa.TORRES, 0.520f, 0.192f, "Torre 15. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t16", "Torre 16", CategoriaMapa.TORRES, 0.626f, 0.192f, "Torre 16. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t17", "Torre 17", CategoriaMapa.TORRES, 0.803f, 0.166f, "Torre 17. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t18", "Torre 18", CategoriaMapa.TORRES, 0.806f, 0.311f, "Torre 18. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t19", "Torre 19", CategoriaMapa.TORRES, 0.810f, 0.460f, "Torre 19. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t20", "Torre 20", CategoriaMapa.TORRES, 0.815f, 0.608f, "Torre 20. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t21", "Torre 21", CategoriaMapa.TORRES, 0.591f, 0.503f, "Torre 21. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t22", "Torre 22", CategoriaMapa.TORRES, 0.481f, 0.503f, "Torre 22. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("t23", "Torre 23", CategoriaMapa.TORRES, 0.373f, 0.503f, "Torre 23. Extintores en los pisos 1, 3 y 5 y salida de emergencia en el primer piso."),
    PuntoMapa("salon1", "Salón social 1", CategoriaMapa.SALON, 0.271f, 0.475f, "Salón social del conjunto, en la zona comunal central junto a la oficina de administración.", destacado = true),
    PuntoMapa("salon2", "Salón social 2", CategoriaMapa.SALON, 0.310f, 0.560f, "Segundo salón social de la zona comunal central.", destacado = true),
    PuntoMapa("almacen", "Almacén de insumos", CategoriaMapa.SERVICIOS, 0.310f, 0.463f, "Almacén de insumos de la administración, en la zona comunal central."),
    PuntoMapa("bano", "Baño", CategoriaMapa.SERVICIOS, 0.257f, 0.526f, "Baño de la zona comunal central."),
    PuntoMapa("oficina", "Oficina de administración", CategoriaMapa.SERVICIOS, 0.279f, 0.559f, "Oficina de administración y archivo, en la zona comunal central.", destacado = true),
    PuntoMapa("cancha", "Cancha deportiva", CategoriaMapa.CANCHA, 0.223f, 0.503f, "Cancha deportiva del conjunto, junto a la zona comunal central.", destacado = true),
    PuntoMapa("parque1", "Parque infantil norte", CategoriaMapa.PARQUES, 0.225f, 0.194f, "Parque infantil, entre las torres T11–T12 y T13.", destacado = true),
    PuntoMapa("parque2", "Parque infantil sur", CategoriaMapa.PARQUES, 0.214f, 0.623f, "Parque infantil, junto a la torre T23 y la cancha.", destacado = true),
    PuntoMapa("porteria", "Portería", CategoriaMapa.PORTERIA, 0.876f, 0.705f, "Portería y punto de recepción del conjunto. Aquí se reciben paquetes, recibos y visitantes.", destacado = true),
    PuntoMapa("shut", "Shut de basuras", CategoriaMapa.SERVICIOS, 0.874f, 0.827f, "Shut de basuras, junto a la torre T1."),
    PuntoMapa("bicicletero", "Bicicletero", CategoriaMapa.SERVICIOS, 0.074f, 0.947f, "Bicicletero del conjunto, junto a la torre T7."),
    PuntoMapa("encuentro", "Punto de encuentro: Parque final (Calle 26)", CategoriaMapa.EMERGENCIAS, 0.896f, 0.030f, "Punto de encuentro en caso de evacuación, en el parque final sobre la Calle 26.", destacado = true),
    PuntoMapa("botiquin1", "Botiquín de la administración", CategoriaMapa.EMERGENCIAS, 0.279f, 0.582f, "Botiquín en la oficina de administración."),
    PuntoMapa("botiquin2", "Botiquín de la portería", CategoriaMapa.EMERGENCIAS, 0.846f, 0.692f, "Botiquín en la portería."),
    PuntoMapa("camilla", "Camilla", CategoriaMapa.EMERGENCIAS, 0.809f, 0.693f, "Camilla de emergencia, junto a la portería."),
    PuntoMapa("electrico", "Riesgo eléctrico", CategoriaMapa.EMERGENCIAS, 0.641f, 0.665f, "Zona con riesgo eléctrico. No manipular ni acercarse sin autorización."),
    PuntoMapa("zv1", "Zona verde: costado de la torre t12", CategoriaMapa.ZONAS_VERDES, 0.224f, 0.116f, "Zona verde ubicada en el costado de la torre T12."),
    PuntoMapa("zv2", "Zona verde: costado de las torres t11 y t10", CategoriaMapa.ZONAS_VERDES, 0.218f, 0.351f, "Zona verde ubicada en el costado de las torres T11 y T10."),
    PuntoMapa("zv3", "Zona verde: costado de la torre t9", CategoriaMapa.ZONAS_VERDES, 0.182f, 0.536f, "Zona verde ubicada en el costado de la torre T9."),
    PuntoMapa("zv4", "Zona verde: costado de la torre t8", CategoriaMapa.ZONAS_VERDES, 0.182f, 0.714f, "Zona verde ubicada en el costado de la torre T8."),
    PuntoMapa("zv5", "Zona verde: costado de la torre t7", CategoriaMapa.ZONAS_VERDES, 0.182f, 0.862f, "Zona verde ubicada en el costado de la torre T7."),
    PuntoMapa("zv6", "Zona verde: norte de la torre t13", CategoriaMapa.ZONAS_VERDES, 0.341f, 0.082f, "Zona verde ubicada en el norte de la torre T13."),
    PuntoMapa("zv7", "Zona verde: norte de la torre t15", CategoriaMapa.ZONAS_VERDES, 0.531f, 0.078f, "Zona verde ubicada en el norte de la torre T15."),
    PuntoMapa("zv8", "Zona verde: norte de la torre t16", CategoriaMapa.ZONAS_VERDES, 0.725f, 0.074f, "Zona verde ubicada en el norte de la torre T16."),
    PuntoMapa("zv9", "Zona verde: costado de la torre t17", CategoriaMapa.ZONAS_VERDES, 0.701f, 0.167f, "Zona verde ubicada en el costado de la torre T17."),
    PuntoMapa("zv10", "Zona verde: frente a la torre t23", CategoriaMapa.ZONAS_VERDES, 0.298f, 0.623f, "Zona verde ubicada en el frente a la torre T23."),
    PuntoMapa("zv11", "Zona verde: frente a la torre t22", CategoriaMapa.ZONAS_VERDES, 0.427f, 0.623f, "Zona verde ubicada en el frente a la torre T22."),
    PuntoMapa("zv12", "Zona verde: frente a la torre t21", CategoriaMapa.ZONAS_VERDES, 0.626f, 0.621f, "Zona verde ubicada en el frente a la torre T21."),
    PuntoMapa("zv13", "Zona verde: sur de la torre t6", CategoriaMapa.ZONAS_VERDES, 0.274f, 0.964f, "Zona verde ubicada en el sur de la torre T6."),
    PuntoMapa("zv14", "Zona verde: sur de la torre t4", CategoriaMapa.ZONAS_VERDES, 0.497f, 0.965f, "Zona verde ubicada en el sur de la torre T4."),
    PuntoMapa("zv15", "Zona verde: sur de la torre t1", CategoriaMapa.ZONAS_VERDES, 0.787f, 0.969f, "Zona verde ubicada en el sur de la torre T1."),
    PuntoMapa("pq1", "Parqueaderos norte", CategoriaMapa.PARQUEADEROS, 0.451f, 0.305f, "Parqueaderos junto a la vía vehicular norte, frente a las torres T13–T16."),
    PuntoMapa("pq2", "Parqueaderos interior norte", CategoriaMapa.PARQUEADEROS, 0.451f, 0.386f, "Parqueaderos al interior del conjunto, sobre la vía vehicular norte."),
    PuntoMapa("pq3", "Parqueaderos interior sur", CategoriaMapa.PARQUEADEROS, 0.392f, 0.682f, "Parqueaderos al interior del conjunto, sobre la vía vehicular sur."),
    PuntoMapa("pq4", "Parqueaderos sur", CategoriaMapa.PARQUEADEROS, 0.444f, 0.744f, "Parqueaderos frente a las torres T1–T6, junto a la vía vehicular sur."),
    PuntoMapa("pq5", "Parqueaderos oriente 1", CategoriaMapa.PARQUEADEROS, 0.672f, 0.530f, "Parqueaderos en el costado oriente, junto a la vía hacia la portería."),
    PuntoMapa("pq6", "Parqueaderos oriente 2", CategoriaMapa.PARQUEADEROS, 0.722f, 0.430f, "Parqueaderos en el costado oriente, junto a las torres T17–T20."),
)
