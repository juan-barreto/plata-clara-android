package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.candlelabs.gestionpersonal.model.DolarResponse

// Recibe UNA cotización y la dibuja como una Card
@Composable
fun DolarCard(cotizacion: DolarResponse) {

    // Card es el contenedor con sombra y bordes redondeados
    Card(
        modifier = Modifier
            .fillMaxWidth()  // ocupa todo el ancho disponible
            .padding(horizontal = 16.dp, vertical = 6.dp), // margen exterior
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // sombra
    ) {

        // Column apila elementos verticalmente (como un div con flex-direction: column)
        Column(
            modifier = Modifier.padding(16.dp) // margen interior
        ) {

            // Nombre de la cotización (ej: "Dólar Blue")
            Text(
                text = cotizacion.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            // Row pone elementos horizontalmente (como flex-direction: row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // uno a cada extremo
            ) {
                Text(text = "Compra: $${cotizacion.compra}")
                Text(text = "Venta: $${cotizacion.venta}")
            }
        }

    }
}