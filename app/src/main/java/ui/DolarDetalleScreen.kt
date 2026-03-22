package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ─── MODELO INTERNO PARA EL GRÁFICO ────────────────────────────
// Cada punto del gráfico con la fecha ya convertida a hora local
data class PuntoGrafico(
    val venta: Double,
    val compra: Double,
    val horaLocal: String,    // "14:30"
    val fechaLocal: String    // "2026-03-22 14:30"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DolarDetalleScreen(
    casa: String,
    nombre: String,
    navController: NavController
) {
    val viewModel: DolarDetalleViewModel = viewModel(
        factory = DolarDetalleViewModel.factory(casa)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = nombre,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00B872),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        when (uiState) {

            is DolarDetalleUiState.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFF8F2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00B872))
                }
            }

            is DolarDetalleUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFF8F2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sin historial suficiente aún",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = "Volvé en 30 minutos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            is DolarDetalleUiState.Exito -> {
                val historial = (uiState as DolarDetalleUiState.Exito).historial

                // Convertir fechas a hora local del dispositivo
                val puntos = remember(historial) {
                    val fmtIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    fmtIn.timeZone = TimeZone.getTimeZone("UTC")
                    val fmtHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                    fmtHora.timeZone = TimeZone.getDefault()
                    val fmtFecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    fmtFecha.timeZone = TimeZone.getDefault()

                    historial.map { item ->
                        try {
                            val fechaLimpia = item.fecha.take(19)
                            val date = fmtIn.parse(fechaLimpia)
                            PuntoGrafico(
                                venta = item.venta,
                                compra = item.compra,
                                horaLocal = date?.let { fmtHora.format(it) } ?: "",
                                fechaLocal = date?.let { fmtFecha.format(it) } ?: item.fecha.take(16)
                            )
                        } catch (e: Exception) {
                            PuntoGrafico(
                                venta = item.venta,
                                compra = item.compra,
                                horaLocal = "",
                                fechaLocal = item.fecha.take(16).replace("T", " ")
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFF8F2F2))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ─── CARD PRECIO ACTUAL ─────────────────────
                    puntos.lastOrNull()?.let { ultimo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp, Color(0xFF00B872)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Compra",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF888888)
                                    )
                                    Text(
                                        text = "$${String.format("%.0f", ultimo.compra)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(Color(0xFFEEEEEE))
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Venta",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF888888)
                                    )
                                    Text(
                                        text = "$${String.format("%.0f", ultimo.venta)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }
                        }
                    }

                    // ─── GRÁFICO ESTILO TRADING ─────────────────
                    if (puntos.size >= 2) {
                        val primerVenta = puntos.first().venta
                        val ultimaVenta = puntos.last().venta
                        val tendencia = ultimaVenta - primerVenta
                        val colorLinea = if (tendencia >= 0) Color(0xFF00B872) else Color(0xFFD42B2B)

                        // Indicador de variación
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Venta — últimos registros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF000000)
                            )
                            val flecha = if (tendencia > 0) "▲" else if (tendencia < 0) "▼" else "→"
                            val diff = String.format("%.0f", kotlin.math.abs(tendencia))
                            Text(
                                text = "$flecha $$diff",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tendencia != 0.0) colorLinea else Color(0xFF888888)
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            GraficoTrading(
                                puntos = puntos,
                                colorLinea = colorLinea,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 16.dp)
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "El gráfico aparece cuando haya más registros.\nEl scheduler guarda datos cada 30 minutos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF888888),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // ─── ÚLTIMOS REGISTROS ──────────────────────
                    Text(
                        text = "Últimos registros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF000000)
                    )

                    puntos.takeLast(5).reversed().forEach { punto ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = punto.fechaLocal,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF888888)
                                )
                                Text(
                                    text = "Venta: $${String.format("%.0f", punto.venta)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF000000)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── GRÁFICO TRADING CON CANVAS ─────────────────────────────────
// Línea step: se mantiene horizontal y sube/baja cuando cambia el precio
// Eje Y: rango acotado al min-max real con un poco de margen
// Eje X: horas locales del dispositivo
@Composable
fun GraficoTrading(
    puntos: List<PuntoGrafico>,
    colorLinea: Color,
    modifier: Modifier = Modifier
) {
    if (puntos.size < 2) return

    val ventas = puntos.map { it.venta }
    val minVenta = ventas.min()
    val maxVenta = ventas.max()

    // Margen del 2% arriba y abajo para que la línea no toque los bordes
    // Si min == max (precio no cambió), creamos un rango artificial de ±$5
    val rango = if (maxVenta == minVenta) 10.0 else (maxVenta - minVenta)
    val margen = rango * 0.15
    val yMin = minVenta - margen
    val yMax = maxVenta + margen

    // Valores para el eje Y: 5 líneas horizontales
    val pasosY = 4
    val valoresY = (0..pasosY).map { i ->
        yMin + (yMax - yMin) * i / pasosY
    }

    // Labels del eje X: mostramos solo algunos para no saturar
    val maxLabelsX = 5
    val pasoX = if (puntos.size > maxLabelsX) puntos.size / maxLabelsX else 1

    // Colores del texto — los definimos acá porque Canvas no tiene acceso a MaterialTheme
    val colorTexto = Color(0xFF888888)
    val colorGrilla = Color(0xFFF0F0F0)

    Canvas(modifier = modifier) {
        val anchoTotal = size.width
        val altoTotal = size.height

        // Márgenes para los ejes
        val margenIzq = 60f   // espacio para labels del eje Y
        val margenAbajo = 30f // espacio para labels del eje X
        val margenDer = 12f
        val margenArriba = 8f

        // Área de dibujo del gráfico
        val anchoGrafico = anchoTotal - margenIzq - margenDer
        val altoGrafico = altoTotal - margenAbajo - margenArriba

        // Función para convertir valor → posición Y en el canvas
        // Y invertido: valores altos arriba, bajos abajo
        fun valorAY(valor: Double): Float {
            val proporcion = (valor - yMin) / (yMax - yMin)
            return margenArriba + altoGrafico * (1f - proporcion.toFloat())
        }

        // Función para convertir índice → posición X en el canvas
        fun indiceAX(indice: Int): Float {
            val proporcion = indice.toFloat() / (puntos.size - 1).toFloat()
            return margenIzq + anchoGrafico * proporcion
        }

        // ─── GRILLA HORIZONTAL (líneas punteadas) ───────────
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
        valoresY.forEach { valor ->
            val y = valorAY(valor)
            drawLine(
                color = colorGrilla,
                start = Offset(margenIzq, y),
                end = Offset(anchoTotal - margenDer, y),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
        }

        // ─── LABELS EJE Y ───────────────────────────────────
        val paint = android.graphics.Paint().apply {
            color = 0xFF888888.toInt()
            textSize = 28f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        valoresY.forEach { valor ->
            val y = valorAY(valor)
            drawContext.canvas.nativeCanvas.drawText(
                "$${String.format("%.0f", valor)}",
                margenIzq - 8f,
                y + 4f, // centrar verticalmente con la línea
                paint
            )
        }

        // ─── LÍNEA STEP DEL PRECIO ──────────────────────────
        // Estilo "escalón": horizontal hasta el siguiente punto, después sube/baja
        for (i in 0 until puntos.size - 1) {
            val x1 = indiceAX(i)
            val x2 = indiceAX(i + 1)
            val y1 = valorAY(puntos[i].venta)
            val y2 = valorAY(puntos[i + 1].venta)

            // Línea horizontal al mismo precio
            drawLine(
                color = colorLinea,
                start = Offset(x1, y1),
                end = Offset(x2, y1),
                strokeWidth = 3f
            )

            // Línea vertical cuando cambia el precio
            if (y1 != y2) {
                drawLine(
                    color = colorLinea,
                    start = Offset(x2, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f
                )
            }
        }

        // ─── PUNTO EN EL ÚLTIMO VALOR ───────────────────────
        val ultimoX = indiceAX(puntos.size - 1)
        val ultimoY = valorAY(puntos.last().venta)
        drawCircle(
            color = colorLinea,
            radius = 6f,
            center = Offset(ultimoX, ultimoY)
        )
        // Halo exterior
        drawCircle(
            color = colorLinea.copy(alpha = 0.2f),
            radius = 12f,
            center = Offset(ultimoX, ultimoY)
        )

        // ─── LABELS EJE X ───────────────────────────────────
        val paintX = android.graphics.Paint().apply {
            color = 0xFF888888.toInt()
            textSize = 24f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        for (i in puntos.indices) {
            if (i % pasoX == 0 || i == puntos.size - 1) {
                val x = indiceAX(i)
                val label = puntos[i].horaLocal
                if (label.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        altoTotal - 4f,
                        paintX
                    )
                }
            }
        }

        // ─── LABEL DEL ÚLTIMO PRECIO ────────────────────────
        // Muestra el valor actual al lado del punto
        val paintPrecio = android.graphics.Paint().apply {
            color = 0xFF000000.toInt()
            textSize = 30f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.LEFT
            isFakeBoldText = true
        }
        // Solo si hay espacio a la derecha, sino lo ponemos a la izquierda
        val textoPrecio = "$${String.format("%.0f", puntos.last().venta)}"
        val anchoTexto = paintPrecio.measureText(textoPrecio)
        val precioX = if (ultimoX + 16f + anchoTexto < anchoTotal) {
            ultimoX + 16f
        } else {
            ultimoX - anchoTexto - 16f
        }
        drawContext.canvas.nativeCanvas.drawText(
            textoPrecio,
            precioX,
            ultimoY - 12f,
            paintPrecio
        )
    }
}