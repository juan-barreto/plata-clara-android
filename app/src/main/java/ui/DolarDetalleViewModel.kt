package com.candlelabs.gestionpersonal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.HistorialCotizacionItem
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DolarDetalleUiState {
    object Cargando : DolarDetalleUiState()
    data class Exito(val historial: List<HistorialCotizacionItem>) : DolarDetalleUiState()
    data class Error(val mensaje: String) : DolarDetalleUiState()
}

// Necesita la casa como parámetro — igual que HomeViewModel necesitaba Context
// Por eso usamos Factory de nuevo
class DolarDetalleViewModel(private val casa: String) : ViewModel() {

    private val _uiState = MutableStateFlow<DolarDetalleUiState>(DolarDetalleUiState.Cargando)
    val uiState: StateFlow<DolarDetalleUiState> = _uiState

    init {
        cargarHistorial()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.value = DolarDetalleUiState.Cargando
            try {
                val historial = RetrofitClient.instance.getHistorialCotizacion(casa)
                _uiState.value = DolarDetalleUiState.Exito(historial)
            } catch (e: Exception) {
                _uiState.value = DolarDetalleUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    companion object {
        fun factory(casa: String) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DolarDetalleViewModel(casa) as T
            }
        }
    }
}