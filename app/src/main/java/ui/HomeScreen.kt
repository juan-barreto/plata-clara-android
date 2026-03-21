package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.model.VariacionDolarResponse

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
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

            // Box raíz — fondo claro en toda la pantalla
            // El header oscuro con esquinas redondeadas abajo "flota" arriba
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB)) // fondo gris clarísimo para todo
            ) {

                // — CONTENIDO — va primero en el Box para quedar DETRÁS del header
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp) // deja espacio para que el header lo tape arriba
                        .verticalScroll(rememberScrollState())
                        .padding(top = 25.dp)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Saludo
                    Text(
                        text = "Hola, ${datos.nombre} 👋",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                            venta = blue.venta,
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

                    // — ÚLTIMO CÁLCULO DE ALQUILER —
                    datos.ultimoAlquiler?.let { alquiler ->

                        Text(
                            text = "Último cálculo de alquiler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                Text(
                                    text = alquiler.tipo_indice.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Inicial",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "$${String.format("%.0f", alquiler.alquiler_inicial)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }

                                    Text(
                                        text = "→",
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Ajustado",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "$${String.format("%.0f", alquiler.alquiler_final)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Calculado: ${alquiler.fecha_calculo.substring(0, 10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // — PRÓXIMO AJUSTE —
                    datos.diasParaAjuste?.let { dias ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    dias < 0 -> MaterialTheme.colorScheme.errorContainer
                                    dias <= 15 -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Próximo ajuste",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when {
                                            dias < 0 -> "Vencido hace ${-dias} días"
                                            dias == 0L -> "¡Hoy!"
                                            else -> "En $dias días"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = when {
                                        dias < 0 -> "⚠️"
                                        dias <= 15 -> "🔔"
                                        else -> "📅"
                                    },
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }

                    // — CONSEJO DEL DÍA —
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Consejo del día",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = datos.consejo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                } // cierra Column contenido

                // — HEADER — va DESPUÉS en el Box para quedar ENCIMA del contenido
                // Esquinas redondeadas solo abajo, bien pronunciadas (36dp)
                // — HEADER — esquinas redondeadas abajo bien pronunciadas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)                    // ← más alto para que la curva tenga espacio
                        .background(
                            color = Color(0xFF1E1E1E),
                            shape = RoundedCornerShape(bottomStart = 92.dp, bottomEnd = 92.dp)
                        )
                    .padding(start = 32.dp),
                    contentAlignment = Alignment.CenterStart   // ← logo centrado horizontal y vertical
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_plata_clara),
                        contentDescription = "Plata Clara",
                        modifier = Modifier.height(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }

            } // cierra Box raíz
        } // cierra rama Exito
    }
}

// Componente reutilizable para cards de dólar
// Lo separamos porque se repite — DRY (Don't Repeat Yourself)
@Composable
fun IndicadorCard(
    titulo: String,
    compra: Double,
    venta: Double,
    variacion: VariacionDolarResponse? = null
) {
    // Determinamos color y flecha según la variación
    val (colorVariacion, flecha) = when {
        variacion == null -> Pair(MaterialTheme.colorScheme.onPrimaryContainer, "")
        variacion.variacion_porcentual > 0 -> Pair(Color(0xFF16A34A), "↑")
        variacion.variacion_porcentual < 0 -> Pair(Color(0xFFDC2626), "↓")
        else -> Pair(MaterialTheme.colorScheme.onPrimaryContainer, "→")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
                    if (variacion != null) {
                        Text(
                            text = "ant: $${String.format("%.0f", variacion.compra_anterior)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorVariacion
                        )
                    }
                }

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