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

    init {
        cargarHistorial()
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.value = HistorialUiState.Cargando
            try {
                val resultado = RetrofitClient.instance.getHistorial()
                _uiState.value = HistorialUiState.Exito(resultado)
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Borra un registro y recarga la lista
    fun borrarUno(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.borrarCalculo(id)
                cargarHistorial() // recarga la lista después de borrar
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(e.message ?: "Error al borrar")
            }
        }
    }

    // Borra todo y recarga la lista
    fun borrarTodo() {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.borrarHistorial()
                cargarHistorial() // recarga la lista después de borrar
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(e.message ?: "Error al borrar")
            }
        }
    }
}