package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.AccesoPeatonal
import com.example.app.Repository.AccesoPeatonalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AccesoPeatonalViewModel @Inject constructor(
    private val repository: AccesoPeatonalRepository
) : ViewModel() {

    private val _accesosPeatonales = MutableStateFlow<List<AccesoPeatonal>>(emptyList())
    val accesosPeatonales: StateFlow<List<AccesoPeatonal>> = _accesosPeatonales.asStateFlow()

    private val _accesoSeleccionado = MutableStateFlow<AccesoPeatonal?>(null)
    val accesoSeleccionado: StateFlow<AccesoPeatonal?> = _accesoSeleccionado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ultimoAccesoGuardado = MutableStateFlow<AccesoPeatonal?>(null)
    val ultimoAccesoGuardado: StateFlow<AccesoPeatonal?> = _ultimoAccesoGuardado.asStateFlow()

    fun obtenerAccesosPeatonales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lista = withContext(Dispatchers.IO) {
                    repository.obtenerAccesosPeatonales()
                }
                _accesosPeatonales.value = lista
            } catch (e: Exception) {
                _error.value = "Error al obtener accesos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerAccesoPeatonal(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val acceso = withContext(Dispatchers.IO) {
                    repository.obtenerAccesoPeatonal(id)
                }
                _accesoSeleccionado.value = acceso
            } catch (e: Exception) {
                _error.value = "Error al obtener acceso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarAccesoPeatonal(accesoPeatonal: AccesoPeatonal) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val accesoGuardado = withContext(Dispatchers.IO) {
                    repository.guardarAccesoPeatonal(accesoPeatonal)
                }
                _ultimoAccesoGuardado.value = accesoGuardado
                obtenerAccesosPeatonales() // Refrescar lista
            } catch (e: Exception) {
                _ultimoAccesoGuardado.value = null
                _error.value = "Error al guardar acceso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarAccesoPeatonal(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    repository.eliminarAccesoPeatonal(id)
                }
                obtenerAccesosPeatonales() // Refrescar lista
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