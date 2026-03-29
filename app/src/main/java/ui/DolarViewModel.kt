package com.candlelabs.gestionpersonal.ui
import androidx.lifecycle.ViewModel        // La clase base que hace sobrevivir rotaciones
import androidx.lifecycle.viewModelScope   // El contexto para corrutinas del ViewModel
import com.candlelabs.gestionpersonal.model.DolarResponse  // Tu data class
import com.candlelabs.gestionpersonal.network.RetrofitClient // Tu cliente HTTP
import com.candlelabs.gestionpersonal.network.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow  // Contenedor modificable
import kotlinx.coroutines.flow.StateFlow         // Contenedor de solo lectura
import kotlinx.coroutines.launch// Para lanzar corrutinas

// Los tres estados posibles de la pantalla
sealed class DolarUiState {
    object Cargando : DolarUiState()
    data class Exito(val cotizaciones: List<DolarResponse>) : DolarUiState()
    data class Error(val mensaje: String) : DolarUiState()
}

class DolarViewModel : ViewModel() {

    // _uiState es privado: solo el ViewModel lo puede modificar
    private val _uiState = MutableStateFlow<DolarUiState>(DolarUiState.Cargando)

    // uiState es público: la pantalla solo puede leerlo
    val uiState: StateFlow<DolarUiState> = _uiState

    // Se ejecuta apenas se crea el ViewModel
    init {
        cargarCotizaciones()
    }
    private fun cargarCotizaciones() {
        // viewModelScope: la corrutina vive mientras el ViewModel exista
        viewModelScope.launch {
            try {
                val resultado = RetrofitClient.create(SupabaseClient.instance).getDolar()
                _uiState.value = DolarUiState.Exito(resultado)
            } catch (e: Exception) {
                _uiState.value = DolarUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}