package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DolarScreen() {

    val viewModel: DolarViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {

        is DolarUiState.Cargando -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DolarUiState.Exito -> {
            val cotizaciones = (uiState as DolarUiState.Exito).cotizaciones

            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // Título de la sección
                item {
                    Text(
                        text = "Cotizaciones del día",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 8.dp
                        )
                    )
                }

                items(cotizaciones) { cotizacion ->
                    DolarCard(cotizacion = cotizacion)
                }
            }
        }

        is DolarUiState.Error -> {
            val mensaje = (uiState as DolarUiState.Error).mensaje
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $mensaje")
            }
        }
    }
}