package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.EstadoPagoAdmin
import com.example.app.ViewModel.PagoAdministracionViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaPagos(
    navController: NavController,
    pagoAdministracionViewModel: PagoAdministracionViewModel = hiltViewModel()
) {
    val estadoPagos by pagoAdministracionViewModel.estadoPagosAdmin.collectAsState()
    val isLoading by pagoAdministracionViewModel.isLoading.collectAsState()
    val error by pagoAdministracionViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        pagoAdministracionViewModel.obtenerEstadoPagosAdmin()
    }

    val alDia = remember(estadoPagos) { estadoPagos.filter { it.estaAlDiaEfectivo() } }
    val enMora = remember(estadoPagos) { estadoPagos.filter { !it.estaAlDiaEfectivo() } }

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
            text = "Regla: pago de administración hasta el día 5 de cada mes",
            style = MaterialTheme.typography.bodySmall,
            color = GrisClaro
        )

        error?.let { mensaje ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
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

        if (alDia.isEmpty() && !isLoading) {
            Text(text = "No hay residentes al día para el mes actual.", color = GrisClaro)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alDia.forEach { estado -> EstadoPagoItem(estado = estado, enMora = false) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Usuarios pendientes o en mora (${enMora.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFFB4AB)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (enMora.isEmpty() && !isLoading) {
            Text(text = "No hay residentes pendientes o en mora.", color = GrisClaro)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                enMora.forEach { estado -> EstadoPagoItem(estado = estado, enMora = true) }
            }
        }
    }
}

@Composable
private fun EstadoPagoItem(estado: EstadoPagoAdmin, enMora: Boolean) {
    val nombre = estado.nombreMostrar()
    val torre = estado.torre ?: estado.usuario?.torre.orEmpty()
    val apto = estado.apartamento ?: estado.usuario?.apartamento.orEmpty()
    val etiqueta = estado.etiquetaEstado()
    val diasMora = estado.diasMora ?: 0L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (enMora) Color(0x33FF6B6B) else Color.White.copy(alpha = 0.07f),
                shape = RoundedCornerShape(10.dp)
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
            Text(text = nombre, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Torre ${torre.ifBlank { "-" }} - Apt ${apto.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
            estado.periodoActual?.let {
                Text(text = "Periodo: $it", style = MaterialTheme.typography.bodySmall, color = GrisClaro)
            }
            if (diasMora > 0) {
                Text(
                    text = "Días en mora: $diasMora",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFB4AB)
                )
            }
            estado.pagoPeriodoActual?.estadoPago?.let { pagoEstado ->
                Text(
                    text = "Último pago: $pagoEstado",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisClaro
                )
            }
        }
        Text(
            text = etiqueta,
            color = if (enMora) Color(0xFFFFB4AB) else Color(0xFF93E9BE),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
