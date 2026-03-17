package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import com.candlelabs.gestionpersonal.model.VariacionDolarResponse

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current

    // Usamos el factory porque el ViewModel necesita el Context
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {

        is HomeUiState.Cargando -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is HomeUiState.Error -> {
            val mensaje = (uiState as HomeUiState.Error).mensaje
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $mensaje", color = MaterialTheme.colorScheme.error)
            }
        }

        is HomeUiState.Exito -> {
            val datos = uiState as HomeUiState.Exito

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // — HEADER —
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${datos.nombre} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¿Cómo está el bolsillo hoy?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = "🇦🇷", fontSize = 32.sp)
                }

                // — INDICADORES —
                Text(
                    text = "Indicadores del día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Card Dólar Blue
                datos.dolarBlue?.let { blue ->
                    IndicadorCard(
                        titulo = "Dólar Blue",
                        compra = blue.compra,
                        venta = blue.venta ,
                        variacion = datos.variacionBlue
                    )
                }

                // Card Dólar Oficial
                datos.dolarOficial?.let { oficial ->
                    IndicadorCard(
                        titulo = "Dólar Oficial",
                        compra = oficial.compra,
                        venta = oficial.venta,
                        variacion = datos.variacionOficial
                    )
                }

                // Card IPC
                datos.ipcUltimo?.let { ipc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Inflación",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = ipc,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // — ACCESOS RÁPIDOS —
                Text(
                    text = "Accesos rápidos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Botón calcular alquiler
                Button(
                    onClick = { navController.navigate(Rutas.ALQUILER) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Calculate, contentDescription = null)
                    Text(
                        text = "  Calcular alquiler",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Botón cotizaciones
                OutlinedButton(
                    onClick = { navController.navigate(Rutas.DOLAR) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null)
                    Text(
                        text = "  Ver cotizaciones",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Botón historial
                OutlinedButton(
                    onClick = { navController.navigate(Rutas.HISTORIAL) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.History, contentDescription = null)
                    Text(
                        text = "  Historial de cálculos",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Componente reutilizable para cards de dólar
// Lo separamos porque se repite — DRY (Don't Repeat Yourself)
@Composable
fun IndicadorCard(
    titulo: String,
    compra: Double,
    venta: Double,
    variacion: VariacionDolarResponse? = null  // null si no hay historial todavía
) {
    // Determinamos color y flecha según la variación
    // Si no hay variación todavía mostramos neutro
    val (colorVariacion, flecha) = when {
        variacion == null -> Pair(MaterialTheme.colorScheme.onPrimaryContainer, "")
        variacion.variacion_porcentual > 0 -> Pair(Color(0xFF16A34A), "↑")  // verde
        variacion.variacion_porcentual < 0 -> Pair(Color(0xFFDC2626), "↓")  // rojo
        else -> Pair(MaterialTheme.colorScheme.onPrimaryContainer, "→")      // neutro
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Título + variación porcentual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Flecha + porcentaje — solo si hay historial
                if (variacion != null) {
                    Text(
                        text = "$flecha ${String.format("%.2f", variacion.variacion_porcentual)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorVariacion
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compra y venta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Compra
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Compra",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$${String.format("%.0f", compra)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    // Valor anterior chico — solo si hay historial
                    if (variacion != null) {
                        Text(
                            text = "ant: $${String.format("%.0f", variacion.compra_anterior)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorVariacion
                        )
                    }
                }

                // Venta
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Venta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$${String.format("%.0f", venta)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    // Valor anterior chico
                    if (variacion != null) {
                        Text(
                            text = "ant: $${String.format("%.0f", variacion.venta_anterior)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorVariacion
                        )
                    }
                }
            }
        }
    }
}