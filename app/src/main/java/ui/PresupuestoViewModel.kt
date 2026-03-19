package com.candlelabs.gestionpersonal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.MovimientoItem
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.model.MovimientoEditRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream

// Los estados posibles de la pantalla
sealed class PresupuestoUiState {
    object Cargando : PresupuestoUiState()
    data class Exito(val movimientos: List<MovimientoItem>) : PresupuestoUiState()
    data class Error(val mensaje: String) : PresupuestoUiState()
}

class PresupuestoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PresupuestoUiState>(PresupuestoUiState.Cargando)
    val uiState: StateFlow<PresupuestoUiState> = _uiState

    // Filtro actual — arranca en mensual
    // Equivalente en Python: filtro_actual = "mensual"
    private val _filtro = MutableStateFlow("mensual")
    val filtro: StateFlow<String> = _filtro

    init {
        cargarMovimientos()
    }

    fun cargarMovimientos() {
        viewModelScope.launch {
            _uiState.value = PresupuestoUiState.Cargando
            try {
                val movimientos = RetrofitClient.instance.getPresupuesto(_filtro.value)
                _uiState.value = PresupuestoUiState.Exito(movimientos)
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Cambia el filtro y recarga los datos
    // Equivalente en Python:
    // def cambiar_filtro(nuevo_filtro):
    //     filtro_actual = nuevo_filtro
    //     cargar_movimientos()
    fun cambiarFiltro(nuevoFiltro: String) {
        _filtro.value = nuevoFiltro
        cargarMovimientos()
    }

    fun agregarMovimiento(tipo: String, categoria: String, descripcion: String, monto: Double) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.agregarMovimiento(
                    MovimientoRequest(tipo, categoria, descripcion, monto)
                )
                cargarMovimientos() // recarga la lista después de agregar
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al agregar")
            }
        }
    }

    fun editarMovimiento(id: Int, tipo: String, categoria: String, descripcion: String, monto: Double) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.editarMovimiento(
                    id,
                    MovimientoEditRequest(tipo, categoria, descripcion, monto)
                )
                cargarMovimientos()
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al editar")
            }
        }
    }

    fun borrarMovimiento(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.borrarMovimiento(id)
                cargarMovimientos()
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al borrar")
            }
        }
    }

    // Calcula el total de ingresos del período actual
    // Equivalente en Python:
    // sum(m['monto'] for m in movimientos if m['tipo'] == 'ingreso')
    fun calcularTotalIngresos(movimientos: List<MovimientoItem>): Double {
        return movimientos.filter { it.tipo == "ingreso" }.sumOf { it.monto }
    }

    // Calcula el total de gastos
    fun calcularTotalGastos(movimientos: List<MovimientoItem>): Double {
        return movimientos.filter { it.tipo == "gasto" }.sumOf { it.monto }
    }

    // Calcula el balance — puede ser positivo o negativo
    fun calcularBalance(movimientos: List<MovimientoItem>): Double {
        return calcularTotalIngresos(movimientos) - calcularTotalGastos(movimientos)
    }

    // Agrupa los gastos por categoría para el gráfico
    // Equivalente en Python:
    // {cat: sum(m['monto'] for m in movimientos if m['categoria'] == cat)
    //  for cat in categorias}
    fun calcularPorCategoria(movimientos: List<MovimientoItem>): Map<String, Double> {
        return movimientos
            .filter { it.tipo == "gasto" }
            .groupBy { it.categoria }
            .mapValues { (_, items) -> items.sumOf { it.monto } }
    }
    private val _exportando = MutableStateFlow(false)
    val exportando: StateFlow<Boolean> = _exportando

    private val _mensajeExport = MutableStateFlow<String?>(null)
    val mensajeExport: StateFlow<String?> = _mensajeExport

    fun exportarExcel(context: Context, filtro: String) {
        viewModelScope.launch {
            _exportando.value = true
            try {
                val response = RetrofitClient.instance.exportarExcel(filtro)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Guardamos en la carpeta Descargas del dispositivo
                        // Equivalente en Python: open("Descargas/archivo.xlsx", "wb").write(bytes)
                        val nombreArchivo = "PlataClara_${filtro}_${System.currentTimeMillis()}.xlsx"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Android 10+ — usamos MediaStore (la forma moderna)
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                                put(MediaStore.Downloads.MIME_TYPE,
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }
                            val uri = context.contentResolver.insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
                            )
                            uri?.let {
                                context.contentResolver.openOutputStream(it)?.use { stream ->
                                    stream.write(body.bytes())
                                }
                            }
                        } else {
                            // Android 9 y anterior — escribimos directo al filesystem
                            val archivo = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                nombreArchivo
                            )
                            FileOutputStream(archivo).use { it.write(body.bytes()) }
                        }

                        _mensajeExport.value = "✅ Guardado en Descargas"
                    }
                } else {
                    _mensajeExport.value = "❌ Error al exportar"
                }
            } catch (e: Exception) {
                _mensajeExport.value = "❌ ${e.message}"
            } finally {
                _exportando.value = false
            }
        }
    }

    fun limpiarMensajeExport() {
        _mensajeExport.value = null
    }
}