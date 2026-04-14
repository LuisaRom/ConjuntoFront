package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Queja
import com.example.app.Repository.QuejaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QuejaViewModel @Inject constructor(
    private val repository: QuejaRepository
) : ViewModel() {
    
    private val _quejas = MutableStateFlow<List<Queja>>(emptyList())
    val quejas: StateFlow<List<Queja>> = _quejas.asStateFlow()

    private val _quejaSeleccionada = MutableStateFlow<Queja?>(null)
    val quejaSeleccionada: StateFlow<Queja?> = _quejaSeleccionada.asStateFlow()

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
            _quejas.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener quejas: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorCategoria(categoria: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerPorCategoria(categoria)
            }
            _quejas.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener quejas por categoría: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(queja: Queja, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val quejaGuardada = withContext(Dispatchers.IO) {
                repository.guardar(queja)
            }
            obtenerTodos()
            onSuccess?.invoke()
        } catch (e: Exception) {
            _error.value = "Error al guardar queja: ${e.message}"
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
            _error.value = "Error al eliminar queja: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val queja = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _quejaSeleccionada.value = queja
        } catch (e: Exception) {
            _error.value = "Error al obtener queja: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    // Función para actualizar estado de queja (para administrador)
    fun actualizarEstado(id: Long, nuevoEstado: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val queja = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            val quejaActualizada = queja.copy(estado = nuevoEstado)
            withContext(Dispatchers.IO) {
                repository.guardar(quejaActualizada)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al actualizar estado: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun finalizarQueja(id: Long, onSuccess: (() -> Unit)? = null) = viewModelScope.launch {
        _error.value = null
        try {
            val quejaFinalizada = withContext(Dispatchers.IO) {
                repository.finalizar(id)
            }
            _quejas.value = _quejas.value.map { actual ->
                if (actual.id == id) quejaFinalizada else actual
            }
            onSuccess?.invoke()
        } catch (e: Exception) {
            _error.value = "Error al finalizar queja: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }
}