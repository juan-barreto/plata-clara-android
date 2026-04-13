package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class PuntoGrafico(
    val venta: Double,
    val compra: Double,
    val horaLocal: String,
    val fechaLocal: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DolarDetalleScreen(casa: String, nombre: String, navController: NavController) {
    val viewModel: DolarDetalleViewModel = viewModel(factory = DolarDetalleViewModel.factory(casa))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = FondoNegro,
                    titleContentColor     = TextoPrimario,
                    navigationIconContentColor = TextoPrimario
                )
            )
        },
        containerColor = FondoNegro
    ) { innerPadding ->

        when (uiState) {
            is DolarDetalleUiState.Cargando -> {
                Box(Modifier.fillMaxSize().padding(innerPadding).background(FondoNegro), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VerdePrimario)
                }
            }

            is DolarDetalleUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding).background(FondoNegro), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sin historial suficiente aún", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario)
                        Text("Volvé en 30 minutos", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    }
                }
            }

            is DolarDetalleUiState.Exito -> {
                val historial = (uiState as DolarDetalleUiState.Exito).historial

                val puntos = remember(historial) {
                    val fmtIn    = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
                    val fmtHora  = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
                    val fmtFecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
                    historial.map { item ->
                        try {
                            val date = fmtIn.parse(item.fecha.take(19))
                            PuntoGrafico(item.venta, item.compra, date?.let { fmtHora.format(it) } ?: "", date?.let { fmtFecha.format(it) } ?: item.fecha.take(16))
                        } catch (_: Exception) {
                            PuntoGrafico(item.venta, item.compra, "", item.fecha.take(16).replace("T", " "))
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).background(FondoNegro).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Card precio actual ────────────────────────
                    puntos.lastOrNull()?.let { ultimo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = CardDefaults.cardColors(containerColor = FondoCard),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Compra", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                                    Spacer(Modifier.height(4.dp))
                                    Text("$${String.format("%.0f", ultimo.compra)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                                }
                                Spacer(Modifier.width(1.dp).background(Divisor))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Venta", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                                    Spacer(Modifier.height(4.dp))
                                    Text("$${String.format("%.0f", ultimo.venta)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = VerdePrimario)
                                }
                            }
                        }
                    }

                    // ── Gráfico ───────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = FondoCard),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Venta — últimos registros", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                            Spacer(Modifier.height(4.dp))

                            // Variación respecto al primer punto
                            val variacion = if (puntos.size >= 2) puntos.last().venta - puntos.first().venta else 0.0
                            Text(
                                text  = "→ ${if (variacion >= 0) "+" else ""}$${String.format("%.0f", variacion)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (variacion >= 0) VerdePrimario else RojoGasto
                            )
                            Spacer(Modifier.height(12.dp))
                            GraficoTrading(puntos = puntos, colorLinea = VerdePrimario, modifier = Modifier.fillMaxWidth().height(200.dp))
                        }
                    }

                    // ── Últimos registros ─────────────────────────
                    Text("Últimos registros", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextoPrimario)

                    puntos.reversed().forEach { punto ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = CardDefaults.cardColors(containerColor = FondoCard),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(punto.fechaLocal, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                Text("Venta: $${String.format("%.0f", punto.venta)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextoPrimario)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// GRÁFICO TRADING — línea step con Canvas
// Adaptado para tema oscuro: grilla gris oscuro, precio blanco
// ═══════════════════════════════════════════════════════════
@Composable
fun GraficoTrading(puntos: List<PuntoGrafico>, colorLinea: Color, modifier: Modifier = Modifier) {
    if (puntos.size < 2) return

    val ventas  = puntos.map { it.venta }
    val minVenta = ventas.min()
    val maxVenta = ventas.max()
    val rango   = if (maxVenta == minVenta) 10.0 else (maxVenta - minVenta)
    val margen  = rango * 0.15
    val yMin    = minVenta - margen
    val yMax    = maxVenta + margen

    val pasosY    = 4
    val valoresY  = (0..pasosY).map { i -> yMin + (yMax - yMin) * i / pasosY }
    val maxLabelsX = 5
    val pasoX     = if (puntos.size > maxLabelsX) puntos.size / maxLabelsX else 1

    // Colores dark
    val colorGrilla = Color(0xFF2A2A2A)
    val colorTexto  = Color(0xFF666666)

    Canvas(modifier = modifier) {
        val anchoTotal  = size.width
        val altoTotal   = size.height
        val margenIzq   = 60f
        val margenAbajo = 30f
        val margenDer   = 12f
        val margenArriba = 8f
        val anchoGrafico = anchoTotal - margenIzq - margenDer
        val altoGrafico  = altoTotal - margenAbajo - margenArriba

        fun valorAY(valor: Double): Float {
            val proporcion = (valor - yMin) / (yMax - yMin)
            return margenArriba + altoGrafico * (1f - proporcion.toFloat())
        }

        fun indiceAX(indice: Int): Float {
            val proporcion = indice.toFloat() / (puntos.size - 1).toFloat()
            return margenIzq + anchoGrafico * proporcion
        }

        // Grilla
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        valoresY.forEach { valor ->
            val y = valorAY(valor)
            drawLine(colorGrilla, Offset(margenIzq, y), Offset(anchoTotal - margenDer, y), strokeWidth = 1f, pathEffect = dashEffect)
        }

        // Labels eje Y
        val paintY = android.graphics.Paint().apply {
            color = colorTexto.hashCode()
            textSize = 28f; isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        // Usamos color ARGB correcto
        paintY.setColor(android.graphics.Color.argb(255, 102, 102, 102))
        valoresY.forEach { valor ->
            drawContext.canvas.nativeCanvas.drawText("$${String.format("%.0f", valor)}", margenIzq - 8f, valorAY(valor) + 4f, paintY)
        }

        // Línea step
        for (i in 0 until puntos.size - 1) {
            val x1 = indiceAX(i); val x2 = indiceAX(i + 1)
            val y1 = valorAY(puntos[i].venta); val y2 = valorAY(puntos[i + 1].venta)
            drawLine(colorLinea, Offset(x1, y1), Offset(x2, y1), strokeWidth = 3f)
            if (y1 != y2) drawLine(colorLinea, Offset(x2, y1), Offset(x2, y2), strokeWidth = 3f)
        }

        // Punto final
        val ultimoX = indiceAX(puntos.size - 1)
        val ultimoY = valorAY(puntos.last().venta)
        drawCircle(colorLinea, radius = 6f, center = Offset(ultimoX, ultimoY))
        drawCircle(colorLinea.copy(alpha = 0.2f), radius = 12f, center = Offset(ultimoX, ultimoY))

        // Labels eje X
        val paintX = android.graphics.Paint().apply {
            setColor(android.graphics.Color.argb(255, 102, 102, 102))
            textSize = 24f; isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        for (i in puntos.indices) {
            if (i % pasoX == 0 || i == puntos.size - 1) {
                val label = puntos[i].horaLocal
                if (label.isNotEmpty()) drawContext.canvas.nativeCanvas.drawText(label, indiceAX(i), altoTotal - 4f, paintX)
            }
        }

        // Label último precio — blanco en dark mode
        val paintPrecio = android.graphics.Paint().apply {
            setColor(android.graphics.Color.WHITE)
            textSize = 30f; isAntiAlias = true; isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.LEFT
        }
        val textoPrecio = "$${String.format("%.0f", puntos.last().venta)}"
        val anchoTexto  = paintPrecio.measureText(textoPrecio)
        val precioX     = if (ultimoX + 16f + anchoTexto < anchoTotal) ultimoX + 16f else ultimoX - anchoTexto - 16f
        drawContext.canvas.nativeCanvas.drawText(textoPrecio, precioX, ultimoY - 12f, paintPrecio)
    }
}