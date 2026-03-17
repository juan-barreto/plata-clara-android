package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString

@Composable
fun AlquilerScreen(navController: NavController) {

    val viewModel: AlquilerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var alquiler by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFirma by remember { mutableStateOf("") }
    var indiceSeleccionado by remember { mutableStateOf("ipc") }
    var periodoSeleccionado by remember { mutableIntStateOf(3) }

    val indices = listOf("ipc", "icl")

    // Períodos disponibles — número de meses y etiqueta para mostrar
    val periodos = listOf(
        3 to "Trimestral",
        4 to "Cuatrimestral",
        6 to "Semestral",
        12 to "Anual"
    )

    // El formulario necesita scroll porque tiene muchos campos
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Título con botón de info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculadora de alquiler",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { navController.navigate(Rutas.INFO) }) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "¿Cómo funciona?",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Campo monto con prefijo $
        OutlinedTextField(
            value = alquiler,
            onValueChange = { alquiler = it },
            label = { Text("Monto actual del alquiler") },
            prefix = { Text("$") }, // prefijo visible dentro del campo
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo fecha de inicio con formato automático YYYY-MM-DD
        OutlinedTextField(
            value = fechaInicio,
            onValueChange = { input ->
                // Solo guardamos números, máximo 8 dígitos
                val soloNumeros = input.filter { it.isDigit() }.take(8)
                fechaInicio = soloNumeros
            },
            label = { Text("Fecha de último aumento") },
            placeholder = { Text("AAAA-MM-DD") },
            visualTransformation = FechaVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo fecha de firma con el mismo formato automático
        OutlinedTextField(
            value = fechaFirma,
            onValueChange = { input ->
                val soloNumeros = input.filter { it.isDigit() }.take(8)
                fechaFirma = soloNumeros
            },
            label = { Text("Fecha de firma del contrato") },
            placeholder = { Text("AAAA-MM-DD") },
            visualTransformation = FechaVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        // Selector de índice
        Text(text = "Índice de ajuste", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            indices.forEach { indice ->
                FilterChip(
                    selected = indiceSeleccionado == indice,
                    onClick = { indiceSeleccionado = indice },
                    label = { Text(indice.uppercase()) }
                )
            }
        }

        // Mensaje aclaratorio cuando el usuario selecciona ICL
        if (indiceSeleccionado == "icl") {
            Text(
                text = "El ICL es obligatorio para contratos firmados antes del 17/10/2023. " +
                        "Para contratos más nuevos, las partes lo acuerdan libremente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Selector de período
        Text(text = "Período de ajuste", style = MaterialTheme.typography.labelMedium)
        // Primera fila — Trimestral y Cuatrimestral
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periodos.take(2).forEach { (meses, etiqueta) ->
                FilterChip(
                    selected = periodoSeleccionado == meses,
                    onClick = { periodoSeleccionado = meses },
                    label = { Text(etiqueta) }
                )
            }
        }
// Segunda fila — Semestral y Anual
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periodos.drop(2).forEach { (meses, etiqueta) ->
                FilterChip(
                    selected = periodoSeleccionado == meses,
                    onClick = { periodoSeleccionado = meses },
                    label = { Text(etiqueta) }
                )
            }
        }

        // Botón calcular
        Button(
            onClick = {
                val monto = alquiler.toDoubleOrNull()
                if (monto != null && fechaInicio.length == 8 && fechaFirma.length == 8) {
                    // Convertimos "20240615" → "2024-06-15" antes de mandar
                    val fechaInicioFormateada = "${fechaInicio.substring(0,4)}-${fechaInicio.substring(4,6)}-${fechaInicio.substring(6)}"
                    val fechaFirmaFormateada = "${fechaFirma.substring(0,4)}-${fechaFirma.substring(4,6)}-${fechaFirma.substring(6)}"
                    viewModel.calcular(monto, fechaInicioFormateada, fechaFirmaFormateada, indiceSeleccionado, periodoSeleccionado)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular ajuste")
        }

        // Resultado
        when (uiState) {
            is AlquilerUiState.Idle -> {}

            is AlquilerUiState.Cargando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is AlquilerUiState.Exito -> {
                val historial = (uiState as AlquilerUiState.Exito).respuesta.historial

                Text(
                    text = "Resultado:",
                    style = MaterialTheme.typography.titleMedium
                )

                // LazyColumn dentro de Column con scroll necesita altura fija
                // por eso usamos forEach en vez de LazyColumn acá
                historial.forEach { periodo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = periodo.periodo)
                            Text(text = "$${String.format("%.2f", periodo.alquiler)}")
                        }
                    }
                }
            }

            is AlquilerUiState.Error -> {
                val mensaje = (uiState as AlquilerUiState.Error).mensaje
                Text(
                    text = "Error: $mensaje",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
// Transforma visualmente "20240601" → "2024-06-01" sin mover el cursor
class FechaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val result = buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                if (index == 3 || index == 5) append('-')
            }
        }
        val offsetMap = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 5 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> result.length
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 4 -> offset
                    offset <= 7 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> digits.length
                }
            }
        }
        return TransformedText(AnnotatedString(result), offsetMap)
    }
}