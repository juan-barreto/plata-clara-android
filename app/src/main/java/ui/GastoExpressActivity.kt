package com.candlelabs.gestionpersonal.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.GestionPersonalARGTheme
import com.candlelabs.gestionpersonal.ui.theme.VerdePrimario
import com.candlelabs.gestionpersonal.ui.theme.FondoNegro
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GastoExpressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            GestionPersonalARGTheme {

                var mostrarCirculo by remember { mutableStateOf(true) }
                var mostrarOverlay by remember { mutableStateOf(false) }
                // true = expandiendo al abrir, false = contrayendo al cerrar
                var expandiendo by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    // Abre: círculo crece y desaparece, aparece overlay
                    delay(400)
                    mostrarOverlay = true
                    mostrarCirculo = false
                }

                Box(Modifier.fillMaxSize().background(FondoNegro)) {

                    if (mostrarOverlay) {
                        GastoRapidoOverlay(
                            onDismiss = { finish() },
                            onConfirmar = { catBackend, monto ->
                                lifecycleScope.launch {
                                    // Guardamos el gasto
                                    try {
                                        SupabaseClient.instance.auth.awaitInitialization()
                                        RetrofitClient.create(SupabaseClient.instance).agregarMovimiento(
                                            MovimientoRequest("gasto", catBackend, "Gasto rápido", monto)
                                        )
                                    } catch (_: Exception) {}

                                    // Animación de cierre — círculo se contrae de vuelta al centro
                                    mostrarOverlay = false
                                    expandiendo = false
                                    mostrarCirculo = true
                                    delay(350) // tiempo que dura la animación de contracción
                                    finish()
                                }
                            }
                        )
                    }

                    if (mostrarCirculo) {
                        CirculoExpandiendose(
                            expandir = expandiendo,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp) // alineado con Clara
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CirculoExpandiendose(
    expandir: Boolean,
    modifier: Modifier = Modifier
) {
    // Si expande: arranca chico y crece. Si contrae: arranca grande y achica.
    val scale = remember { Animatable(if (expandir) 1f else 30f) }

    LaunchedEffect(expandir) {
        scale.animateTo(
            targetValue = if (expandir) 30f else 1f,
            animationSpec = tween(if (expandir) 250 else 350, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .size(60.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(CircleShape)
            .background(VerdePrimario)
    )
}