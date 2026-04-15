package com.candlelabs.gestionpersonal.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.MovimientoItem
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.model.MovimientoEditRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // Totales derivados — se recalculan solo cuando cambia uiState, no en cada recomposición
    val totales: StateFlow<Triple<Double, Double, Double>> = uiState.map { estado ->
        if (estado is PresupuestoUiState.Exito) {
            val ingresos = estado.movimientos.filter { it.tipo == "ingreso" }.sumOf { it.monto }
            val gastos = estado.movimientos.filter { it.tipo == "gasto" }.sumOf { it.monto }
            Triple(ingresos, gastos, ingresos - gastos)
        } else Triple(0.0, 0.0, 0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Triple(0.0, 0.0, 0.0))

    init { cargarMovimientos() }

    fun cargarMovimientos() {
        viewModelScope.launch {
            _uiState.value = PresupuestoUiState.Cargando
            try {
                SupabaseClient.instance.auth.awaitInitialization() // ← agregar esto
                val movimientos = RetrofitClient.create(SupabaseClient.instance).getPresupuesto(_filtro.value)
                _uiState.value = PresupuestoUiState.Exito(movimientos)
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun recargar() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                SupabaseClient.instance.auth.awaitInitialization() // ← agregar
                val movimientos = RetrofitClient.create(SupabaseClient.instance).getPresupuesto(_filtro.value)
                _uiState.value = PresupuestoUiState.Exito(movimientos)
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error desconocido")
            }
            _isRefreshing.value = false
        }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        _filtro.value = nuevoFiltro
        cargarMovimientos()
    }

    fun agregarMovimiento(tipo: String, categoria: String, descripcion: String, monto: Double) {
        viewModelScope.launch {
            try {
                RetrofitClient.create(SupabaseClient.instance).agregarMovimiento(
                    MovimientoRequest(tipo, categoria, descripcion, monto)
                )
                cargarMovimientos()
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al agregar")
            }
        }
    }

    fun editarMovimiento(id: String, tipo: String, categoria: String, descripcion: String, monto: Double) {
        viewModelScope.launch {
            try {
                RetrofitClient.create(SupabaseClient.instance).editarMovimiento(
                    id, MovimientoEditRequest(tipo, categoria, descripcion, monto)
                )
                cargarMovimientos()
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al editar")
            }
        }
    }

    fun borrarMovimiento(id: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.create(SupabaseClient.instance).borrarMovimiento(id)
                cargarMovimientos()
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al borrar")
            }
        }
    }

    fun resetMovimientos() {
        viewModelScope.launch {
            try {
                RetrofitClient.create(SupabaseClient.instance).resetPresupuesto()
                cargarMovimientos()
            } catch (e: Exception) {
                _uiState.value = PresupuestoUiState.Error(e.message ?: "Error al resetear")
            }
        }
    }

    fun calcularPorCategoria(movimientos: List<MovimientoItem>): Map<String, Double> =
        movimientos
            .filter { it.tipo == "gasto" }
            .groupBy { it.categoria }
            .mapValues { (_, items) -> items.sumOf { it.monto } }

    fun exportarExcel(context: Context, filtro: String) {
        viewModelScope.launch {
            _exportando.value = true
            try {
                val response = RetrofitClient.create(SupabaseClient.instance).exportarExcel(filtro)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes() ?: throw Exception("Respuesta vacía")
                    val nombre = "PlataClara_${filtro}_${System.currentTimeMillis()}.xlsx"
                    guardarEnDescargas(context, bytes, nombre, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    _mensajeExport.value = "Excel guardado en Descargas"
                    delay(1000)
                    compartirArchivo(context, bytes, nombre, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                } else {
                    _mensajeExport.value = "Error al exportar (${response.code()})"
                }
            } catch (e: Exception) {
                _mensajeExport.value = "Error: ${e.message}"
            } finally {
                _exportando.value = false
            }
        }
    }

    fun exportarPdf(context: Context, filtro: String) {
        viewModelScope.launch {
            _exportando.value = true
            try {
                val response = RetrofitClient.create(SupabaseClient.instance).exportarPdf(filtro)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes() ?: throw Exception("Respuesta vacía")
                    val nombre = "PlataClara_${filtro}_${System.currentTimeMillis()}.pdf"
                    guardarEnDescargas(context, bytes, nombre, "application/pdf")
                    _mensajeExport.value = "PDF guardado en Descargas"
                    delay(1000)
                    compartirArchivo(context, bytes, nombre, "application/pdf")
                } else {
                    _mensajeExport.value = "Error al exportar PDF (${response.code()})"
                }
            } catch (e: Exception) {
                _mensajeExport.value = "Error: ${e.message}"
            } finally {
                _exportando.value = false
            }
        }
    }

    private fun guardarEnDescargas(context: Context, bytes: ByteArray, nombreArchivo: String, mimeType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            }
        } else {
            val archivo = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
            FileOutputStream(archivo).use { it.write(bytes) }
        }
    }

    private fun compartirArchivo(context: Context, bytes: ByteArray, nombreArchivo: String, mimeType: String) {
        val archivo = File(context.cacheDir, nombreArchivo)
        FileOutputStream(archivo).use { it.write(bytes) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Presupuesto Plata Clara")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir presupuesto").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun limpiarMensajeExport() { _mensajeExport.value = null }
}