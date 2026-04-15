package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════
// CATEGORÍAS — lista de categorías disponibles para gastos
// ═══════════════════════════════════════════════════════════
data class CategoriaGastoRapido(
    val nombre: String,
    val categoriaBackend: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector
)

val categoriasGastoRapido = listOf(
    CategoriaGastoRapido("Súper",      "supermercado",   Icons.Rounded.ShoppingCart),
    CategoriaGastoRapido("Transporte", "transporte",     Icons.Rounded.DirectionsBus),
    CategoriaGastoRapido("Salidas",    "comida/salidas", Icons.Rounded.Restaurant),
    CategoriaGastoRapido("Servicios",  "servicios",      Icons.Rounded.PhoneAndroid),
    CategoriaGastoRapido("Salud",      "salud",          Icons.Rounded.LocalPharmacy),
    CategoriaGastoRapido("Varios",     "varios",         Icons.Rounded.FolderOpen),
)

// ═══════════════════════════════════════════════════════════
// GASTO RÁPIDO OVERLAY
// Pantalla completa para registrar un gasto rápido.
// Entra desde la derecha (AnimatedVisibility en MainScreen).
//
// Responsive: ajusta tamaños según alto de pantalla.
// Moto E22 (~595dp) → pantallaChica = true → tamaños reducidos.
// ═══════════════════════════════════════════════════════════
@Composable
fun GastoRapidoOverlay(
    sinIngreso: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmar: (String, Double) -> Unit
) {
    var monto      by remember { mutableStateOf("") }
    var cat        by remember { mutableStateOf<CategoriaGastoRapido?>(null) }
    var confirmado by remember { mutableStateOf(false) }
    val listo = monto.isNotEmpty() && cat != null
    BackHandler { onDismiss() }

    // ── Pantalla de confirmación ──────────────────────────────
    if (confirmado) {
        Box(Modifier.fillMaxSize().background(FondoNegro), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = VerdePrimario, modifier = Modifier.size(90.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, null, tint = FondoNegro, modifier = Modifier.size(44.dp))
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("¡Registrado!", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = TextoPrimario)
                Spacer(Modifier.height(6.dp))
                Text("$${fmtGR(monto)}", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = VerdePrimario)
                Spacer(Modifier.height(4.dp))
                Text(cat!!.nombre, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
        }
        LaunchedEffect(Unit) { delay(1500); onConfirmar(cat!!.categoriaBackend, monto.toDouble()) }
        return
    }

    // ── Responsive ────────────────────────────────────────────
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(FondoNegro)
    ) {
        val pantallaChica = maxHeight < 700.dp
        val burbujaTam  = if (pantallaChica) 72.dp  else 96.dp
        val tecladoAlto = if (pantallaChica) 44.dp  else 54.dp
        val headerAlto  = if (pantallaChica) 80.dp  else 120.dp
        val espaciado   = if (pantallaChica) 4.dp   else 10.dp
        val paddingVert = if (pantallaChica) 4.dp   else 8.dp

        Column(Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = 20.dp,
                    vertical   = if (pantallaChica) 8.dp else 16.dp
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(id = R.drawable.gasto_express), "Gasto Express",
                    Modifier.height(headerAlto)
                        .padding(top = if (pantallaChica) 4.dp else 10.dp)
                        .offset(x = (-20).dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(75.dp).padding(top = 6.dp)) {
                    Icon(Icons.Rounded.Close, null, tint = TextoPrimario, modifier = Modifier.size(30.dp))
                }
            }

            // ── Step dots ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hA = monto.isNotEmpty(); val hC = cat != null
                StepDot(if (hA || hC) "done" else "active"); Spacer(Modifier.width(5.dp))
                StepDot(if (hA && hC) "done" else if (hA || hC) "active" else ""); Spacer(Modifier.width(5.dp))
                StepDot(if (hA && hC) "active" else ""); Spacer(Modifier.width(8.dp))
                Text(
                    when { hA && hC -> "Deslizá para confirmar"; hA -> "Elegí categoría"; hC -> "Ingresá monto"; else -> "Elegí monto y categoría" },
                    style = MaterialTheme.typography.labelSmall, color = TextoMuted
                )
            }

            // ── Monto ─────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = paddingVert),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ARS $", style = MaterialTheme.typography.bodyMedium, color = TextoMuted)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (monto.isEmpty()) "0" else fmtGR(monto),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (monto.isNotEmpty()) VerdePrimario else TextoMuted
                )
            }

            // ── Categorías ────────────────────────────────────
            Spacer(Modifier.height(espaciado))
            categoriasGastoRapido.chunked(3).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    fila.forEach { c -> BurbujaCategoria(c, cat == c, burbujaTam) { cat = c } }
                }
                Spacer(Modifier.height(espaciado))
            }

            Spacer(Modifier.weight(0.05f))

            // ── Teclado ───────────────────────────────────────
            val teclas = listOf(
                listOf("1","2","3"),
                listOf("4","5","6"),
                listOf("7","8","9"),
                listOf("000","0","⌫")
            )
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                teclas.forEach { fila ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        fila.forEach { btn ->
                            Button(
                                onClick = {
                                    if (btn == "⌫") { if (monto.isNotEmpty()) monto = monto.dropLast(1) }
                                    else if (monto.length < 9) monto += btn
                                },
                                modifier = Modifier.weight(1f).height(tecladoAlto),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (btn == "⌫") RojoGasto.copy(alpha = 0.1f) else Color(0xFF111111),
                                    contentColor   = if (btn == "⌫") RojoGasto else TextoPrimario
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                if (btn == "⌫") Icon(Icons.Rounded.Backspace, null, modifier = Modifier.size(22.dp))
                                else Text(btn, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            SwipeToConfirm(listo, { confirmado = true }, Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.weight(0.15f))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BURBUJA CATEGORÍA
// ═══════════════════════════════════════════════════════════
@Composable
fun BurbujaCategoria(
    c: CategoriaGastoRapido,
    sel: Boolean,
    tamano: androidx.compose.ui.unit.Dp = 96.dp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .then(if (sel) Modifier.background(Color.White.copy(alpha = 0.04f)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Surface(
            shape  = CircleShape,
            color  = if (sel) VerdePrimario.copy(alpha = 0.1f) else Color(0xFF111111),
            border = androidx.compose.foundation.BorderStroke(2.dp, if (sel) VerdePrimario else Divisor),
            modifier = Modifier.size(tamano)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(c.icono, null, tint = if (sel) VerdePrimario else TextoSecundario, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            c.nombre,
            style = MaterialTheme.typography.labelMedium,
            color = if (sel) TextoPrimario else TextoSecundario,
            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ═══════════════════════════════════════════════════════════
// SWIPE TO CONFIRM
// ═══════════════════════════════════════════════════════════
@Composable
fun SwipeToConfirm(enabled: Boolean, onConfirmed: () -> Unit, modifier: Modifier = Modifier) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var trackW  by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val thumbPx = with(density) { 54.dp.toPx() }
    val maxDrag = (trackW - thumbPx - 8f).coerceAtLeast(0f)

    Box(
        modifier.fillMaxWidth().height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(if (enabled) Color(0xFF111111) else Color(0xFF0A0A0A))
            .onSizeChanged { trackW = it.width }
    ) {
        if (maxDrag > 0) Box(
            Modifier.fillMaxHeight()
                .fillMaxWidth((offsetX / maxDrag).coerceIn(0f, 1f))
                .background(VerdePrimario.copy(alpha = 0.15f))
        )
        Text(
            if (enabled) "Deslizá para confirmar  →" else "Completá monto y categoría",
            Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) TextoSecundario else TextoMuted,
            letterSpacing = 0.5.sp
        )
        Box(
            Modifier
                .offset { IntOffset(offsetX.roundToInt() + 4, with(density) { 3.dp.roundToPx() }) }
                .size(54.dp).clip(CircleShape)
                .background(if (enabled) VerdePrimario else VerdePrimario.copy(alpha = 0.3f))
                .then(if (enabled) Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { if (offsetX >= maxDrag * 0.9f) onConfirmed() else offsetX = 0f },
                        onHorizontalDrag = { _, d -> offsetX = (offsetX + d).coerceIn(0f, maxDrag) }
                    )
                } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ArrowForward, null, tint = FondoNegro, modifier = Modifier.size(22.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// STEP DOT + FORMATTER
// ═══════════════════════════════════════════════════════════
@Composable
fun StepDot(state: String) {
    Box(
        Modifier
            .then(if (state == "active") Modifier.width(22.dp).height(6.dp) else Modifier.size(6.dp))
            .clip(RoundedCornerShape(3.dp))
            .background(when (state) {
                "active" -> VerdePrimario
                "done"   -> VerdePrimario.copy(alpha = 0.5f)
                else     -> TextoMuted.copy(alpha = 0.3f)
            })
    )
}

fun fmtGR(v: String): String {
    val n = v.toLongOrNull() ?: return v
    return String.format("%,d", n).replace(",", ".")
}