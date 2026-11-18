package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.NuevoVehiculoRequest
import com.example.app.Model.VehiculoResidente
import com.example.app.Repository.VehiculoResidenteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class VehiculoResidenteViewModel @Inject constructor(
    private val repository: VehiculoResidenteRepository
) : ViewModel() {

    private val _vehiculos = MutableStateFlow<List<VehiculoResidente>>(emptyList())
    val vehiculos: StateFlow<List<VehiculoResidente>> = _vehiculos.asStateFlow()

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
            _vehiculos.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener vehículos: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(vehiculo: VehiculoResidente) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val vehiculoGuardado = withContext(Dispatchers.IO) {
                repository.guardar(vehiculo)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al guardar vehículo: ${e.message}"
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
            _error.value = "Error al eliminar vehículo: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
        } catch (e: Exception) {
            _error.value = "Error al obtener vehículo: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    private val _misVehiculos = MutableStateFlow<List<VehiculoResidente>>(emptyList())
    val misVehiculos: StateFlow<List<VehiculoResidente>> = _misVehiculos.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargarMisVehiculos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            _misVehiculos.value = withContext(Dispatchers.IO) { repository.misVehiculos() }
        } catch (e: Exception) {
            _error.value = mensajeError(e, "Error al cargar tus vehículos")
        } finally {
            _isLoading.value = false
        }
    }

    fun registrarMiVehiculo(placa: String, marca: String, modelo: String, color: String, tipo: String, onExito: () -> Unit) =
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.registrarMiVehiculo(
                        NuevoVehiculoRequest(placa.trim(), marca.trim(), modelo.trim(), color.trim().ifBlank { null }, tipo)
                    )
                }
                _mensaje.value = "Vehículo registrado. La administración te asignará el parqueadero."
                _misVehiculos.value = withContext(Dispatchers.IO) { repository.misVehiculos() }
                onExito()
            } catch (e: Exception) {
                _error.value = mensajeError(e, "No se pudo registrar el vehículo")
            } finally {
                _isLoading.value = false
            }
        }

    fun eliminarMiVehiculo(id: Long) = viewModelScope.launch {
        _error.value = null
        try {
            withContext(Dispatchers.IO) { repository.eliminarMiVehiculo(id) }
            _misVehiculos.value = _misVehiculos.value.filterNot { it.id == id }
            _mensaje.value = "Vehículo eliminado"
        } catch (e: Exception) {
            _error.value = mensajeError(e, "No se pudo eliminar el vehículo")
        }
    }

    fun asignarParqueadero(id: Long, parqueadero: String?, onExito: () -> Unit = {}) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val actualizado = withContext(Dispatchers.IO) { repository.asignarParqueadero(id, parqueadero?.trim()) }
            _vehiculos.value = _vehiculos.value.map { if (it.id == id) actualizado else it }
            _mensaje.value = if (actualizado.parqueadero.isNullOrBlank()) "Parqueadero liberado"
            else "Parqueadero ${actualizado.parqueadero} asignado a ${actualizado.placa}"
            onExito()
        } catch (e: Exception) {
            _error.value = mensajeError(e, "No se pudo asignar el parqueadero")
        } finally {
            _isLoading.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private fun mensajeError(e: Exception, base: String): String {
        if (e is HttpException) {
            val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            val detalle = Regex("\"(?:mensaje|message|error)\"\\s*:\\s*\"([^\"]+)\"").find(cuerpo)?.groupValues?.get(1)
            return detalle ?: "$base (HTTP ${e.code()})"
        }
        return "$base: ${e.message ?: "error de conexión"}"
    }

    fun clearError() {
        _error.value = null
    }
}