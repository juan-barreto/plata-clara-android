package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun DolarScreen() {

    // Le pide el ViewModel a Android (lo crea si no existe, lo reutiliza si ya existe)
    val viewModel: DolarViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Según el estado actual, dibuja una cosa u otra
    when (uiState) {

        // Estado 1: todavía cargando → mostramos el spinner
        is DolarUiState.Cargando -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // centra el spinner en pantalla
            ) {
                CircularProgressIndicator() // el círculo giratorio de carga
            }
        }

        // Estado 2: llegaron los datos → mostramos la lista
        is DolarUiState.Exito -> {
            // "as" hace un cast: le dice al compilador "sabemos que es Exito, tratalo así"
            val cotizaciones = (uiState as DolarUiState.Exito).cotizaciones

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                // "items" itera la lista — por cada elemento llama a DolarCard
                items(cotizaciones) { cotizacion ->
                    DolarCard(cotizacion = cotizacion)
                }
            }
        }

        // Estado 3: algo falló → mostramos el mensaje de error
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

