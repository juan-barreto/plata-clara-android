package com.candlelabs.gestionpersonal.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GastoExpressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ── Verificar si hay ingreso registrado ───────────────
        val prefsGlobal  = getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
        val userId       = prefsGlobal.getString("user_id", null) ?: ""
        val prefsUsuario = getSharedPreferences("plata_clara_$userId", Context.MODE_PRIVATE)
        val balance      = prefsUsuario.getLong("balance_disponible", -1L)
        val sinIngreso   = balance == 0L

        setContent {
            GestionPersonalARGTheme {

                // ── Si no hay ingreso: dialog y cerrar ────────
                if (sinIngreso) {
                    DialogSinIngreso(onTerminar = { finish() })
                    return@GestionPersonalARGTheme
                }

                // ── Flujo normal: overlay de gasto ────────────
                var mostrarCirculo by remember { mutableStateOf(true) }
                var mostrarOverlay by remember { mutableStateOf(false) }
                var expandiendo    by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
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
                                    try {
                                        SupabaseClient.instance.auth.awaitInitialization()
                                        RetrofitClient.create(SupabaseClient.instance).agregarMovimiento(
                                            MovimientoRequest("gasto", catBackend, "Gasto rápido", monto)
                                        )
                                    } catch (_: Exception) {}
                                    mostrarOverlay = false
                                    expandiendo    = false
                                    mostrarCirculo = true
                                    delay(350)
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
                                .padding(bottom = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DIALOG SIN INGRESO
// Se muestra cuando el usuario abre el widget sin ingreso.
// Aparece, espera 2 segundos, se desvanece y cierra la activity.
// ═══════════════════════════════════════════════════════════
@Composable
private fun DialogSinIngreso(onTerminar: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Aparece suavemente
        alpha.animateTo(1f, tween(300))
        // Espera 2 segundos
        delay(2000)
        // Se desvanece elegantemente
        alpha.animateTo(0f, tween(600))
        onTerminar()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(FondoNegro.copy(alpha = 0.6f))
            .graphicsLayer { this.alpha = alpha.value },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard),
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = SombraCard, spotColor = SombraCard)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícono naranja
                Surface(
                    shape    = CircleShape,
                    color    = Naranja.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Warning, null, tint = Naranja, modifier = Modifier.size(28.dp))
                    }
                }
                Text(
                    "Sin ingreso registrado",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = TextoPrimario
                )
                Text(
                    "Registrá tu ingreso del mes antes de cargar gastos.",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = TextoSecundario,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CÍRCULO EXPANDIÉNDOSE
// ═══════════════════════════════════════════════════════════
@Composable
private fun CirculoExpandiendose(
    expandir: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(if (expandir) 1f else 30f) }

    LaunchedEffect(expandir) {
        scale.animateTo(
            targetValue    = if (expandir) 30f else 1f,
            animationSpec  = tween(if (expandir) 250 else 350, easing = FastOutSlowInEasing)
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