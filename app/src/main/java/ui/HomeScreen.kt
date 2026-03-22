package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.model.VariacionDolarResponse
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {

        is HomeUiState.Cargando -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00B872))
            }
        }

        is HomeUiState.Error -> {
            val mensaje = (uiState as HomeUiState.Error).mensaje
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $mensaje", color = Color(0xFFFF4444))
            }
        }

        is HomeUiState.Exito -> {
            val datos = uiState as HomeUiState.Exito
            val balanceVisible by viewModel.balanceVisible.collectAsState()

            // Column raíz — fondo negro, scroll vertical
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // — HEADER — negro, ocupa todo el ancho
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF000000))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hola, ${datos.nombre}",
                                modifier = Modifier.padding(horizontal = 15.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Tu panorama financiero hoy",
                                modifier = Modifier.padding(horizontal = 15.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF888888)
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.logo_plata_clara),
                            contentDescription = "Plata Clara",
                            modifier = Modifier.height(140.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // — BLOQUE CREMA — negro visible a los costados
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 800.dp)
                        .padding(horizontal = 16.dp)
                        .background(
                            color = Color(0xFFF8F2F2),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(horizontal = 12.dp)
                        .padding(top = 15.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // — HERO CARD — balance real con ojo
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F2F2)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00B872))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // label Superávit / Déficit / Sin movimientos
                            Text(
                                text = when {
                                    datos.balance > 0 -> "Superávit"
                                    datos.balance < 0 -> "Déficit"
                                    else -> "Sin movimientos"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // número o asteriscos
                                Text(
                                    text = if (balanceVisible) {
                                        "$${String.format("%,.0f", datos.balance).replace(",", ".")}"
                                    } else {
                                        "$  ••••••"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        datos.balance > 0 -> Color(0xFF00B872)
                                        datos.balance < 0 -> Color(0xFFFF4444)
                                        else -> Color(0xFF888888)
                                    }
                                )

                                // ícono del ojo
                                IconButton(
                                    onClick = { viewModel.toggleBalanceVisible() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (balanceVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = if (balanceVisible) "Ocultar" else "Mostrar",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // — DÓLAR OFICIAL —
                    datos.dolarOficial?.let { oficial ->
                        IndicadorCard(
                            titulo = "Dólar Oficial",
                            compra = oficial.compra,
                            venta = oficial.venta,
                            variacion = datos.variacionOficial
                        )
                    }

                    // — DÓLAR BLUE —
                    datos.dolarBlue?.let { blue ->
                        IndicadorCard(
                            titulo = "Dólar Blue",
                            compra = blue.compra,
                            venta = blue.venta,
                            variacion = datos.variacionBlue
                        )
                    }

                    // — INFLACIÓN + RIPTE —
                    datos.ipcUltimo?.let { ipc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Inflación",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF888888)
                                    )
                                    Text(
                                        text = ipc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00B872)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "RIPTE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF888888)
                                    )
                                    Text(
                                        text = "Feb 2026: 2.9%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00B872)
                                    )
                                }
                            }
                        }
                    }

                    // — CONSEJO DEL DÍA —
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Consejo del día",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF888888)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 ${datos.consejo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF000000)
                            )
                        }
                    }
                    // — ÚLTIMO AJUSTE DE ALQUILER —
                    datos.ultimoAlquiler?.let { alquiler ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Text(
                                    text = "Último ajuste de alquiler",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF888888)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // — montos inicial → ajustado —
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alquiler.tipo_indice.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00B872)
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", alquiler.alquiler_inicial).replace(",", ".")} → $${String.format("%,.0f", alquiler.alquiler_final).replace(",", ".")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // — próximo ajuste —
                                datos.diasParaAjuste?.let { dias ->
                                    Text(
                                        text = when {
                                            dias < 0 -> "⚠️ Ajuste vencido hace ${-dias} días"
                                            dias == 0L -> "⚠️ El ajuste vence hoy"
                                            dias <= 15 -> "🔔 Próximo ajuste en $dias días"
                                            else -> "📅 Próximo ajuste en $dias días"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            dias <= 0 -> Color(0xFFFF0034)
                                            dias <= 15 -> Color(0xFFFF8800)
                                            else -> Color(0xFF888888)
                                        }
                                    )
                                }
                            }
                        }
                    }

                } // cierra Column crema
            } // cierra Column raíz
        } // cierra rama Exito
    } // cierra when
} // cierra HomeScreen


// Componente reutilizable para cards de dólar
// Lo separamos porque se repite — DRY (Don't Repeat Yourself)
@Composable
fun IndicadorCard(
    modifier: Modifier = Modifier,
    titulo: String,
    compra: Double,
    venta: Double,
    variacion: VariacionDolarResponse? = null
) {
    val (colorVariacion, flecha) = when {
        variacion == null -> Pair(Color(0xFF888888), "")
        variacion.variacion_porcentual > 0 -> Pair(Color(0xFF00B872), "▲")
        variacion.variacion_porcentual < 0 -> Pair(Color(0xFFFF4444), "▼")
        else -> Pair(Color(0xFF888888), "→")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )
                if (variacion != null) {
                    Text(
                        text = "$flecha ${String.format("%.1f", variacion.variacion_porcentual)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorVariacion
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Compra
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Compra",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = "$${String.format("%.0f", compra)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                    if (variacion != null) {
                        Text(
                            text = "ant: $${String.format("%.0f", variacion.compra_anterior)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorVariacion
                        )
                    }
                }

                // Divisor vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(Color(0xFFEEEEEE))
                )

                // Venta
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Venta",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = "$${String.format("%.0f", venta)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
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