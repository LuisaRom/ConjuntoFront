package com.example.app.Pantallas.RolCelador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.Model.Paqueteria
import com.example.app.ViewModel.PaqueteriaViewModel
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PantallaPaqueteriaCelador(
    navController: NavController,
    paqueteriaViewModel: PaqueteriaViewModel = hiltViewModel()
) {
    val paquetes by paqueteriaViewModel.paquetes.collectAsState()
    val isLoading by paqueteriaViewModel.isLoading.collectAsState()
    
    // Cargar paquetes al iniciar
    DisposableEffect(Unit) {
        paqueteriaViewModel.obtenerTodos()
        onDispose { }
    }
    
    // Separar paquetes por estado
    val paquetesPendientes = paquetes.filter { it.estado == "PENDIENTE" }
    val paquetesEntregados = paquetes.filter { it.estado == "ENTREGADO" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = GrisClaro,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Paqueteria",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para agregar nuevo paquete
        Button(
            onClick = { navController.navigate("PantallaDetallesPaqueteriaCelador") },
            colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Paquete", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = DoradoElegante,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Paquetes a Entregar (${paquetesPendientes.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Text("Cargando...", color = Color.White)
        } else if (paquetesPendientes.isEmpty()) {
            Text(
                text = "No hay paquetes pendientes",
                color = GrisClaro,
                fontSize = 14.sp
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paquetesPendientes) { paquete ->
                    PaqueteCardCompleto(
                        paquete = paquete,
                        onClick = {
                            // Podría navegar a una pantalla de detalles si se requiere
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckBox,
                contentDescription = null,
                tint = DoradoElegante,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Paquetes Entregados (${paquetesEntregados.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (paquetesEntregados.isEmpty()) {
            Text(
                text = "No hay paquetes entregados",
                color = GrisClaro,
                fontSize = 14.sp
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paquetesEntregados) { paquete ->
                    PaqueteCardCompleto(
                        paquete = paquete,
                        onClick = {
                            // Podría navegar a una pantalla de detalles si se requiere
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PaqueteCardCompleto(
    paquete: Paqueteria,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = AzulOscuro.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con ID, estado e ícono de expandir/colapsar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = DoradoElegante,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Paquete #${paquete.id ?: "N/A"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        // Información resumida cuando está colapsado
                        if (!expanded) {
                            Text(
                                text = "${paquete.transportadora ?: "N/A"} - ${paquete.usuario?.nombre ?: "N/A"}",
                                color = GrisClaro,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = paquete.estado ?: "N/A",
                        color = if (paquete.estado == "ENTREGADO") Color.Green else DoradoElegante,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(
                                if (paquete.estado == "ENTREGADO") Color.Green.copy(alpha = 0.2f)
                                else DoradoElegante.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint = DoradoElegante,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Contenido expandible
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Información del destinatario
                if (paquete.usuario != null) {
                    Text(
                        text = "Destinatario",
                        color = GrisClaro,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = paquete.usuario.nombre ?: "N/A",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Torre y Apartamento
                    if (!paquete.usuario.torre.isNullOrBlank() || !paquete.usuario.apartamento.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ubicación: ",
                                color = GrisClaro,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Torre ${paquete.usuario.torre ?: "N/A"} - Apt ${paquete.usuario.apartamento ?: "N/A"}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // Teléfono del destinatario
                    if (!paquete.usuario.telefono.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Teléfono: ",
                                color = GrisClaro,
                                fontSize = 12.sp
                            )
                            Text(
                                text = paquete.usuario.telefono ?: "N/A",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Documento del destinatario (si está disponible)
                    if (!paquete.usuario.documento.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Documento: ",
                                color = GrisClaro,
                                fontSize = 12.sp
                            )
                            Text(
                                text = paquete.usuario.documento ?: "N/A",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Información de la transportadora
                Text(
                    text = "Transportadora",
                    color = GrisClaro,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paquete.transportadora ?: "N/A",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Fecha de recepción
                if (!paquete.fechaRecepcion.isNullOrBlank()) {
                    Text(
                        text = "Fecha de Recepción",
                        color = GrisClaro,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatearFechaPaquete(paquete.fechaRecepcion),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Función para formatear la fecha del paquete
fun formatearFechaPaquete(fechaISO: String?): String {
    if (fechaISO.isNullOrBlank()) return "No disponible"
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val fecha = LocalDateTime.parse(fechaISO, formatter)
        val formatterSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        fecha.format(formatterSalida)
    } catch (e: Exception) {
        fechaISO
    }
}