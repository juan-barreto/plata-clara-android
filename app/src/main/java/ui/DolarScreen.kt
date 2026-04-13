package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun DolarScreen(navController: NavController) {
    val viewModel: DolarViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(FondoNegro)) {

        // ── Header ───────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().height(75.dp).background(FondoNegro).padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Cotizaciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                val subtitulo = when (uiState) {
                    is DolarUiState.Exito -> {
                        val primera = (uiState as DolarUiState.Exito).cotizaciones.firstOrNull()
                        primera?.fechaActualizacion?.let { fecha ->
                            "Actualizado: ${fecha.replace("T", " ").take(16)}"
                        } ?: "Todas las cotizaciones"
                    }
                    else -> "Cargando..."
                }
                Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
            }
        }

        // ── Contenido ─────────────────────────────────────────
        when (uiState) {
            is DolarUiState.Cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VerdePrimario)
                }
            }
            is DolarUiState.Exito -> {
                val cotizaciones = (uiState as DolarUiState.Exito).cotizaciones
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(cotizaciones) { cotizacion ->
                        DolarCard(cotizacion = cotizacion, navController = navController)
                    }
                }
            }
            is DolarUiState.Error -> {
                val mensaje = (uiState as DolarUiState.Error).mensaje
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sin conexión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario)
                        Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    }
                }
            }
        }
    }
}