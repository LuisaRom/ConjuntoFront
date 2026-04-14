package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.ReservaZonaComun
import com.example.app.Repository.ReservaZonaComunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReservaZonaComunViewModel @Inject constructor(
    private val repository: ReservaZonaComunRepository
) : ViewModel() {
    
    private val _reservas = MutableStateFlow<List<ReservaZonaComun>>(emptyList())
    val reservas: StateFlow<List<ReservaZonaComun>> = _reservas.asStateFlow()

    private val _reservaSeleccionada = MutableStateFlow<ReservaZonaComun?>(null)
    val reservaSeleccionada: StateFlow<ReservaZonaComun?> = _reservaSeleccionada.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerTodos()
            }
            _reservas.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener reservas: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(reserva: ReservaZonaComun, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val reservaGuardada = withContext(Dispatchers.IO) {
                repository.guardar(reserva)
            }
            obtenerTodos()
            onSuccess?.invoke()
        } catch (e: Exception) {
            _error.value = "Error al guardar reserva: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            withContext(Dispatchers.IO) {
                repository.eliminar(id)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al eliminar reserva: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val reserva = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _reservaSeleccionada.value = reserva
        } catch (e: Exception) {
            _error.value = "Error al obtener reserva: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    // Función para consultar disponibilidad de una zona en una fecha y hora específica
    fun consultarDisponibilidad(zonaComun: String, fecha: String, horaInicio: String, horaFin: String): Boolean {
        val reservasExistentes = _reservas.value.filter { 
            it.zonaComun == zonaComun && 
            it.fechaReserva == fecha &&
            !(it.horaFin <= horaInicio || it.horaInicio >= horaFin)
        }
        return reservasExistentes.isEmpty()
    }

    fun clearError() {
        _error.value = null
    }
}