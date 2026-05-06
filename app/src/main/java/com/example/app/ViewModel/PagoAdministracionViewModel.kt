package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.CrearPagoRequest
import com.example.app.Model.PagoAdministracion
import com.example.app.Repository.PagoAdministracionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PagoAdministracionViewModel @Inject constructor(
    private val repository: PagoAdministracionRepository
) : ViewModel() {

    private val _pagos = MutableStateFlow<List<PagoAdministracion>>(emptyList())
    val pagos: StateFlow<List<PagoAdministracion>> = _pagos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _checkoutUrl = MutableStateFlow<String?>(null)
    val checkoutUrl: StateFlow<String?> = _checkoutUrl.asStateFlow()

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerTodos()
            }
            _pagos.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener pagos: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(pago: PagoAdministracion) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val pagoGuardado = withContext(Dispatchers.IO) {
                repository.guardar(pago)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al guardar pago: ${e.message}"
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
            _error.value = "Error al eliminar pago: ${e.message}"
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
            _error.value = "Error al obtener pago: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun crearCheckoutAdministracion(
        usuarioId: Long,
        apartamento: String,
        valor: Long,
        descripcion: String
    ) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val request = CrearPagoRequest(
                usuarioId = usuarioId,
                apartamento = apartamento,
                valor = valor,
                descripcion = descripcion
            )
            val url = withContext(Dispatchers.IO) {
                repository.crearCheckoutAdministracion(request)
            }
            _checkoutUrl.value = url
        } catch (e: Exception) {
            _error.value = "Error al iniciar checkout: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun consumirCheckoutUrl() {
        _checkoutUrl.value = null
    }

    fun clearError() {
        _error.value = null
    }
}