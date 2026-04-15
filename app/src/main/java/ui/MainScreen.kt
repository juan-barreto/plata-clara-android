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
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import com.candlelabs.gestionpersonal.R

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

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))

    val userId = try { SupabaseClient.instance.auth.currentUserOrNull()?.id ?: "anonimo" } catch (_: Exception) { "anonimo" }
    val prefsOnboarding = remember { context.getSharedPreferences("plata_clara_prefs_$userId", android.content.Context.MODE_PRIVATE) }
    var pasoOnboarding by remember { mutableStateOf(prefsOnboarding.getInt("onboarding_paso", 0)) }
    var boundsCards    by remember { mutableStateOf(ElementoBounds()) }

    // Estado de Clara
    var clariEstado         by remember { mutableStateOf(ClariState.IDLE) }
    var mostrarOverlay      by remember { mutableStateOf(false) }
    var mostrarCirculoVerde by remember { mutableStateOf(false) }
    var mensajePostFeedback by remember { mutableStateOf<String?>(null) }
    var mostrarDialogSinIngreso by remember { mutableStateOf(false) }

    val prefs       = remember { context.getSharedPreferences("plata_clara_prefs", android.content.Context.MODE_PRIVATE) }
    var esPrimerUso by remember { mutableStateOf(prefs.getBoolean("hold_primer_uso", true)) }

    // ── Estado del home para verificar ingreso ─────────────
    val uiStateHome by homeViewModel.uiState.collectAsState()
    val sinIngreso = (uiStateHome as? HomeUiState.Exito)?.totalIngresos == 0.0

    LaunchedEffect(mensajePostFeedback) {
        if (mensajePostFeedback != null) { delay(1000); mensajePostFeedback = null }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val navBarPadding      = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val claraPaddingBottom = (navBarPadding + 4.dp).coerceAtLeast(12.dp)

    val itemsIzquierda = listOf(
        ItemNavegacion(Rutas.HOME,  com.candlelabs.gestionpersonal.R.drawable.icon_home,  "Inicio"),
        ItemNavegacion(Rutas.DOLAR, com.candlelabs.gestionpersonal.R.drawable.icon_dolar, "Dólar"),
    )
    val itemsDerecha = listOf(
        ItemNavegacion(Rutas.ALQUILER, com.candlelabs.gestionpersonal.R.drawable.icon_alquiler, "Alquiler"),
    )
    val itemsMas = listOf(
        ItemMas(Rutas.MERCADO_PAGO, Icons.Filled.AccountBalance,      "Mercado Pago",       "Importá tus movimientos"),
        ItemMas(Rutas.PRESUPUESTO,  Icons.Filled.AccountBalanceWallet, "Presupuesto",        "Ingresos y gastos",        iconoRes = R.drawable.presupuesto),
        ItemMas(Rutas.HISTORIAL,    Icons.Filled.History,              "Historial Alquiler", "Cálculos de alquiler",     iconoRes = R.drawable.calendario_alquiler),
        ItemMas(Rutas.INFO,         Icons.Filled.Info,                 "¿Cómo funciona?",    "Guía de contratos",        iconoRes = R.drawable.como),
        ItemMas(Rutas.PERFIL,       Icons.Filled.Person,               "Perfil",             "Tu cuenta y configuración",iconoRes = R.drawable.perfil),
    )

    // ── Abrir / cerrar Gasto Express ──────────────────────────
    fun abrirGastoRapido() {
        // Si no hay ingreso registrado, mostrar dialog y bloquear
        if (sinIngreso) {
            mostrarDialogSinIngreso = true
            return
        }
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
                    navController  = navController,
                    itemsIzquierda = itemsIzquierda,
                    itemsDerecha   = itemsDerecha,
                    itemsMas       = itemsMas
                )
            }
        ) { innerPadding ->
            NavHost(
                navController,
                Rutas.HOME,
                Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
            ) {
                composable(Rutas.HOME) {
                    HomeScreen(
                        navController        = navController,
                        viewModel            = homeViewModel,
                        claraPaddingBottom   = claraPaddingBottom,
                        pasoOnboarding       = pasoOnboarding,
                        onBoundsCardsChanged = { boundsCards = it }
                    )
                }
                composable(Rutas.MERCADO_PAGO) { MercadoPagoScreen() }
                composable(Rutas.DOLAR)     { DolarScreen(navController = navController) }
                composable(Rutas.ALQUILER)  { AlquilerScreen(navController = navController) }
                composable(Rutas.HISTORIAL) { HistorialScreen() }
                composable(Rutas.ASISTENTE) { AsistenteScreen(homeViewModel = homeViewModel) }
                composable(Rutas.INFO)      { InfoScreen() }
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
                            val prefsNombre = context.getSharedPreferences("plata_clara_prefs", android.content.Context.MODE_PRIVATE)
                            val prefsOnb = context.getSharedPreferences("plata_clara_prefs_$userId", android.content.Context.MODE_PRIVATE)
                            prefsNombre.edit().clear().apply()
                            prefsOnb.edit().clear().apply()
                        }
                    })
                }
            }
        }

        // ── Clara + ondas ─────────────────────────────────────
        ClaraButton(
            navController       = navController,
            rutaActual          = rutaActual,
            clariEstado         = clariEstado,
            onClariEstadoChange = { clariEstado = it },
            onAbrirGastoExpress = { abrirGastoRapido() },
            mensajePostFeedback = mensajePostFeedback,
            claraPaddingBottom  = claraPaddingBottom
        )

        // ── Círculo contrayéndose post-confirmación ───────────
        if (mostrarCirculoVerde) {
            CirculoContrayendose(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = claraPaddingBottom)
            )
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
                androidx.compose.material3.Text(
                    "Gasto registrado",
                    color = VerdePrimario,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Dialog sin ingreso — aparece y se desvanece solo ──
        if (mostrarDialogSinIngreso) {
            val alpha = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                // Aparece rápido
                alpha.animateTo(1f, tween(300))
                // Espera 2 segundos
                delay(2000)
                // Se desvanece lentamente
                alpha.animateTo(0f, tween(600))
                mostrarDialogSinIngreso = false
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .graphicsLayer { this.alpha = alpha.value },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard),
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = SombraCard, spotColor = SombraCard)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Ícono naranja
                        Surface(
                            shape  = CircleShape,
                            color  = Naranja.copy(alpha = 0.15f),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Warning, null, tint = Naranja, modifier = Modifier.size(30.dp))
                            }
                        }
                        Text(
                            "Sin ingreso registrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrimario,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Registrá tu ingreso del mes desde la pantalla de inicio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Onboarding spotlight ──────────────────────────────
        if (pasoOnboarding < 2) {
            OnboardingSpotlight(
                paso               = pasoOnboarding,
                boundsCards        = boundsCards,
                claraPaddingBottom = claraPaddingBottom,
                onSiguiente        = {
                    val nuevoPaso = pasoOnboarding + 1
                    pasoOnboarding = nuevoPaso
                    prefsOnboarding.edit().putInt("onboarding_paso", nuevoPaso).apply()
                }
            )
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