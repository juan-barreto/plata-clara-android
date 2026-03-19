package com.candlelabs.gestionpersonal.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.candlelabs.gestionpersonal.model.MovimientoItem
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.model.MovimientoEditRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    private val _exportando = MutableStateFlow(false)
    val exportando: StateFlow<Boolean> = _exportando

    // Mensaje de resultado — null cuando no hay nada que mostrar
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
    fun calcularTotalIngresos(movimientos: List<MovimientoItem>): Double =
        movimientos.filter { it.tipo == "ingreso" }.sumOf { it.monto }

    // Calcula el total de gastos
    fun calcularTotalGastos(movimientos: List<MovimientoItem>): Double =
        movimientos.filter { it.tipo == "gasto" }.sumOf { it.monto }

    // Calcula el balance — puede ser positivo o negativo
    fun calcularBalance(movimientos: List<MovimientoItem>): Double =
        calcularTotalIngresos(movimientos) - calcularTotalGastos(movimientos)

    // Agrupa los gastos por categoría para el gráfico
    // Equivalente en Python:
    // {cat: sum(m['monto'] for m in movimientos if m['categoria'] == cat)
    //  for cat in categorias}
    fun calcularPorCategoria(movimientos: List<MovimientoItem>): Map<String, Double> =
        movimientos
            .filter { it.tipo == "gasto" }
            .groupBy { it.categoria }
            .mapValues { (_, items) -> items.sumOf { it.monto } }

    // ── EXPORTACIÓN EXCEL ────────────────────────────────────

    fun exportarExcel(context: Context, filtro: String) {
        viewModelScope.launch {
            _exportando.value = true
            try {
                val response = RetrofitClient.instance.exportarExcel(filtro)

                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                        ?: throw Exception("Respuesta vacía del servidor")

                    val nombreArchivo = "PlataClara_${filtro}_${System.currentTimeMillis()}.xlsx"

                    // Paso 1 — guardamos en Descargas (siempre)
                    guardarEnDescargas(
                        context, bytes, nombreArchivo,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    _mensajeExport.value = "✅ Excel guardado en Descargas"

                    // Paso 2 — esperamos 1 segundo y abrimos el selector
                    // El delay da tiempo para que el usuario vea el mensaje antes del Intent
                    delay(1000)
                    compartirArchivo(
                        context, bytes, nombreArchivo,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )

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

    // ── EXPORTACIÓN PDF ──────────────────────────────────────

    fun exportarPdf(context: Context, filtro: String) {
        viewModelScope.launch {
            _exportando.value = true
            try {
                val response = RetrofitClient.instance.exportarPdf(filtro)

                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                        ?: throw Exception("Respuesta vacía del servidor")

                    val nombreArchivo = "PlataClara_${filtro}_${System.currentTimeMillis()}.pdf"

                    // Paso 1 — guardamos en Descargas (siempre)
                    guardarEnDescargas(context, bytes, nombreArchivo, "application/pdf")
                    _mensajeExport.value = "✅ PDF guardado en Descargas"

                    // Paso 2 — esperamos 1 segundo y abrimos el selector
                    delay(1000)
                    compartirArchivo(context, bytes, nombreArchivo, "application/pdf")

                } else {
                    _mensajeExport.value = "❌ Error al exportar PDF (${response.code()})"
                }

            } catch (e: Exception) {
                _mensajeExport.value = "❌ ${e.message}"
            } finally {
                _exportando.value = false
            }
        }
    }

    // ── FUNCIONES PRIVADAS ───────────────────────────────────

    /**
     * Guarda el archivo en la carpeta Descargas del dispositivo.
     * Android 10+ usa MediaStore (API moderna que no necesita permiso de escritura).
     * Android 9 y anterior escribe directo al filesystem con el permiso WRITE_EXTERNAL_STORAGE.
     */
    private fun guardarEnDescargas(
        context: Context,
        bytes: ByteArray,
        nombreArchivo: String,
        mimeType: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ — MediaStore
            // Equivalente en Python: pathlib.Path("Descargas/archivo").write_bytes(bytes)
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(bytes)
                }
            }
        } else {
            // Android 9 y anterior — filesystem directo
            val archivo = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                nombreArchivo
            )
            FileOutputStream(archivo).use { it.write(bytes) }
        }
    }

    /**
     * Guarda los bytes en caché y lanza el Share Intent.
     *
     * ¿Por qué caché y no Descargas?
     * Porque el Intent necesita una URI generada por FileProvider, y FileProvider
     * solo puede trabajar con rutas que declaramos en file_paths.xml.
     * La carpeta de caché es privada de la app → perfecta para archivos temporales.
     *
     * ¿Por qué FileProvider y no la ruta directa?
     * Desde Android 7 (API 24), compartir una ruta de archivo directamente
     * entre apps lanza FileUriExposedException. FileProvider genera una URI
     * content:// con permisos temporales de lectura → seguro para el Intent.
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

        // 2 — FileProvider convierte la ruta del archivo en una URI segura
        // La "authority" tiene que coincidir exactamente con lo declarado en el Manifest
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // → "com.candlelabs.gestionpersonal.provider"
            archivo
        )

        // 3 — Construimos el Intent de compartir
        // ACTION_SEND = "quiero compartir algo con otra app"
        // FLAG_GRANT_READ_URI_PERMISSION = le da permiso de lectura TEMPORAL a la app receptora
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Presupuesto Plata Clara")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // necesario cuando el Intent sale de un ViewModel
        }

        // 4 — Abrimos el selector de apps (chooser)
        // createChooser fuerza que el sistema muestre el selector
        // aunque solo haya una app compatible
        val chooser = Intent.createChooser(intent, "Compartir presupuesto")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun limpiarMensajeExport() {
        _mensajeExport.value = null
    }
}