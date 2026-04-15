package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun HistorialScreen() {

    val viewModel: HistorialViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Borrar historial", color = TextoPrimario) },
            text  = { Text("¿Estás seguro? Esta acción no se puede deshacer.", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = { viewModel.borrarTodo(); mostrarDialogo = false }) {
                    Text("Borrar todo", color = RojoGasto)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
    ) {
        // ── Header ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Historial", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = TextoPrimario)
                Text("Cálculos de alquiler", style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario)
            }
            IconButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Rounded.DeleteSweep, null, tint = RojoGasto,
                    modifier = Modifier.size(26.dp))
            }
        }

        when (uiState) {
            is HistorialUiState.Cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VerdePrimario)
                }
            }

            is HistorialUiState.Exito -> {
                val items = (uiState as HistorialUiState.Exito).items

                if (items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.ReceiptLong, null, tint = TextoMuted,
                                modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("No hay cálculos que mostrar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium, color = TextoPrimario)
                            Text("Calculá tu primer ajuste de alquiler",
                                style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                        }
                    }
                    return@Column
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(16.dp),
                                    ambientColor = SombraCard, spotColor = SombraCard),
                            shape  = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Column(Modifier.padding(14.dp)) {

                                // ── Fila superior — índice + fecha inicio + borrar ──
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape  = RoundedCornerShape(8.dp),
                                        color  = VerdePrimario.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            item.tipo_indice.uppercase(),
                                            style    = MaterialTheme.typography.labelSmall,
                                            color    = VerdePrimario,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Desde: ${item.fecha_inicio}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextoSecundario)
                                        IconButton(
                                            onClick  = { viewModel.borrarUno(item.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.Delete, null,
                                                tint = RojoGasto, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                HorizontalDivider(color = Divisor, modifier = Modifier.padding(vertical = 10.dp))

                                // ── Montos ────────────────────────────────────────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("INICIAL", style = MaterialTheme.typography.labelSmall,
                                            color = TextoSecundario, letterSpacing = 0.8.sp)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "$${String.format("%,.0f", item.alquiler_inicial).replace(",", ".")}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold, color = TextoPrimario
                                        )
                                    }
                                    Icon(Icons.Rounded.ArrowForward, null,
                                        tint = TextoMuted, modifier = Modifier.size(20.dp)
                                            .align(Alignment.CenterVertically))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("AJUSTADO", style = MaterialTheme.typography.labelSmall,
                                            color = TextoSecundario, letterSpacing = 0.8.sp)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "$${String.format("%,.0f", item.alquiler_final).replace(",", ".")}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold, color = VerdePrimario
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text("Calculado el ${item.fecha_calculo.substring(0, 10)}",
                                    style = MaterialTheme.typography.labelSmall, color = TextoMuted)
                            }
                        }
                    }
                }
            }

            is HistorialUiState.Error -> {
                val mensaje = (uiState as HistorialUiState.Error).mensaje
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $mensaje", color = RojoGasto)
                }
            }
        }
    }
}