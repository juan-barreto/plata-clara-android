package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.nativeCanvas
import com.candlelabs.gestionpersonal.model.MovimientoItem
import java.time.LocalDate


// ── COLORES ──────────────────────────────
private val ColorVerde = Color(0xFF16A34A)
private val ColorRojo = Color(0xFFDC2626)
private val ColorVerdeClaro = Color(0xFFDCFCE7)
private val ColorRojoClaro = Color(0xFFFEE2E2)
private val ColorGris = Color(0xFFE5E7EB)

// ── GRÁFICO 1 — BALANCE ACUMULADO ────────
@Composable
fun GraficoBalanceAcumulado(movimientos: List<MovimientoItem>) {

    // Ordenamos los movimientos por fecha — del más viejo al más nuevo
    // Equivalente en Python: sorted(movimientos, key=lambda m: m['fecha'])
    val ordenados = remember(movimientos) {
        movimientos.sortedBy { it.fecha }
    }

    // Calculamos el balance acumulado punto a punto
    // Equivalente en Python:
    // balance = 0
    // puntos = []
    // for m in ordenados:
    //     balance += m['monto'] if m['tipo'] == 'ingreso' else -m['monto']
    //     puntos.append(balance)
    val puntos = remember(ordenados) {
        var balance = 0.0
        ordenados.map { movimiento ->
            balance += if (movimiento.tipo == "ingreso") movimiento.monto else -movimiento.monto
            balance
        }
    }

    if (puntos.isEmpty()) {
        Text(
            text = "Sin datos para mostrar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // Valores mínimo y máximo para escalar el gráfico
    val maxValor = puntos.max()
    val minValor = puntos.min()
    val rango = if (maxValor == minValor) 1.0 else maxValor - minValor

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Balance acumulado",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp)
        ) {
            val ancho = size.width
            val alto = size.height
            val paddingVertical = 20f
            val altoUtil = alto - paddingVertical * 2

            // Línea de cero — referencia visual
            // Y del cero: si el mínimo es negativo, el cero está proporcionalmente arriba
            val yCero = if (minValor >= 0) {
                // todo positivo — cero en el fondo
                alto - paddingVertical
            } else if (maxValor <= 0) {
                // todo negativo — cero arriba
                paddingVertical
            } else {
                // mix — cero proporcional
                paddingVertical + altoUtil * (maxValor / rango).toFloat()
            }

            // Dibujamos la línea de cero
            drawLine(
                color = ColorGris,
                start = Offset(0f, yCero),
                end = Offset(ancho, yCero),
                strokeWidth = 1.dp.toPx()
            )

            // Si hay un solo punto, dibujamos un círculo
            if (puntos.size == 1) {
                val y = paddingVertical + altoUtil * (1f - ((puntos[0] - minValor) / rango).toFloat())
                val color = if (puntos[0] >= 0) ColorVerde else ColorRojo
                drawCircle(color = color, radius = 6.dp.toPx(), center = Offset(ancho / 2, y))
                return@Canvas
            }

            // Calculamos las coordenadas X e Y de cada punto
            // X: distribuido uniformemente a lo largo del ancho
            // Y: escalado entre paddingVertical y alto - paddingVertical
            val coordenadas = puntos.mapIndexed { index, valor ->
                val x = if (puntos.size == 1) ancho / 2
                else index * (ancho / (puntos.size - 1).toFloat())
                val y = paddingVertical + altoUtil * (1f - ((valor - minValor) / rango).toFloat())
                Offset(x, y)
            }

            // Construimos el path (el trazo de la línea)
            // Equivalente en Python: plt.plot(xs, ys)
            val path = Path()
            coordenadas.forEachIndexed { index, punto ->
                if (index == 0) path.moveTo(punto.x, punto.y)
                else path.lineTo(punto.x, punto.y)
            }

            // Color de la línea según el balance final
            val colorLinea = if (puntos.last() >= 0) ColorVerde else ColorRojo

            // Dibujamos la línea principal
            drawPath(
                path = path,
                color = colorLinea,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Dibujamos un círculo en cada punto
            coordenadas.forEachIndexed { index, punto ->
                val colorPunto = if (puntos[index] >= 0) ColorVerde else ColorRojo
                drawCircle(
                    color = colorPunto,
                    radius = 4.dp.toPx(),
                    center = punto
                )
            }

            // Dibujamos el valor final a la derecha
        }

        // Leyenda con valor final
        val balanceFinal = puntos.last()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Inicio",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${if (balanceFinal >= 0) "+" else ""}$${String.format("%,.0f", balanceFinal)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (balanceFinal >= 0) ColorVerde else ColorRojo
            )
        }
    }
}

