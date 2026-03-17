package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.DolarResponse
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estado del Home — puede estar cargando, exitoso o con error
sealed class HomeUiState {
    object Cargando : HomeUiState()
    data class Exito(
        val nombre: String,
        val dolarBlue: DolarResponse?,
        val dolarOficial: DolarResponse?,
        val ipcUltimo: String?
    ) : HomeUiState()
    data class Error(val mensaje: String) : HomeUiState()
}

// El ViewModel necesita el Context para leer SharedPreferences
// Por eso usamos un Factory — Android no nos deja pasarle parámetros directo
// Equivalente en Python: sería como un constructor con dependencias inyectadas
class HomeViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Cargando)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Cargando
            try {
                // Leemos el nombre guardado
                val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
                val nombre = prefs.getString("nombre_usuario", "Usuario") ?: "Usuario"

                // Traemos los dólares
                val dolares = RetrofitClient.instance.getDolar()

                // Filtramos blue y oficial de la lista
                // Equivalente en Python:
                // blue = next((d for d in dolares if d.casa == "blue"), None)
                val blue = dolares.firstOrNull { it.casa == "blue" }
                val oficial = dolares.firstOrNull { it.casa == "oficial" }

                // Traemos el IPC — ya tenés el endpoint en tu backend
                val ipcDatos = RetrofitClient.instance.getIpc()
                // El IPC viene como lista de [fecha, valor] — tomamos el último
                val ipcUltimo = ipcDatos.lastOrNull()?.let { item ->
                    "IPC ${item.fecha}: ${String.format("%.1f", item.valor)}%"
                }
                _uiState.value = HomeUiState.Exito(
                    nombre = nombre,
                    dolarBlue = blue,
                    dolarOficial = oficial,
                    ipcUltimo = ipcUltimo
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Factory — le dice a Android cómo crear este ViewModel con parámetros
    // Sin esto Android no sabe cómo instanciarlo
    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(context) as T
            }
        }
    }
}