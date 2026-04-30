package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class Notificacion(
    val id: Long? = null,

    @SerializedName("mensaje")
    val mensaje: String? = null,

    @SerializedName("fechaEnvio")
    val fechaEnvio: String? = null,  // Formato ISO 8601

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,  // Ruta de la imagen adjunta

    @SerializedName("videoUrl")
    val videoUrl: String? = null,  // Ruta del video adjunto

    @SerializedName("usuariosEtiquetados")
    val usuariosEtiquetados: String? = null  // JSON array de IDs de usuarios etiquetados
) {
    // Función helper para obtener mensaje seguro (renombrada para evitar conflicto con getter automático)
    fun mensajeSeguro(): String {
        val original = mensaje ?: ""
        return original.replace("Pago PSE", "Pago en línea", ignoreCase = true)
    }
    
    // Función helper para verificar si la notificación es válida
    fun esValida(): Boolean = (id != null) || (mensaje?.isNotBlank() == true)
    
    // Función helper para obtener lista de IDs de usuarios etiquetados
    fun obtenerUsuariosEtiquetados(): List<Long> {
        return try {
            usuariosEtiquetados?.let {
                com.google.gson.Gson().fromJson(it, Array<Long>::class.java).toList()
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}