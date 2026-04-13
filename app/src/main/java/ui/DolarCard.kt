package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.model.DolarResponse
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun DolarCard(cotizacion: DolarResponse, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable {
                navController.navigate(Rutas.dolarDetalleRuta(casa = cotizacion.casa, nombre = cotizacion.nombre))
            },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = FondoCard),
        border    = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {

            // ── Título + link ─────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(cotizacion.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextoPrimario)
                Text("Ver detalle →", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = VerdePrimario)
            }

            Spacer(Modifier.height(6.dp))

            // ── Compra | Venta ────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Compra", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    Text("$${String.format("%.0f", cotizacion.compra)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario)
                }
                Box(Modifier.width(1.dp).height(36.dp).background(Divisor))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Venta", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    Text("$${String.format("%.0f", cotizacion.venta)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Fecha ─────────────────────────────────────────
            Text(cotizacion.fechaActualizacion.replace("T", " ").take(16), style = MaterialTheme.typography.labelSmall, color = TextoMuted)
        }
    }
}