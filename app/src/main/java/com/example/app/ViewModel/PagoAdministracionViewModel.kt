package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.app.Auth.AuthManager
import com.example.app.Model.ConfirmarPagoResponse
import com.example.app.Model.CrearPagoRequest
import com.example.app.Model.EstadoPagoAdmin
import com.example.app.Model.PagoAdministracion
import com.example.app.Repository.PagoAdministracionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
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

    private val _confirmacionPago = MutableStateFlow<ConfirmarPagoResponse?>(null)
    val confirmacionPago: StateFlow<ConfirmarPagoResponse?> = _confirmacionPago.asStateFlow()

    private val _estadoPagosAdmin = MutableStateFlow<List<EstadoPagoAdmin>>(emptyList())
    val estadoPagosAdmin: StateFlow<List<EstadoPagoAdmin>> = _estadoPagosAdmin.asStateFlow()

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

    fun crearCheckoutAdministracion(
        valor: Long,
        descripcion: String,
        periodo: String = YearMonth.now().toString()
    ) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            if (!AuthManager.isAuthenticated()) {
                _error.value = "No hay sesión activa. Inicia sesión de nuevo."
                return@launch
            }
            if (AuthManager.isTokenExpired()) {
                AuthManager.handleUnauthorized()
                _error.value = "El token JWT expiró. Inicia sesión de nuevo."
                return@launch
            }

            val request = CrearPagoRequest(
                monto = valor,
                valor = valor,
                concepto = descripcion,
                descripcion = descripcion,
                periodo = periodo
            )
            val response = withContext(Dispatchers.IO) {
                repository.crearCheckoutAdministracion(request)
            }
            val url = withContext(Dispatchers.IO) {
                repository.extraerUrlCheckout(response)
            }
            Log.d("PagosCheckout", "Abriendo checkout en navegador: $url")
            _checkoutUrl.value = url
        } catch (e: Exception) {
            Log.e("PagosCheckout", "Error al iniciar checkout", e)
            _error.value = e.message?.ifBlank { "Error al iniciar checkout" } ?: "Error al iniciar checkout"
        } finally {
            _isLoading.value = false
        }
    }

    fun consumirCheckoutUrl() {
        _checkoutUrl.value = null
    }

    fun confirmarPago(referenciaExterna: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        _confirmacionPago.value = null
        try {
            val resultado = withContext(Dispatchers.IO) {
                repository.confirmarPago(referenciaExterna)
            }
            _confirmacionPago.value = resultado
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al confirmar pago: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerEstadoPagosAdmin() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerEstadoPagosAdmin()
            }
            _estadoPagosAdmin.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener estado de pagos: ${e.message}"
            _estadoPagosAdmin.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun consumirConfirmacionPago() {
        _confirmacionPago.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
