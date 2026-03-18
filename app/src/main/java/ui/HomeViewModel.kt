package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.DolarResponse
import com.candlelabs.gestionpersonal.model.HistorialItem
import com.candlelabs.gestionpersonal.model.VariacionDolarResponse
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Cargando : HomeUiState()
    data class Exito(
        val nombre: String,
        val dolarBlue: DolarResponse?,
        val dolarOficial: DolarResponse?,
        val variacionBlue: VariacionDolarResponse?,
        val variacionOficial: VariacionDolarResponse?,
        val ipcUltimo: String?,
        val ultimoAlquiler: HistorialItem?  // nuevo
    ) : HomeUiState()
    data class Error(val mensaje: String) : HomeUiState()
}

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
                // Nombre guardado en SharedPreferences
                val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
                val nombre = prefs.getString("nombre_usuario", "Usuario") ?: "Usuario"

                // Dólares actuales
                val dolares = RetrofitClient.instance.getDolar()
                val blue = dolares.firstOrNull { it.casa == "blue" }
                val oficial = dolares.firstOrNull { it.casa == "oficial" }

                // Variación — null si no hay historial suficiente
                val variacionBlue = try {
                    RetrofitClient.instance.getVariacionDolar("blue")
                } catch (e: Exception) { null }

                val variacionOficial = try {
                    RetrofitClient.instance.getVariacionDolar("oficial")
                } catch (e: Exception) { null }

                // IPC — variación mensual real
                val ipcDatos = RetrofitClient.instance.getIpc()
                val ipcUltimo = if (ipcDatos.size >= 2) {
                    val ultimo = ipcDatos[ipcDatos.size - 1]
                    val penultimo = ipcDatos[ipcDatos.size - 2]
                    val variacion = ((ultimo.valor - penultimo.valor) / penultimo.valor) * 100
                    val partes = ultimo.fecha.split("-")
                    val meses = listOf("","Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
                    val mes = meses[partes[1].toInt()]
                    val anio = partes[0]
                    "IPC $mes $anio: ${String.format("%.1f", variacion)}%"
                } else null

                // Último cálculo de alquiler
                val ultimoAlquiler = try {
                    val historial = RetrofitClient.instance.getHistorial()
                    historial.lastOrNull()
                } catch (e: Exception) { null }

                _uiState.value = HomeUiState.Exito(
                    nombre = nombre,
                    dolarBlue = blue,
                    dolarOficial = oficial,
                    variacionBlue = variacionBlue,
                    variacionOficial = variacionOficial,
                    ipcUltimo = ipcUltimo,
                    ultimoAlquiler = ultimoAlquiler
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(context) as T
            }
        }
    }
}