package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.AsistenteRequest
import com.candlelabs.gestionpersonal.model.MensajeChat
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MensajeChatUI(
    val rol: String,
    val contenido: String,
    val cargando: Boolean = false
)

class AsistenteViewModel(private val context: Context) : ViewModel() {

    private val _mensajes = MutableStateFlow<List<MensajeChatUI>>(emptyList())
    val mensajes: StateFlow<List<MensajeChatUI>> = _mensajes

    private val _esperando = MutableStateFlow(false)
    val esperando: StateFlow<Boolean> = _esperando

    // Lee el nombre guardado en SharedPreferences
    // Equivalente en Python: prefs.get("nombre_usuario", "Usuario")
    private fun obtenerNombre(): String {
        val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
        return prefs.getString("nombre_usuario", "Usuario") ?: "Usuario"
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank() || _esperando.value) return

        viewModelScope.launch {
            // Agregamos el mensaje del usuario
            val mensajeUsuario = MensajeChatUI(rol = "user", contenido = texto)
            _mensajes.value = _mensajes.value + mensajeUsuario

            // Agregamos burbuja de cargando
            val mensajeCargando = MensajeChatUI(rol = "assistant", contenido = "...", cargando = true)
            _mensajes.value = _mensajes.value + mensajeCargando
            _esperando.value = true

            try {
                // Construimos el historial sin el mensaje de cargando
                val historial = _mensajes.value
                    .filter { !it.cargando }
                    .dropLast(1)
                    .takeLast(10) // solo los últimos 10 mensajes
                    .map { MensajeChat(role = it.rol, content = it.contenido) }

                val request = AsistenteRequest(
                    mensaje = texto,
                    historial = historial,
                    nombre = obtenerNombre()  // nuevo — manda el nombre al backend
                )

                val respuesta = RetrofitClient.create(com.candlelabs.gestionpersonal.network.SupabaseClient.instance).consultarAsistente(request)

                // Reemplazamos el cargando por la respuesta real
                _mensajes.value = _mensajes.value.dropLast(1) + MensajeChatUI(
                    rol = "assistant",
                    contenido = respuesta.respuesta
                )
            } catch (e: Exception) {
                _mensajes.value = _mensajes.value.dropLast(1) + MensajeChatUI(
                    rol = "assistant",
                    contenido = "No pude conectarme. Verificá tu conexión e intentá de nuevo."
                )
            } finally {
                _esperando.value = false
            }
        }
    }

    fun limpiarChat() {
        _mensajes.value = emptyList()
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AsistenteViewModel(context) as T
            }
        }
    }
}