package com.candlelabs.gestionpersonal.ui

import android.content.Intent
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
import com.candlelabs.gestionpersonal.MainActivity
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.GestionPersonalARGTheme
import com.candlelabs.gestionpersonal.ui.theme.VerdePrimario
import com.candlelabs.gestionpersonal.ui.theme.FondoNegro
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

                // Secuencia: círculo expande → overlay aparece → círculo desaparece
                LaunchedEffect(Unit) {
                    delay(400)
                    mostrarOverlay = true
                    mostrarCirculo = false
                }

                Box(Modifier.fillMaxSize().background(FondoNegro)) {

                    // Overlay de Gasto Express
                    if (mostrarOverlay) {
                        GastoRapidoOverlay(
                            onDismiss = { finish() },
                            onConfirmar = { catBackend, monto ->
                                lifecycleScope.launch {
                                    try {
                                        RetrofitClient.create(SupabaseClient.instance).agregarMovimiento(
                                            MovimientoRequest("gasto", catBackend, "Gasto rápido", monto)
                                        )
                                    } catch (_: Exception) {}
                                    val intent = Intent(this@GastoExpressActivity, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        )
                    }

                    // Círculo verde expandiéndose — igual al de MainScreen
                    if (mostrarCirculo) {
                        CirculoExpandiendose(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp, bottom = 84.dp)
                        )
                    }
                }
            }
        }
    }
}

// Igual al CirculoExpandible de MainScreen
@Composable
private fun CirculoExpandiendose(modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 30f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
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