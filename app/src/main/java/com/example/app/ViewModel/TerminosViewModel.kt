package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Repository.TerminosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TerminosViewModel @Inject constructor(
    private val repository: TerminosRepository
) : ViewModel() {

    private val _fechaAceptacion = MutableStateFlow<String?>(null)
    val fechaAceptacion: StateFlow<String?> = _fechaAceptacion.asStateFlow()

    fun cargarEstado() {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { repository.estado() } }
                .onSuccess { _fechaAceptacion.value = it.fechaAceptacion }
        }
    }
}
