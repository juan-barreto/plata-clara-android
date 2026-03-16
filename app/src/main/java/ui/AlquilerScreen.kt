package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.navigation.NavController
@Composable
fun AlquilerScreen(navController: androidx.navigation.NavController) {

    val viewModel: AlquilerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Variables que guardan lo que el usuario escribe
    // "remember" hace que sobrevivan a los redibujos de Compose
    // "by" delega la lectura y escritura — igual que con el uiState
    var alquiler by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var indiceSeleccionado by remember { mutableStateOf("ipc") }

    // Lista de índices disponibles para el selector
    val indices = listOf("ipc", "icl")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // espacio entre cada elemento
    ) {

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
        // Campo para el monto del alquiler
        OutlinedTextField(
            value = alquiler,
            onValueChange = { alquiler = it }, // "it" es el nuevo valor cada vez que el usuario escribe
            label = { Text("Monto actual del alquiler") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo para la fecha de inicio
        OutlinedTextField(
            value = fechaInicio,
            onValueChange = { fechaInicio = it },
            label = { Text("Fecha de inicio (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Selector de índice con chips
        Text(text = "Índice de ajuste", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            indices.forEach { indice ->
                FilterChip(
                    selected = indiceSeleccionado == indice, // resalta el seleccionado
                    onClick = { indiceSeleccionado = indice },
                    label = { Text(indice.uppercase()) }
                )
            }
        }

        // Botón calcular
        Button(
            onClick = {
                val monto = alquiler.toDoubleOrNull() // convierte el texto a número
                if (monto != null && fechaInicio.isNotBlank()) {
                    viewModel.calcular(monto, fechaInicio, indiceSeleccionado)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular ajuste")
        }

        // Resultado según el estado
        when (uiState) {

            is AlquilerUiState.Idle -> {
                // No muestra nada — el usuario todavía no calculó
            }

            is AlquilerUiState.Cargando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is AlquilerUiState.Exito -> {
                val historial = (uiState as AlquilerUiState.Exito).respuesta.historial

                Text(
                    text = "Resultado:",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(modifier = Modifier.weight(1f)) { // weight(1f) ocupa el espacio restante
                    items(historial) { periodo ->
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