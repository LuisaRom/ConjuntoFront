package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.ConocimientoIA
import com.example.app.Model.RespuestaMalCalificada
import com.example.app.Model.ResumenGestionIA
import com.example.app.Repository.IaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GestionIaViewModel @Inject constructor(
    private val repository: IaRepository
) : ViewModel() {

    private val _resumen = MutableStateFlow<ResumenGestionIA?>(null)
    val resumen: StateFlow<ResumenGestionIA?> = _resumen.asStateFlow()

    private val _conocimiento = MutableStateFlow<List<ConocimientoIA>>(emptyList())
    val conocimiento: StateFlow<List<ConocimientoIA>> = _conocimiento.asStateFlow()

    private val _filtroEstado = MutableStateFlow("PENDIENTE")
    val filtroEstado: StateFlow<String> = _filtroEstado.asStateFlow()

    private val _negativos = MutableStateFlow<List<RespuestaMalCalificada>>(emptyList())
    val negativos: StateFlow<List<RespuestaMalCalificada>> = _negativos.asStateFlow()

    private val _comunicado = MutableStateFlow<String?>(null)
    val comunicado: StateFlow<String?> = _comunicado.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    fun cargarResumen() = ejecutar("No se pudo generar el resumen") {
        _resumen.value = repository.resumenGestion()
    }

    fun cambiarFiltro(estado: String) {
        _filtroEstado.value = estado
        cargarConocimiento()
    }

    fun cargarConocimiento() = ejecutar("No se pudo cargar el conocimiento") {
        val estado = _filtroEstado.value.takeIf { it != "TODOS" }
        _conocimiento.value = repository.conocimiento(estado)
    }

    fun cargarNegativos() = ejecutar("No se pudieron cargar las respuestas") {
        _negativos.value = repository.respuestasMalCalificadas()
    }

    fun crearConocimiento(titulo: String, contenido: String, categoria: String) =
        ejecutar("No se pudo guardar el conocimiento") {
            repository.crearConocimiento(titulo, contenido, categoria)
            _aviso.value = "Conocimiento guardado. El asistente ya puede usarlo."
            _conocimiento.value = repository.conocimiento(_filtroEstado.value.takeIf { it != "TODOS" })
        }

    fun aprobar(id: Long) = ejecutar("No se pudo aprobar") {
        repository.aprobarConocimiento(id)
        _conocimiento.value = repository.conocimiento(_filtroEstado.value.takeIf { it != "TODOS" })
    }

    fun rechazar(id: Long) = ejecutar("No se pudo rechazar") {
        repository.rechazarConocimiento(id)
        _conocimiento.value = repository.conocimiento(_filtroEstado.value.takeIf { it != "TODOS" })
    }

    fun eliminar(id: Long) = ejecutar("No se pudo eliminar") {
        repository.eliminarConocimiento(id)
        _conocimiento.value = repository.conocimiento(_filtroEstado.value.takeIf { it != "TODOS" })
    }

    fun redactarComunicado(tema: String, tono: String, detalles: String) =
        ejecutar("No se pudo redactar el comunicado") {
            _comunicado.value = repository.redactarComunicado(tema, tono, detalles.ifBlank { null }).texto
        }

    fun limpiarAviso() {
        _aviso.value = null
    }

    private fun ejecutar(errorBase: String, bloque: suspend () -> Unit) = viewModelScope.launch {
        _cargando.value = true
        try {
            withContext(Dispatchers.IO) { bloque() }
        } catch (e: Exception) {
            _aviso.value = "$errorBase: ${e.message ?: "error de conexión"}"
        } finally {
            _cargando.value = false
        }
    }
}
