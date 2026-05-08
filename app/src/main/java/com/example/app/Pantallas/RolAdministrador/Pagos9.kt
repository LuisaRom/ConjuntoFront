package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app.Model.PagoAdministracion
import com.example.app.Model.Usuario
import com.example.app.ViewModel.PagoAdministracionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun PantallaPagos(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel(),
    pagoAdministracionViewModel: PagoAdministracionViewModel = hiltViewModel()
) {
    val usuarios by usuarioViewModel.usuarios.collectAsState()
    val pagos by pagoAdministracionViewModel.pagos.collectAsState()
    val isLoadingUsuarios by usuarioViewModel.isLoading.collectAsState()
    val isLoadingPagos by pagoAdministracionViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerTodos()
        pagoAdministracionViewModel.obtenerTodos()
    }

    val residentes = remember(usuarios) {
        usuarios
            .filter { it.rol.equals("RESIDENTE", ignoreCase = true) }
            .filter { it.id != null }
            .sortedBy { it.nombre.ifBlank { it.usuario } }
    }
    val pagosActualMes = remember(pagos) { pagos.filter { esPagoDelMesActual(it) } }
    val pagoPorResidente = remember(pagosActualMes) {
        pagosActualMes
            .filter { it.usuario?.id != null }
            .groupBy { it.usuario?.id!! }
            .mapValues { (_, lista) ->
                lista.maxByOrNull { convertirFechaPagoALocalDate(it.fechaPago) ?: LocalDate.MIN }
            }
    }
    val hoy = remember { LocalDate.now() }
    val alDia = remember(residentes, pagoPorResidente, hoy) {
        residentes.filter { residente ->
            val pago = pagoPorResidente[residente.id]
            estaAlDiaSegunRegla(pago, hoy)
        }
    }
    val enMora = remember(residentes, pagoPorResidente, hoy) {
        residentes.filter { residente ->
            val pago = pagoPorResidente[residente.id]
            !estaAlDiaSegunRegla(pago, hoy)
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
            Text("Pagos", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Regla aplicada: pago de administración hasta el día 5 de cada mes",
            style = MaterialTheme.typography.bodySmall,
            color = GrisClaro
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoadingUsuarios || isLoadingPagos) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        }

        Text(
            text = "Usuarios al día (${alDia.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoElegante
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (alDia.isEmpty()) {
            Text(
                text = "No hay residentes al día para el mes actual.",
                color = GrisClaro
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alDia.forEach { usuario ->
                    UsuarioPago(usuario = usuario, enMora = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Usuarios en mora (${enMora.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFFB4AB)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (enMora.isEmpty()) {
            Text(
                text = "No hay residentes en mora para el mes actual.",
                color = GrisClaro
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                enMora.forEach { usuario ->
                    UsuarioPago(usuario = usuario, enMora = true)
                }
            }
        }
    }
}

@Composable
fun UsuarioPago(usuario: Usuario, enMora: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (enMora) Color(0x33FF6B6B) else Color.White.copy(alpha = 0.07f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = if (enMora) Color(0xFFFFB4AB) else GrisClaro,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = usuario.nombre.ifBlank { usuario.usuario },
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Torre ${usuario.torre.ifBlank { "-" }} - Apt ${usuario.apartamento.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
        Text(
            text = if (enMora) "MORA" else "AL DÍA",
            color = if (enMora) Color(0xFFFFB4AB) else Color(0xFF93E9BE),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun esPagoDelMesActual(pago: PagoAdministracion): Boolean {
    val fecha = convertirFechaPagoALocalDate(pago.fechaPago) ?: return false
    val hoy = LocalDate.now()
    return fecha.year == hoy.year && fecha.monthValue == hoy.monthValue
}

private fun estaAlDiaSegunRegla(pagoDelMes: PagoAdministracion?, hoy: LocalDate): Boolean {
    // Hasta el día 5 sigue al día incluso si no ha pagado.
    if (hoy.dayOfMonth <= 5 && pagoDelMes == null) return true
    val fechaPago = convertirFechaPagoALocalDate(pagoDelMes?.fechaPago) ?: return false
    return fechaPago.dayOfMonth <= 5
}

private fun convertirFechaPagoALocalDate(fecha: String?): LocalDate? {
    if (fecha.isNullOrBlank()) return null

    return try {
        OffsetDateTime.parse(fecha).toLocalDate()
    } catch (_: DateTimeParseException) {
        try {
            LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            try {
                val instant = java.time.Instant.parse(fecha)
                instant.atZone(ZoneId.systemDefault()).toLocalDate()
            } catch (_: Exception) {
                null
            }
        }
    }
}
