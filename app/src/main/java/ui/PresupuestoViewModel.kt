package com.candlelabs.gestionpersonal.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.MovimientoItem
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.model.MovimientoEditRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

sealed class PresupuestoUiState {
    object Cargando : PresupuestoUiState()
    data class Exito(val movimientos: List<MovimientoItem>) : PresupuestoUiState()
    data class Error(val mensaje: String) : PresupuestoUiState()
}

class PresupuestoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PresupuestoUiState>(PresupuestoUiState.Cargando)
    val uiState: StateFlow<PresupuestoUiState> = _uiState

    private val _filtro = MutableStateFlow("mensual")
    val filtro: StateFlow<String> = _filtro

    private val _exportando = MutableStateFlow(false)
    val exportando: StateFlow<Boolean> = _exportando

    private val _mensajeExport = MutableStateFlow<String?>(null)
    val mensajeExport: StateFlow<String?> = _mensajeExport

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
                cargarMovimientos()
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

    fun calcularTotalIngresos(movimientos: List<MovimientoItem>): Double =
        movimientos.filter { it.tipo == "ingreso" }.sumOf { it.monto }

    fun calcularTotalGastos(movimientos: List<MovimientoItem>): Double =
        movimientos.filter { it.tipo == "gasto" }.sumOf { it.monto }

    fun calcularBalance(movimientos: List<MovimientoItem>): Double =
        calcularTotalIngresos(movimientos) - calcularTotalGastos(movimientos)

    fun calcularPorCategoria(movimientos: List<MovimientoItem>): Map<String, Double> =
        movimientos
            .filter { it.tipo == "gasto" }
            .groupBy { it.categoria }
            .mapValues { (_, items) -> items.sumOf { it.monto } }

    // ── EXPORTACIÓN ─────────────────────────────────────────

    fun exportarExcel(context: Context, filtro: String) {
        viewModelScope.launch {
            _exportando.value = true
            try {
                val response = RetrofitClient.instance.exportarExcel(filtro)

                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                        ?: throw Exception("Respuesta vacía del servidor")

                    val nombreArchivo = "PlataClara_${filtro}_${System.currentTimeMillis()}.xlsx"

                    // Guardamos en caché y abrimos el selector de apps
                    compartirArchivo(
                        context = context,
                        bytes = bytes,
                        nombreArchivo = nombreArchivo,
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )

                    _mensajeExport.value = "✅ Archivo descargado correctamente"

                } else {
                    _mensajeExport.value = "❌ Error al exportar (${response.code()})"
                }

            } catch (e: Exception) {
                _mensajeExport.value = "❌ ${e.message}"
            } finally {
                _exportando.value = false
            }
        }
    }

    /**
     * Guarda los bytes en caché y lanza el Share Intent.
     * Usamos FileProvider porque desde Android 7 no se puede compartir
     * una ruta de archivo directamente entre apps — lanza FileUriExposedException.
     * FileProvider genera una URI content:// con permiso temporal de lectura.
     */
    private fun compartirArchivo(
        context: Context,
        bytes: ByteArray,
        nombreArchivo: String,
        mimeType: String
    ) {
        // 1 — Guardamos el archivo en la carpeta de caché de la app
        val archivo = File(context.cacheDir, nombreArchivo)
        FileOutputStream(archivo).use { it.write(bytes) }

        // 2 — FileProvider convierte la ruta en una URI segura
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            archivo
        )

        // 3 — Construimos el Intent de compartir
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Presupuesto Plata Clara")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 4 — Abrimos el selector de apps
        val chooser = Intent.createChooser(intent, "Compartir presupuesto")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun limpiarMensajeExport() {
        _mensajeExport.value = null
    }
}