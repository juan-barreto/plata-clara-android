package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun HistorialScreen() {

    val viewModel: HistorialViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Controla si el diálogo de confirmación está visible
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Diálogo de confirmación para borrar todo
    // Solo se dibuja si mostrarDialogo es true
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false }, // se cierra si tocás fuera
            title = { Text("Borrar historial") },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarTodo()
                    mostrarDialogo = false
                }) {
                    Text("Borrar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    when (uiState) {

        is HistorialUiState.Cargando -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is HistorialUiState.Exito -> {
            val items = (uiState as HistorialUiState.Exito).items

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Encabezado con título y botón borrar todo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de cálculos",
                            style = MaterialTheme.typography.titleLarge
                        )
                        // Botón borrar todo — abre el diálogo
                        Button(
                            onClick = { mostrarDialogo = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar todo")
                        }
                    }
                }

                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.tipo_indice.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Desde: ${item.fecha_inicio}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    // Ícono tacho para borrar uno
                                    IconButton(onClick = { viewModel.borrarUno(item.id) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Borrar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Inicial",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", item.alquiler_inicial)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Ajustado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", item.alquiler_final)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "Calculado: ${item.fecha_calculo.substring(0, 10)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        is HistorialUiState.Error -> {
            val mensaje = (uiState as HistorialUiState.Error).mensaje
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $mensaje", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}