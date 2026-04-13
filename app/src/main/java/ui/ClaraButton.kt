package com.candlelabs.gestionpersonal.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.ui.theme.*
import kotlinx.coroutines.withTimeoutOrNull

// ═══════════════════════════════════════════════════════════
// CLARA BUTTON
// Composable que se dibuja sobre la navbar.
// Internamente usa Box(fillMaxSize) para poder usar .align()
// sin depender del BoxScope del padre.
//
// TAP       → abre/cierra AsistenteScreen
// HOLD 500ms → ondas + ícono GastoExpress + vibra MEDIUM
//              Al soltar → llama onAbrirGastoExpress()
// ═══════════════════════════════════════════════════════════
@Composable
fun ClaraButton(
    navController: NavController,
    rutaActual: String?,
    clariEstado: ClariState,
    onClariEstadoChange: (ClariState) -> Unit,
    onAbrirGastoExpress: () -> Unit,
    mensajePostFeedback: String?,
    claraPaddingBottom: Dp
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val claraScale = remember { Animatable(1f) }

    LaunchedEffect(clariEstado) {
        when (clariEstado) {
            ClariState.IDLE -> {
                while (clariEstado == ClariState.IDLE) {
                    claraScale.animateTo(1.03f, tween(1250, easing = FastOutSlowInEasing))
                    if (clariEstado != ClariState.IDLE) break
                    claraScale.animateTo(1.0f, tween(1250, easing = FastOutSlowInEasing))
                }
            }
            ClariState.TOUCH_DOWN ->
                claraScale.animateTo(0.96f, tween(120, easing = FastOutSlowInEasing))
            ClariState.HOLD ->
                claraScale.animateTo(1.08f, tween(180, easing = FastOutSlowInEasing))
            ClariState.ASISTENTE_OPEN -> {
                claraScale.stop()
                claraScale.animateTo(1.08f, tween(200, easing = FastOutSlowInEasing))
            }
            ClariState.EXPRESS_OPEN ->
                claraScale.animateTo(1.0f, tween(150))
            ClariState.CANCELLED -> {
                claraScale.animateTo(1.05f, tween(100))
                claraScale.animateTo(1.0f, tween(200))
                onClariEstadoChange(ClariState.IDLE)
            }
            ClariState.POST_FEEDBACK -> {
                claraScale.animateTo(1.06f, tween(150))
                claraScale.animateTo(1.0f, tween(200))
            }
            else -> {}
        }
    }

    val claraGlowAlpha by animateFloatAsState(
        targetValue = when (clariEstado) {
            ClariState.IDLE           -> 0.30f
            ClariState.TOUCH_DOWN     -> 0.45f
            ClariState.HOLD           -> 0.90f
            ClariState.ASISTENTE_OPEN -> 0.80f
            ClariState.EXPRESS_OPEN   -> 0.65f
            ClariState.POST_FEEDBACK  -> 0.85f
            else                      -> 0.30f
        },
        animationSpec = tween(200), label = "glow"
    )

    val mostrarIconoGasto = clariEstado in listOf(
        ClariState.HOLD, ClariState.CONFIRMED, ClariState.EXPRESS_OPEN
    )
    val iconoGastoAlpha by animateFloatAsState(if (mostrarIconoGasto) 1f else 0f, tween(130), label = "ig")
    val iconoClaraAlpha by animateFloatAsState(if (!mostrarIconoGasto) 1f else 0f, tween(130), label = "ic")

    // Box fullscreen — nos da BoxScope para poder usar .align()
    Box(Modifier.fillMaxSize()) {

        // ── Ondas circulares durante HOLD ─────────────────────
        if (clariEstado == ClariState.HOLD) {
            val auraTransition = rememberInfiniteTransition(label = "aura")
            val auraPhase by auraTransition.animateFloat(
                initialValue  = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                label = "phase"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val claraBottomPx = with(density) { claraPaddingBottom.toPx() } + with(density) { 40.dp.toPx() }
                val claraCenter   = Offset(size.width / 2f, size.height - claraBottomPx)
                val baseRadius    = 42.dp.toPx()
                val expansion     = 38.dp.toPx()
                repeat(3) { i ->
                    val ringPhase = (auraPhase + i * 0.333f) % 1f
                    drawCircle(
                        color  = Color(0xFF00B872).copy(alpha = (1f - ringPhase) * 0.50f),
                        radius = baseRadius + ringPhase * expansion,
                        center = claraCenter,
                        style  = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // ── Botón Clara ───────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = claraPaddingBottom)
                .size(80.dp)
                .graphicsLayer { scaleX = claraScale.value; scaleY = claraScale.value }
                .pointerInput(rutaActual) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.first { it.changedToDown() }

                            val estadoAntesTap = clariEstado
                            onClariEstadoChange(ClariState.TOUCH_DOWN)
                            vibrar(context, "light")

                            val startTime   = System.currentTimeMillis()
                            var isLongPress = false
                            var released    = false

                            while (!released) {
                                val event = withTimeoutOrNull(500L - (System.currentTimeMillis() - startTime)) {
                                    awaitPointerEvent()
                                }

                                if (event == null) {
                                    if (estadoAntesTap == ClariState.ASISTENTE_OPEN) {
                                        onClariEstadoChange(ClariState.ASISTENTE_OPEN)
                                        released = true
                                    } else {
                                        isLongPress = true
                                        onClariEstadoChange(ClariState.HOLD)
                                        vibrar(context, "medium")

                                        var waiting = true
                                        while (waiting) {
                                            val holdEvent = awaitPointerEvent()
                                            val change = holdEvent.changes.firstOrNull() ?: break
                                            change.consume()
                                            if (!change.pressed) {
                                                waiting  = false
                                                released = true
                                                onClariEstadoChange(ClariState.CONFIRMED)
                                                vibrar(context, "heavy")
                                                onAbrirGastoExpress()
                                            }
                                        }
                                    }
                                } else {
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) {
                                        released = true
                                        if (!isLongPress) {
                                            if (estadoAntesTap == ClariState.ASISTENTE_OPEN) {
                                                onClariEstadoChange(ClariState.IDLE)
                                                navController.popBackStack()
                                            } else {
                                                onClariEstadoChange(ClariState.ASISTENTE_OPEN)
                                                navController.navigate(Rutas.ASISTENTE) { launchSingleTop = true }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(FondoNegro)
                    .border(2.dp, VerdePrimario.copy(alpha = claraGlowAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.simple_clara),
                    contentDescription = "Clara",
                    modifier           = Modifier.size(45.dp).padding(end = 5.dp).graphicsLayer { alpha = iconoClaraAlpha },
                    contentScale       = ContentScale.Fit
                )
                Image(
                    painter            = painterResource(id = R.drawable.gasto_express2),
                    contentDescription = "Gasto Express",
                    modifier           = Modifier.size(70.dp).graphicsLayer { alpha = iconoGastoAlpha },
                    contentScale       = ContentScale.Fit
                )
            }
        }

        // ── Mensaje post-feedback ─────────────────────────────
        mensajePostFeedback?.let {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 115.dp)
                    .zIndex(3f)
                    .background(Color(0xFF111111).copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(16.dp))
                Text(
                    "Gasto registrado",
                    color = VerdePrimario,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}