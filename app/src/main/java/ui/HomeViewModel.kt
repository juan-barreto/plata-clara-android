package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.HistorialItem
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Resumen de una categoría para el home
data class CategoriaResumen(
    val nombre: String,
    val gastado: Double,
    val presupuesto: Double
) {
    val disponible: Double get() = presupuesto - gastado
    val porcentaje: Float get() = if (presupuesto > 0) (gastado / presupuesto).toFloat() else 0f
    val excedido: Boolean get() = gastado > presupuesto
}

sealed class HomeUiState {
    object Cargando : HomeUiState()
    data class Exito(
        val nombre: String,
        val ipcUltimo: String?,
        val ipcAnterior: String?,
        val ultimoAlquiler: HistorialItem?,
        val diasParaAjuste: Long?,
        val consejo: String,
        val balance: Double,
        val totalIngresos: Double,
        val totalGastos: Double,
        val categorias: List<CategoriaResumen>
    ) : HomeUiState()
    data class Error(val mensaje: String) : HomeUiState()
}

class HomeViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Cargando)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _balanceVisible = MutableStateFlow(true)
    val balanceVisible: StateFlow<Boolean> = _balanceVisible

    // Para el pull-to-refresh — indica si está recargando
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init { cargarDatos() }

    fun toggleBalanceVisible() {
        _balanceVisible.value = !_balanceVisible.value
    }

    // Función pública para pull-to-refresh
    // No muestra el loading de pantalla completa, solo el indicador del pull
    fun recargar() {
        viewModelScope.launch {
            _isRefreshing.value = true
            cargarDatosInterno(mostrarLoading = false)
            _isRefreshing.value = false
        }
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            cargarDatosInterno(mostrarLoading = true)
        }
    }

    private suspend fun cargarDatosInterno(mostrarLoading: Boolean) {
        if (mostrarLoading) {
            _uiState.value = HomeUiState.Cargando
        }
        try {
            val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
            val nombre = prefs.getString("nombre_usuario", "Usuario") ?: "Usuario"

            // ═══════════════════════════════════════════════════════
            // LLAMADAS EN PARALELO — async lanza cada una al mismo
            // tiempo. awaitAll espera a que terminen todas.
            // Equivalente en Python:
            //   results = await asyncio.gather(get_ipc(), get_historial(), get_presupuesto())
            // ═══════════════════════════════════════════════════════
            val ipcDeferred = viewModelScope.async {
                try { RetrofitClient.instance.getIpc() } catch (e: Exception) { emptyList() }
            }
            val historialDeferred = viewModelScope.async {
                try { RetrofitClient.instance.getHistorial() } catch (e: Exception) { emptyList() }
            }
            val presupuestoDeferred = viewModelScope.async {
                try { RetrofitClient.instance.getPresupuesto("mensual") } catch (e: Exception) { emptyList() }
            }

            // Las 3 llamadas ya están en vuelo — ahora esperamos resultados
            val ipcDatos = ipcDeferred.await()
            val historial = historialDeferred.await()
            val movimientos = presupuestoDeferred.await()

            // ═══════════════════════════════════════════════════════
            // PROCESAMIENTO — todo esto es local, no tarda nada
            // ═══════════════════════════════════════════════════════

            // IPC con valor anterior
            var ipcUltimo: String? = null
            var ipcAnterior: String? = null
            if (ipcDatos.size >= 2) {
                val ultimo = ipcDatos[ipcDatos.size - 1]
                val penultimo = ipcDatos[ipcDatos.size - 2]
                val variacion = ((ultimo.valor - penultimo.valor) / penultimo.valor) * 100
                val partes = ultimo.fecha.split("-")
                val meses = listOf("","Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
                val mes = meses[partes[1].toInt()]
                val anio = partes[0]
                ipcUltimo = "IPC $mes $anio: ${String.format("%.1f", variacion)}%"

                if (ipcDatos.size >= 3) {
                    val antepenultimo = ipcDatos[ipcDatos.size - 3]
                    val variacionAnt = ((penultimo.valor - antepenultimo.valor) / antepenultimo.valor) * 100
                    ipcAnterior = "ant: ${String.format("%.1f", variacionAnt)}%"
                }
            }

            // Historial alquiler
            val ultimoAlquiler = historial.lastOrNull()
            val diasParaAjuste: Long? = try {
                if (ultimoAlquiler != null) {
                    val fechaInicio = java.time.LocalDate.parse(ultimoAlquiler.fecha_inicio)
                    val proximoAjuste = fechaInicio.plusMonths(3)
                    java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), proximoAjuste)
                } else null
            } catch (e: Exception) { null }

            // Presupuesto
            val totalIngresos = movimientos.filter { it.tipo == "ingreso" }.sumOf { it.monto }
            val totalGastos = movimientos.filter { it.tipo == "gasto" }.sumOf { it.monto }
            val balance = totalIngresos - totalGastos

            val gastosPorCategoria = movimientos
                .filter { it.tipo == "gasto" }
                .groupBy { it.categoria.lowercase() }
                .mapValues { (_, items) -> items.sumOf { it.monto } }

            val categoriasConocidas = listOf(
                "supermercado", "transporte", "comida/salidas", "servicios", "salud"
            )
            val gastosVarios = gastosPorCategoria
                .filter { it.key !in categoriasConocidas }
                .values.sum()

            val presupuestoPorCat = if (totalIngresos > 0) totalIngresos / 6 else 0.0

            val categorias = (categoriasConocidas + "varios").map { cat ->
                CategoriaResumen(
                    nombre = cat,
                    gastado = if (cat == "varios") gastosVarios
                    else (gastosPorCategoria[cat] ?: 0.0),
                    presupuesto = presupuestoPorCat
                )
            }

            // Consejo del día
            val consejos = listOf(
                "Guardá al menos el 10% de tus ingresos cada mes.",
                "El dólar blue no es el único refugio — los plazos fijos UVA también ajustan por inflación.",
                "Revisá tu contrato antes de cada ajuste — los errores son más comunes de lo que pensás.",
                "Tres gastos hormiga al día pueden sumar más de $50.000 al mes.",
                "Si tu alquiler ajusta por IPC, el aumento viene con delay de un mes.",
                "El MEP es una alternativa legal para dolarizarte sin salir del sistema bancario.",
                "Anotá tus gastos fijos y variables — lo que no medís, no podés controlar.",
                "El RIPTE refleja los salarios formales — si tu sueldo no lo sigue, perdés poder adquisitivo.",
                "Antes de sacar un préstamo, calculá el costo financiero total, no solo la cuota.",
                "Una canasta básica familiar supera los $800.000 — revisá si tu presupuesto la cubre."
            )
            val diaDelAnio = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)

            _uiState.value = HomeUiState.Exito(
                nombre = nombre,
                ipcUltimo = ipcUltimo,
                ipcAnterior = ipcAnterior,
                ultimoAlquiler = ultimoAlquiler,
                diasParaAjuste = diasParaAjuste,
                consejo = consejos[diaDelAnio % consejos.size],
                balance = balance,
                totalIngresos = totalIngresos,
                totalGastos = totalGastos,
                categorias = categorias
            )
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
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