// ── GRÁFICO 2 — BARRAS POR SEMANA ────────
@Composable
fun GraficoBarrasPorSemana(movimientos: List<MovimientoItem>) {

    // Agrupamos por semana del mes (1, 2, 3, 4)
    // Equivalente en Python:
    // semanas = defaultdict(lambda: {'ingreso': 0, 'gasto': 0})
    // for m in movimientos:
    //     semana = (dia - 1) // 7 + 1
    //     semanas[semana][m['tipo']] += m['monto']
    val porSemana = remember(movimientos) {
        val mapa = mutableMapOf<Int, Pair<Double, Double>>() // semana → (ingresos, gastos)
        movimientos.forEach { mov ->
            try {
                val fecha = LocalDate.parse(mov.fecha.substring(0, 10))
                val semana = ((fecha.dayOfMonth - 1) / 7) + 1
                val actual = mapa.getOrDefault(semana, Pair(0.0, 0.0))
                mapa[semana] = if (mov.tipo == "ingreso") {
                    Pair(actual.first + mov.monto, actual.second)
                } else {
                    Pair(actual.first, actual.second + mov.monto)
                }
            } catch (e: Exception) { }
        }
        mapa.toSortedMap()
    }

    if (porSemana.isEmpty()) {
        Text(
            text = "Sin datos para mostrar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val maxValor = porSemana.values.maxOf { maxOf(it.first, it.second) }

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Por semana",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp)
        ) {
            val ancho = size.width
            val alto = size.height
            val paddingBottom = 24f
            val altoUtil = alto - paddingBottom

            val semanas = porSemana.keys.toList()
            val cantidadSemanas = semanas.size
            // Ancho de cada grupo de 2 barras
            val anchoGrupo = ancho / cantidadSemanas
            // Ancho de cada barra individual — 35% del grupo con espacio entre ellas
            val anchoBarra = anchoGrupo * 0.35f
            val espacioEntreBarras = anchoGrupo * 0.05f

            semanas.forEachIndexed { index, semana ->
                val (ingresos, gastos) = porSemana[semana] ?: return@forEachIndexed
                val xBase = index * anchoGrupo + anchoGrupo * 0.1f

                // Barra de ingresos (verde)
                val alturaIngreso = if (maxValor > 0) (altoUtil * (ingresos / maxValor)).toFloat() else 0f
                drawRect(
                    color = ColorVerde,
                    topLeft = Offset(xBase, altoUtil - alturaIngreso),
                    size = Size(anchoBarra, alturaIngreso)
                )

                // Barra de gastos (roja)
                val alturaGasto = if (maxValor > 0) (altoUtil * (gastos / maxValor)).toFloat() else 0f
                drawRect(
                    color = ColorRojo,
                    topLeft = Offset(xBase + anchoBarra + espacioEntreBarras, altoUtil - alturaGasto),
                    size = Size(anchoBarra, alturaGasto)
                )

                // Etiqueta "Sem X" abajo
                drawContext.canvas.nativeCanvas.drawText(
                    "S$semana",
                    xBase + anchoBarra / 2,
                    alto - 4f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // Leyenda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).padding(end = 4.dp),
                contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawRect(color = ColorVerde)
                }
            }
            Text(text = " Ingresos  ", style = MaterialTheme.typography.labelSmall)
            Box(modifier = Modifier.size(10.dp),
                contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawRect(color = ColorRojo)
                }
            }
            Text(text = " Gastos", style = MaterialTheme.typography.labelSmall)
        }
    }
}