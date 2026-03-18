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
        val ultimoAlquiler: HistorialItem?,
        val diasParaAjuste: Long?,
        val consejo: String
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
                // Nombre
                val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
                val nombre = prefs.getString("nombre_usuario", "Usuario") ?: "Usuario"

                // Dólares
                val dolares = RetrofitClient.instance.getDolar()
                val blue = dolares.firstOrNull { it.casa == "blue" }
                val oficial = dolares.firstOrNull { it.casa == "oficial" }

                // Variación
                val variacionBlue = try {
                    RetrofitClient.instance.getVariacionDolar("blue")
                } catch (e: Exception) { null }

                val variacionOficial = try {
                    RetrofitClient.instance.getVariacionDolar("oficial")
                } catch (e: Exception) { null }

                // IPC
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

                // Historial — último alquiler + días para ajuste
                val historial = try {
                    RetrofitClient.instance.getHistorial()
                } catch (e: Exception) { emptyList() }

                val ultimoAlquiler = historial.lastOrNull()

                // Próximo ajuste — asume trimestral por defecto
                // Próximo ajuste — usando Calendar en vez de java.time (compatible con API 24)
                // Próximo ajuste — java.time disponible desde minSdk 26
                val diasParaAjuste: Long? = try {
                    if (ultimoAlquiler != null) {
                        val fechaInicio = java.time.LocalDate.parse(ultimoAlquiler.fecha_inicio)
                        val proximoAjuste = fechaInicio.plusMonths(3)
                        val hoy = java.time.LocalDate.now()
                        java.time.temporal.ChronoUnit.DAYS.between(hoy, proximoAjuste)
                    } else null
                } catch (e: Exception) { null }

                // Consejo del día — rota según el día del año
                val consejos = listOf(
                    "💡 Guardá al menos el 10% de tus ingresos cada mes.",
                    "💡 El dólar blue no es el único refugio — los plazos fijos UVA también ajustan por inflación.",
                    "💡 Revisá tu contrato antes de cada ajuste — los errores son más comunes de lo que pensás.",
                    "💡 Tres gastos hormiga al día pueden sumar más de $50.000 al mes.",
                    "💡 Si tu alquiler ajusta por IPC, el aumento viene con delay de un mes.",
                    "💡 El MEP es una alternativa legal para dolarizarte sin salir del sistema bancario.",
                    "💡 Anotá tus gastos fijos y variables — lo que no medís, no podés controlar.",
                    "💡 El RIPTE refleja los salarios formales — si tu sueldo no lo sigue, perdés poder adquisitivo.",
                    "💡 Antes de sacar un préstamo, calculá el costo financiero total, no solo la cuota.",
                    "💡 Una canasta básica familiar supera los $800.000 — revisá si tu presupuesto la cubre."
                )
                val diaDelAnio = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
                val consejo = consejos[diaDelAnio % consejos.size]

                // Todo calculado — actualizamos el estado
                _uiState.value = HomeUiState.Exito(
                    nombre = nombre,
                    dolarBlue = blue,
                    dolarOficial = oficial,
                    variacionBlue = variacionBlue,
                    variacionOficial = variacionOficial,
                    ipcUltimo = ipcUltimo,
                    ultimoAlquiler = ultimoAlquiler,
                    diasParaAjuste = diasParaAjuste,
                    consejo = consejo
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