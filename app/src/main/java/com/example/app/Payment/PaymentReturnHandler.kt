package com.example.app.Payment

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PaymentReturnHandler {

    private val _pendingReferencia = MutableStateFlow<String?>(null)
    val pendingReferencia: StateFlow<String?> = _pendingReferencia.asStateFlow()

    fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (!esUrlResultadoPago(uri)) return

        val ref = uri.getQueryParameter("ref")
            ?: uri.getQueryParameter("referenciaExterna")
            ?: uri.getQueryParameter("external_reference")

        if (!ref.isNullOrBlank()) {
            _pendingReferencia.value = ref.trim()
        }
    }

    fun consumeReferencia() {
        _pendingReferencia.value = null
    }

    private fun esUrlResultadoPago(uri: Uri): Boolean {
        val path = uri.path.orEmpty().lowercase()
        return path.contains("/pagos/resultado")
    }
}
