package com.candlelabs.gestionpersonal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.HistorialItem
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HistorialUiState {
    object Cargando : HistorialUiState()
    data class Exito(val items: List<HistorialItem>) : HistorialUiState()
    data class Error(val mensaje: String) : HistorialUiState()
}

class HistorialViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Cargando)
    val uiState: StateFlow<HistorialUiState> = _uiState

    // Carga el historial apenas se crea el ViewModel
    init {
        cargarHistorial()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            try {
                val resultado = RetrofitClient.instance.getHistorial()
                _uiState.value = HistorialUiState.Exito(resultado)
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}