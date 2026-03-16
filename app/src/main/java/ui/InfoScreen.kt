package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InfoScreen() {

    // verticalScroll permite scrollear el contenido si es más largo que la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "¿Cómo funciona?",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Guía para entender el ajuste de alquileres en Argentina según la ley vigente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Sección 1
        InfoCard(
            titulo = "Contratos firmados antes del 17/10/2023",
            subtitulo = "Ley 27.551 — Ley de Alquileres original",
            contenido = "Se ajustan una vez por año usando el ICL (Índice de Contratos de Locación). " +
                    "El ICL combina 50% IPC (inflación) y 50% RIPTE (salarios). " +
                    "Estos contratos se extinguen progresivamente — la mayoría habrá vencido para fines de 2026.",
            color = InfoCardColor.AZUL
        )

        // Sección 2
        InfoCard(
            titulo = "Contratos firmados entre Oct y Dic 2023",
            subtitulo = "Período de transición",
            contenido = "Se ajustan cada 6 meses por ICL. " +
                    "Corresponden al período entre la derogación de la ley anterior y la entrada en vigor del DNU 70/2023.",
            color = InfoCardColor.NARANJA
        )

        // Sección 3
        InfoCard(
            titulo = "Contratos firmados desde el 29/12/2023",
            subtitulo = "DNU 70/2023 — Marco vigente",
            contenido = "Las partes acuerdan libremente el índice (IPC, ICL u otro) y el período de ajuste " +
                    "(trimestral, cuatrimestral, semestral o anual). " +
                    "En la práctica, más del 70% de los contratos nuevos pactan ajuste trimestral o cuatrimestral por IPC.",
            color = InfoCardColor.VERDE
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Índices explicados
        Text(
            text = "Los índices",
            style = MaterialTheme.typography.titleMedium
        )

        IndiceRow(
            nombre = "IPC",
            descripcion = "Índice de Precios al Consumidor — mide la inflación general. Lo publica el INDEC con un mes de delay."
        )
        IndiceRow(
            nombre = "ICL",
            descripcion = "Índice de Contratos de Locación — combina 50% IPC y 50% RIPTE (salarios). Lo publica el BCRA."
        )
        IndiceRow(
            nombre = "RIPTE",
            descripcion = "Remuneración Imponible Promedio de Trabajadores Estables — refleja la evolución de los salarios formales."
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = "Sobre el delay de publicación",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "El INDEC publica el IPC del mes anterior con algunas semanas de demora. " +
                    "Por ejemplo, el IPC de marzo se conoce en abril. " +
                    "Esta app siempre usa el último índice disponible al momento del cálculo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Colores posibles para las cards
enum class InfoCardColor { AZUL, NARANJA, VERDE }

@Composable
fun InfoCard(
    titulo: String,
    subtitulo: String,
    contenido: String,
    color: InfoCardColor
) {
    val colorContenedor = when (color) {
        InfoCardColor.AZUL -> MaterialTheme.colorScheme.primaryContainer
        InfoCardColor.NARANJA -> MaterialTheme.colorScheme.tertiaryContainer
        InfoCardColor.VERDE -> MaterialTheme.colorScheme.secondaryContainer
    }
    val colorTexto = when (color) {
        InfoCardColor.AZUL -> MaterialTheme.colorScheme.onPrimaryContainer
        InfoCardColor.NARANJA -> MaterialTheme.colorScheme.onTertiaryContainer
        InfoCardColor.VERDE -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorContenedor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = colorTexto
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = colorTexto.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = contenido,
                style = MaterialTheme.typography.bodySmall,
                color = colorTexto
            )
        }
    }
}

@Composable
fun IndiceRow(nombre: String, descripcion: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

