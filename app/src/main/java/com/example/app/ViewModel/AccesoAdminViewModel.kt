package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.AccesoAdmin
import com.example.app.Repository.AccesoAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AccesoAdminViewModel @Inject constructor(
    private val repository: AccesoAdminRepository
) : ViewModel() {

    private val _accesos = MutableStateFlow<List<AccesoAdmin>>(emptyList())
    val accesos: StateFlow<List<AccesoAdmin>> = _accesos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun obtenerTodos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lista = withContext(Dispatchers.IO) { repository.obtenerTodos() }
                _accesos.value = lista
            } catch (e: Exception) {
                _error.value = "Error al obtener accesos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            _error.value = null
            try {
                withContext(Dispatchers.IO) { repository.eliminar(id) }
                _accesos.value = _accesos.value.filterNot { it.id == id }
            } catch (e: Exception) {
                _error.value = "Error al eliminar acceso: ${e.message}"
            }
        }
    }
}
