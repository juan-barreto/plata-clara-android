package com.candlelabs.gestionpersonal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.AjusteRequest
import com.candlelabs.gestionpersonal.model.AjusteResponse
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Los tres estados posibles — igual que DolarUiState
sealed class AlquilerUiState {
    object Idle : AlquilerUiState()        // estado inicial — no hizo nada todavía
    object Cargando : AlquilerUiState()    // esperando respuesta de la API
    data class Exito(val respuesta: AjusteResponse) : AlquilerUiState()
    data class Error(val mensaje: String) : AlquilerUiState()
}

class AlquilerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AlquilerUiState>(AlquilerUiState.Idle)
    val uiState: StateFlow<AlquilerUiState> = _uiState

    // Se llama cuando el usuario aprieta "Calcular"
    fun calcular(alquiler: Double, fechaInicio: String, indice: String) {
        viewModelScope.launch {
            // Cambia el estado a cargando mientras espera
            _uiState.value = AlquilerUiState.Cargando

            try {
                // Arma el request con los datos del formulario
                val request = AjusteRequest(
                    alquiler = alquiler,
                    fecha_inicio = fechaInicio,
                    indice = indice
                )
                val respuesta = RetrofitClient.instance.calcularAjuste(request)
                _uiState.value = AlquilerUiState.Exito(respuesta)
            } catch (e: Exception) {
                _uiState.value = AlquilerUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}