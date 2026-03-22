package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun DolarScreen(navController: NavController) {

    val viewModel: DolarViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F2F2)) // fondo crema unificado
    ) {

        // ─── HEADER NEGRO ───────────────────────────────────────
        // Mismo patrón que el Home y el AsistenteScreen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .background(Color(0xFF000000))
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Cotizaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Subtítulo dinámico: muestra la fecha de la última actualización
                // si hay datos disponibles
                val subtitulo = when (uiState) {
                    is DolarUiState.Exito -> {
                        val primera = (uiState as DolarUiState.Exito).cotizaciones.firstOrNull()
                        primera?.fechaActualizacion?.let { fecha ->
                            // La fecha viene en formato ISO, mostramos solo fecha y hora
                            val limpio = fecha
                                .replace("T", " ")
                                .take(16) // "2024-12-15 14:30"
                            "Actualizado: $limpio"
                        } ?: "Todas las cotizaciones"
                    }
                    else -> "Cargando..."
                }
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888888)
                )
            }
        }

        // ─── CONTENIDO ──────────────────────────────────────────
        when (uiState) {

            is DolarUiState.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00B872))
                }
            }

            is DolarUiState.Exito -> {
                val cotizaciones = (uiState as DolarUiState.Exito).cotizaciones

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(cotizaciones) { cotizacion ->
                        DolarCard(cotizacion = cotizacion, navController = navController)
                    }
                }
            }

            is DolarUiState.Error -> {
                val mensaje = (uiState as DolarUiState.Error).mensaje
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sin conexión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }
}