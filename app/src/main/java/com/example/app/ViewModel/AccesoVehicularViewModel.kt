package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.AccesoVehicular
import com.example.app.Repository.AccesoVehicularRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AccesoVehicularViewModel @Inject constructor(
    private val repository: AccesoVehicularRepository
) : ViewModel() {

    private val _accesosVehiculares = MutableStateFlow<List<AccesoVehicular>>(emptyList())
    val accesosVehiculares: StateFlow<List<AccesoVehicular>> = _accesosVehiculares.asStateFlow()

    private val _accesoSeleccionado = MutableStateFlow<AccesoVehicular?>(null)
    val accesoSeleccionado: StateFlow<AccesoVehicular?> = _accesoSeleccionado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ultimoAccesoGuardado = MutableStateFlow<AccesoVehicular?>(null)
    val ultimoAccesoGuardado: StateFlow<AccesoVehicular?> = _ultimoAccesoGuardado.asStateFlow()

    fun obtenerAccesosVehiculares() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lista = withContext(Dispatchers.IO) {
                    repository.obtenerTodos()
                }
                _accesosVehiculares.value = lista
            } catch (e: Exception) {
                _error.value = "Error al obtener accesos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerAccesoVehicular(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val acceso = withContext(Dispatchers.IO) {
                    repository.obtenerPorId(id)
                }
                _accesoSeleccionado.value = acceso
            } catch (e: Exception) {
                _error.value = "Error al obtener acceso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarAccesoVehicular(accesoVehicular: AccesoVehicular) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val accesoGuardado = withContext(Dispatchers.IO) {
                    repository.guardar(accesoVehicular)
                }
                _ultimoAccesoGuardado.value = accesoGuardado
                obtenerAccesosVehiculares() // Refrescar lista
            } catch (e: Exception) {
                _ultimoAccesoGuardado.value = null
                _error.value = "Error al guardar acceso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarAccesoVehicular(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.eliminar(id)
                }
                obtenerAccesosVehiculares() // Refrescar lista
            } catch (e: Exception) {
                _error.value = "Error al eliminar acceso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUltimoAccesoGuardado() {
        _ultimoAccesoGuardado.value = null
    }
}