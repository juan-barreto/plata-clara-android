package com.candlelabs.gestionpersonal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex

// ═══════════════════════════════════════════════════════════
// MAIN SCREEN
// Composable raíz de la app autenticada.
// Responsabilidades:
//   - NavController + NavHost
//   - Estado compartido de Clara (clariEstado)
//   - MainNavBar + ClaraButton + GastoRapidoOverlay
//   - HomeViewModel scoped aquí para evitar recargas al navegar
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onCerrarSesion: () -> Unit = {}, abrirGastoExpress: Boolean = false) {
    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()
    val context       = LocalContext.current

    // ViewModel scoped a MainScreen — no se recrea al navegar entre pantallas
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))

    // Estado de Clara
    var clariEstado         by remember { mutableStateOf(ClariState.IDLE) }
    var mostrarOverlay      by remember { mutableStateOf(false) }
    var mostrarCirculoVerde by remember { mutableStateOf(false) }
    var mensajePostFeedback by remember { mutableStateOf<String?>(null) }

    val prefs       = remember { context.getSharedPreferences("plata_clara_prefs", android.content.Context.MODE_PRIVATE) }
    var esPrimerUso by remember { mutableStateOf(prefs.getBoolean("hold_primer_uso", true)) }

    // Desaparecer mensaje después de 1 segundo
    LaunchedEffect(mensajePostFeedback) {
        if (mensajePostFeedback != null) { delay(1000); mensajePostFeedback = null }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // Padding de Clara — dinámico según navbar del dispositivo
    val navBarPadding      = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val claraPaddingBottom = (navBarPadding + 4.dp).coerceAtLeast(12.dp)

    // Definición de ítems de navegación
    val itemsIzquierda = listOf(
        ItemNavegacion(Rutas.HOME,  com.candlelabs.gestionpersonal.R.drawable.icon_home,  "Inicio"),
        ItemNavegacion(Rutas.DOLAR, com.candlelabs.gestionpersonal.R.drawable.icon_dolar, "Dólar"),
    )
    val itemsDerecha = listOf(
        ItemNavegacion(Rutas.ALQUILER, com.candlelabs.gestionpersonal.R.drawable.icon_alquiler, "Alquiler"),
    )
    val itemsMas = listOf(
        ItemMas(Rutas.PRESUPUESTO, Icons.Filled.AccountBalanceWallet, "Presupuesto",    "Ingresos y gastos"),
        ItemMas(Rutas.HISTORIAL,   Icons.Filled.History,              "Historial",       "Cálculos de alquiler"),
        ItemMas(Rutas.INFO,        Icons.Filled.Info,                 "¿Cómo funciona?", "Guía de contratos"),
        ItemMas(Rutas.PERFIL,      Icons.Filled.Person,               "Perfil",          "Tu cuenta y configuración"),
    )

    // ── Abrir / cerrar Gasto Express ──────────────────────────
    fun abrirGastoRapido() {
        if (esPrimerUso) { prefs.edit().putBoolean("hold_primer_uso", false).apply(); esPrimerUso = false }
        clariEstado    = ClariState.EXPRESS_OPEN
        mostrarOverlay = true
    }

    fun cerrarGastoRapido() {
        mostrarOverlay = false
        scope.launch { delay(60); clariEstado = ClariState.IDLE }
    }

    LaunchedEffect(abrirGastoExpress) {
        if (abrirGastoExpress) { delay(300); abrirGastoRapido() }
    }

    // ── Layout ────────────────────────────────────────────────
    Box(Modifier.fillMaxSize()) {

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor      = FondoNegro,
            modifier            = Modifier.systemBarsPadding(),

            bottomBar = {
                MainNavBar(
                    navController    = navController,
                    itemsIzquierda   = itemsIzquierda,
                    itemsDerecha     = itemsDerecha,
                    itemsMas         = itemsMas
                )
            }
        ) { innerPadding ->
            NavHost(
                navController,
                Rutas.HOME,
                Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
            ) {
                composable(Rutas.HOME)        { HomeScreen(navController = navController, viewModel = homeViewModel) }
                composable(Rutas.DOLAR)       { DolarScreen(navController = navController) }
                composable(Rutas.ALQUILER)    { AlquilerScreen(navController = navController) }
                composable(Rutas.HISTORIAL)   { HistorialScreen() }
                composable(Rutas.ASISTENTE)   { AsistenteScreen() }
                composable(Rutas.INFO)        { InfoScreen() }
                composable(Rutas.DOLAR_DETALLE) { back ->
                    DolarDetalleScreen(
                        back.arguments?.getString("casa") ?: "",
                        back.arguments?.getString("nombre") ?: "",
                        navController
                    )
                }
                composable(Rutas.PRESUPUESTO) { PresupuestoScreen() }
                composable(Rutas.PERFIL) {
                    PerfilScreen(onCerrarSesion = {
                        scope.launch {
                            try { SupabaseClient.instance.auth.signOut() } catch (_: Exception) {}
                            onCerrarSesion()
                        }
                    })
                }
            }
        }

        // ── Clara + ondas ─────────────────────────────────────
        ClaraButton(
            navController        = navController,
            rutaActual           = rutaActual,
            clariEstado          = clariEstado,
            onClariEstadoChange  = { clariEstado = it },
            onAbrirGastoExpress  = { abrirGastoRapido() },
            mensajePostFeedback  = mensajePostFeedback,
            claraPaddingBottom   = claraPaddingBottom
        )

        // ── Círculo contrayéndose post-confirmación ───────────
        if (mostrarCirculoVerde) {
            CirculoContrayendose(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = claraPaddingBottom)
            )
        }

        // ── Mensaje post-feedback ─────────────────────────────────
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

    // ── Gasto Express — entra desde la derecha ────────────
        AnimatedVisibility(
            visible = mostrarOverlay,
            enter   = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec  = tween(240, easing = FastOutSlowInEasing)
            ),
            exit    = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
        ) {
            GastoRapidoOverlay(
                onDismiss = { cerrarGastoRapido() },
                onConfirmar = { catBackend, monto ->
                    scope.launch {
                        try {
                            RetrofitClient.create(SupabaseClient.instance)
                                .agregarMovimiento(MovimientoRequest("gasto", catBackend, "Gasto rápido", monto))
                        } catch (_: Exception) {}
                    }
                    mensajePostFeedback = "Gasto registrado"
                    clariEstado = ClariState.POST_FEEDBACK
                    cerrarGastoRapido()
                }
            )
        }
    }


// ═══════════════════════════════════════════════════════════
// CÍRCULO CONTRAYÉNDOSE
// Animación de cierre post-gasto: aparece grande y se contrae a Clara.
// ═══════════════════════════════════════════════════════════
@Composable
private fun CirculoContrayendose(modifier: Modifier = Modifier) {
    val scale = remember { Animatable(30f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = modifier
            .size(60.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(CircleShape)
            .background(VerdePrimario)
    )
}
