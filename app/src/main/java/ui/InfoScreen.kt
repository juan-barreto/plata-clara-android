package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun InfoScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
            .verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════════════
        // HEADER
        // ══════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.MenuBook, null, tint = VerdePrimario, modifier = Modifier.size(28.dp))
                Column {
                    Text("Guía de alquileres", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text("Cómo funciona el ajuste en Argentina", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }
            }
        }

        // ══════════════════════════════════════════════
        // BLOQUE CREMA
        // ══════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(FondoPrincipal, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── SECCIÓN 1: Antes Oct 2023 ──
            InfoCardPlataClara(
                icono = Icons.Rounded.History,
                titulo = "Antes del 17/10/2023",
                subtitulo = "Ley 27.551 — Ley de Alquileres original",
                contenido = "Se ajustan una vez por año usando el ICL (Índice de Contratos de Locación). El ICL combina 50% IPC (inflación) y 50% RIPTE (salarios). Estos contratos se extinguen progresivamente — la mayoría habrá vencido para fines de 2026.",
                accentColor = Color(0xFF4E9AFF)
            )

            // ── SECCIÓN 2: Transición ──
            InfoCardPlataClara(
                icono = Icons.Rounded.SwapHoriz,
                titulo = "Oct — Dic 2023",
                subtitulo = "Período de transición",
                contenido = "Se ajustan cada 6 meses por ICL. Corresponden al período entre la derogación de la ley anterior y la entrada en vigor del DNU 70/2023.",
                accentColor = Naranja
            )

            // ── SECCIÓN 3: DNU vigente ──
            InfoCardPlataClara(
                icono = Icons.Rounded.Gavel,
                titulo = "Desde el 29/12/2023",
                subtitulo = "DNU 70/2023 — Marco vigente",
                contenido = "Las partes acuerdan libremente el índice (IPC, ICL u otro) y el período de ajuste (trimestral, cuatrimestral, semestral o anual). En la práctica, más del 70% de los contratos nuevos pactan ajuste trimestral o cuatrimestral por IPC.",
                accentColor = VerdePrimario
            )

            // ── LOS ÍNDICES ──
            Text("LOS ÍNDICES", style = MaterialTheme.typography.labelSmall, color = TextoSobreCreme, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

            IndiceCardPlataClara(
                sigla = "IPC",
                nombre = "Índice de Precios al Consumidor",
                descripcion = "Mide la inflación general. Lo publica el INDEC con un mes de delay."
            )
            IndiceCardPlataClara(
                sigla = "ICL",
                nombre = "Índice de Contratos de Locación",
                descripcion = "Combina 50% IPC y 50% RIPTE (salarios). Lo publica el BCRA."
            )
            IndiceCardPlataClara(
                sigla = "RIPTE",
                nombre = "Remuneración Imponible Promedio",
                descripcion = "Refleja la evolución de los salarios formales registrados."
            )

            // ── DELAY ──
            Card(
                Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Schedule, null, tint = Naranja, modifier = Modifier.size(20.dp))
                    Column {
                        Text("Sobre el delay de publicación", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextoPrimario)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "El INDEC publica el IPC del mes anterior con algunas semanas de demora. Por ejemplo, el IPC de marzo se conoce en abril. Esta app siempre usa el último índice disponible al momento del cálculo.",
                            style = MaterialTheme.typography.bodySmall, color = TextoMuted, lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// INFO CARD — estilo Plata Clara
// ═══════════════════════════════════════════════════════════
@Composable
private fun InfoCardPlataClara(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    contenido: String,
    accentColor: Color
) {
    Card(
        Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icono, null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = accentColor)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(contenido, style = MaterialTheme.typography.bodySmall, color = TextoMuted, lineHeight = 18.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ÍNDICE CARD — sigla destacada
// ═══════════════════════════════════════════════════════════
@Composable
private fun IndiceCardPlataClara(sigla: String, nombre: String, descripcion: String) {
    Card(
        Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VerdePrimario.copy(alpha = 0.15f),
                modifier = Modifier.width(52.dp).height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(sigla, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VerdePrimario)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextoPrimario)
                Spacer(Modifier.height(2.dp))
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoMuted, lineHeight = 18.sp)
            }
        }
    }
}