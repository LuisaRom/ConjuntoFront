package com.example.app.Auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TerminosSesion {

    private val _aceptados = MutableStateFlow<Set<String>>(emptySet())
    val aceptados: StateFlow<Set<String>> = _aceptados.asStateFlow()

    fun clave(usuarioId: Long?, version: String): String = "${usuarioId ?: 0}-$version"

    fun marcarAceptado(usuarioId: Long?, version: String) {
        _aceptados.value = _aceptados.value + clave(usuarioId, version)
    }

    fun estaAceptado(usuarioId: Long?, version: String): Boolean = clave(usuarioId, version) in _aceptados.value
}
