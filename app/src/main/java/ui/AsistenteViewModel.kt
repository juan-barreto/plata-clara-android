package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.AsistenteRequest
import com.candlelabs.gestionpersonal.model.CategoriaContexto
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

    // ── Contexto financiero — se actualiza desde AsistenteScreen ──
    // AsistenteScreen tiene acceso al HomeViewModel y lo pasa acá
    private var ingreso: Double = 0.0
    private var gastos: Double = 0.0
    private var balance: Double = 0.0
    private var categorias: List<CategoriaContexto> = emptyList()

    fun actualizarContexto(
        ingreso: Double,
        gastos: Double,
        balance: Double,
        categorias: List<CategoriaContexto>
    ) {
        this.ingreso   = ingreso
        this.gastos    = gastos
        this.balance   = balance
        this.categorias = categorias
    }

    private fun obtenerNombre(): String {
        val prefs = context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
        return prefs.getString("nombre_usuario", "Usuario") ?: "Usuario"
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank() || _esperando.value) return

        viewModelScope.launch {
            val mensajeUsuario  = MensajeChatUI(rol = "user", contenido = texto)
            _mensajes.value     = _mensajes.value + mensajeUsuario

            val mensajeCargando = MensajeChatUI(rol = "assistant", contenido = "...", cargando = true)
            _mensajes.value     = _mensajes.value + mensajeCargando
            _esperando.value    = true

            try {
                val historial = _mensajes.value
                    .filter { !it.cargando }
                    .dropLast(1)
                    .takeLast(10)
                    .map { MensajeChat(role = it.rol, content = it.contenido) }

                val request = AsistenteRequest(
                    mensaje   = texto,
                    historial = historial,
                    nombre    = obtenerNombre(),
                    ingreso   = ingreso,
                    gastos    = gastos,
                    balance   = balance,
                    categorias = categorias
                )

                val respuesta = RetrofitClient.create(SupabaseClient.instance).consultarAsistente(request)

                _mensajes.value = _mensajes.value.dropLast(1) + MensajeChatUI(
                    rol      = "assistant",
                    contenido = respuesta.respuesta
                )
            } catch (e: Exception) {
                _mensajes.value = _mensajes.value.dropLast(1) + MensajeChatUI(
                    rol      = "assistant",
                    contenido = "No pude conectarme. Verificá tu conexión e intentá de nuevo."
                )
            } finally {
                _esperando.value = false
            }
        }
    }

    fun limpiarChat() { _mensajes.value = emptyList() }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AsistenteViewModel(context) as T
            }
        }
    }
}