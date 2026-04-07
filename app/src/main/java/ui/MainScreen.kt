package com.candlelabs.gestionpersonal.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.model.MovimientoRequest
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt
import com.candlelabs.gestionpersonal.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.unit.Dp
// ═══════════════════════════════════════════════════════════
// ESTADOS DE CLARI
// IDLE          → latido suave, ícono C
// TOUCH_DOWN    → compresión leve
// HOLD          → ondas circulares + ícono GastoExpress + vibra
// CONFIRMED     → soltó tras hold → abre GastoExpress
// CANCELLED     → soltó sin hold → rebote
// POST_FEEDBACK → gasto guardado
// ASISTENTE_OPEN → scale+glow fijos en chat
// EXPRESS_OPEN  → dentro de GastoExpress
// ═══════════════════════════════════════════════════════════
enum class ClariState {
    IDLE, TOUCH_DOWN, HOLD, CONFIRMED, CANCELLED,
    POST_FEEDBACK, ASISTENTE_OPEN, EXPRESS_OPEN
}

fun vibrar(context: Context, tipo: String) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(when (tipo) {
                "light"  -> VibrationEffect.createOneShot(25, 80)
                "medium" -> VibrationEffect.createOneShot(45, 160)
                "heavy"  -> VibrationEffect.createOneShot(70, 255)
                else     -> VibrationEffect.createOneShot(25, 80)
            })
        }
    } catch (_: Exception) {}
}

data class ItemNavegacion(val ruta: String, val iconoRes: Int, val etiqueta: String)
data class ItemMas(val ruta: String, val icono: ImageVector, val titulo: String, val subtitulo: String)
data class CategoriaGastoRapido(val nombre: String, val categoriaBackend: String, val icono: ImageVector)

private val categoriasGastoRapido = listOf(
    CategoriaGastoRapido("Súper",      "supermercado",   Icons.Rounded.ShoppingCart),
    CategoriaGastoRapido("Transporte", "transporte",     Icons.Rounded.DirectionsBus),
    CategoriaGastoRapido("Salidas",    "comida/salidas", Icons.Rounded.Restaurant),
    CategoriaGastoRapido("Servicios",  "servicios",      Icons.Rounded.PhoneAndroid),
    CategoriaGastoRapido("Salud",      "salud",          Icons.Rounded.LocalPharmacy),
    CategoriaGastoRapido("Varios",     "varios",         Icons.Rounded.FolderOpen),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onCerrarSesion: () -> Unit = {}, abrirGastoExpress: Boolean = false) {
    val navController  = rememberNavController()
    var mostrarMenuMas by remember { mutableStateOf(false) }
    val scope          = rememberCoroutineScope()
    val context        = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))
    var mostrarOverlay        by remember { mutableStateOf(false) }
    var mostrarCirculoVerde   by remember { mutableStateOf(false) }
    var circuloExpandiendo    by remember { mutableStateOf(false) }
    var clariEstado           by remember { mutableStateOf(ClariState.IDLE) }
    var mensajePostFeedback   by remember { mutableStateOf<String?>(null) }

    val prefs       = remember { context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE) }
    var esPrimerUso by remember { mutableStateOf(prefs.getBoolean("hold_primer_uso", true)) }

    LaunchedEffect(mensajePostFeedback) {
        if (mensajePostFeedback != null) { delay(1000); mensajePostFeedback = null }
    }

    val claraScale = remember { Animatable(1f) }

    LaunchedEffect(clariEstado) {
        when (clariEstado) {
            ClariState.IDLE -> {
                while (clariEstado == ClariState.IDLE) {
                    claraScale.animateTo(1.03f, tween(1250, easing = FastOutSlowInEasing))
                    if (clariEstado != ClariState.IDLE) break
                    claraScale.animateTo(1.0f,  tween(1250, easing = FastOutSlowInEasing))
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
                claraScale.animateTo(1.0f,  tween(200))
                clariEstado = ClariState.IDLE
            }
            ClariState.POST_FEEDBACK -> {
                claraScale.animateTo(1.06f, tween(150))
                claraScale.animateTo(1.0f,  tween(200))
                delay(1000)
                mensajePostFeedback = null
                clariEstado = ClariState.IDLE
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

    // Ícono cambia a GastoExpress en HOLD y EXPRESS_OPEN
    val mostrarIconoGasto = clariEstado in listOf(ClariState.HOLD, ClariState.CONFIRMED, ClariState.EXPRESS_OPEN)
    val iconoGastoAlpha  by animateFloatAsState(if (mostrarIconoGasto) 1f else 0f, tween(130), label = "ig")
    val iconoClaraAlpha  by animateFloatAsState(if (!mostrarIconoGasto) 1f else 0f, tween(130), label = "ic")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val itemsIzquierda = listOf(
        ItemNavegacion(Rutas.HOME,  R.drawable.icon_home,  "Inicio"),
        ItemNavegacion(Rutas.DOLAR, R.drawable.icon_dolar, "Dólar"),
    )
    val itemsDerecha = listOf(
        ItemNavegacion(Rutas.ALQUILER, R.drawable.icon_alquiler, "Alquiler"),
    )
    val itemsMas = listOf(
        ItemMas(Rutas.PRESUPUESTO, Icons.Filled.AccountBalanceWallet, "Presupuesto",    "Ingresos y gastos"),
        ItemMas(Rutas.HISTORIAL,   Icons.Filled.History,              "Historial",       "Cálculos de alquiler"),
        ItemMas(Rutas.INFO,        Icons.Filled.Info,                 "¿Cómo funciona?", "Guía de contratos"),
        ItemMas(Rutas.PERFIL,      Icons.Filled.Person,               "Perfil",          "Tu cuenta y configuración"),
    )

    // Abrir GastoExpress — slide desde derecha
    fun abrirGastoRapido() {
        if (esPrimerUso) { prefs.edit().putBoolean("hold_primer_uso", false).apply(); esPrimerUso = false }
        clariEstado    = ClariState.EXPRESS_OPEN
        mostrarOverlay = true
    }

    // Cerrar GastoExpress — círculo verde aparece grande y se contrae a Clara
    fun cerrarGastoRapido() {
        mostrarOverlay = false
        scope.launch { delay(60); clariEstado = ClariState.IDLE }
    }

    LaunchedEffect(abrirGastoExpress) {
        if (abrirGastoExpress) { delay(300); abrirGastoRapido() }
    }

    if (mostrarMenuMas) {
        ModalBottomSheet(onDismissRequest = { mostrarMenuMas = false }, containerColor = Color(0xFF111111)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Más opciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario, modifier = Modifier.padding(bottom = 8.dp))
                itemsMas.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { navController.navigate(item.ruta) { launchSingleTop = true }; mostrarMenuMas = false }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = VerdeOscuro, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(item.icono, null, tint = VerdePrimario, modifier = Modifier.size(24.dp)) }
                        }
                        Column {
                            Text(item.titulo,    style = MaterialTheme.typography.bodyLarge,  fontWeight = FontWeight.Medium, color = TextoPrimario)
                            Text(item.subtitulo, style = MaterialTheme.typography.bodySmall,  color = TextoSecundario)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
                    }
                    if (item != itemsMas.last()) HorizontalDivider(color = Divisor, modifier = Modifier.padding(horizontal = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    Box(Modifier.fillMaxSize()) {

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor      = FondoNegro,
            modifier            = Modifier.systemBarsPadding(),
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(FondoNegro)
                        .drawBehind {
                            drawRect(Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.07f), Color.Transparent),
                                startY = 0f, endY = 32f
                            ))
                        }
                        .height(45.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        itemsIzquierda.forEach { item ->
                            val sel = rutaActual == item.ruta
                            Column(modifier = Modifier.weight(1f).clickable { navController.navigate(item.ruta) { launchSingleTop = true } },
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(painterResource(item.iconoRes), item.etiqueta, Modifier.size(24.dp).offset(y = 4.dp), tint = if (sel) VerdePrimario else Color(0xFF444444))
                                Spacer(Modifier.height(2.dp))
                                Text(item.etiqueta, style = MaterialTheme.typography.labelSmall, color = if (sel) VerdePrimario else Color(0xFF444444), modifier = Modifier.offset(y = 4.dp))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        itemsDerecha.forEach { item ->
                            val sel = rutaActual == item.ruta
                            Column(modifier = Modifier.weight(1f).clickable { navController.navigate(item.ruta) { launchSingleTop = true } },
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(painterResource(item.iconoRes), item.etiqueta, Modifier.size(24.dp).offset(y = 4.dp), tint = if (sel) VerdePrimario else Color(0xFF444444))
                                Spacer(Modifier.height(2.dp))
                                Text(item.etiqueta, style = MaterialTheme.typography.labelSmall, color = if (sel) VerdePrimario else Color(0xFF444444), modifier = Modifier.offset(y = 4.dp))
                            }
                        }
                        val masSel = rutaActual in listOf(Rutas.PRESUPUESTO, Rutas.HISTORIAL, Rutas.INFO, Rutas.PERFIL)
                        Column(modifier = Modifier.weight(1f).clickable { mostrarMenuMas = true },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(painterResource(R.drawable.icon_mas), "Más", Modifier.size(24.dp).offset(y = 4.dp), tint = if (masSel) VerdePrimario else Color(0xFF444444))
                            Spacer(Modifier.height(2.dp))
                            Text("Más", style = MaterialTheme.typography.labelSmall, color = if (masSel) VerdePrimario else Color(0xFF444444), modifier = Modifier.offset(y = 4.dp))
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, Rutas.HOME, Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
                composable(Rutas.HOME) { HomeScreen(navController = navController, viewModel = homeViewModel) }
                composable(Rutas.DOLAR)       { DolarScreen(navController = navController) }
                composable(Rutas.ALQUILER)    { AlquilerScreen(navController = navController) }
                composable(Rutas.HISTORIAL)   { HistorialScreen() }
                composable(Rutas.ASISTENTE)   { AsistenteScreen() }
                composable(Rutas.INFO)        { InfoScreen() }
                composable(Rutas.DOLAR_DETALLE) { back ->
                    DolarDetalleScreen(back.arguments?.getString("casa") ?: "", back.arguments?.getString("nombre") ?: "", navController)
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
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
        val claraPaddingBottom = (navBarPadding + 4.dp).coerceAtLeast(12.dp)

        // ══════════════════════════════════════════════════════
        // ONDAS CIRCULARES — Canvas fullscreen durante HOLD
        // Tres anillos que se expanden desde Clara.
        // Indican que el hold está activo y algo va a pasar.
        // ══════════════════════════════════════════════════════
        if (clariEstado == ClariState.HOLD) {
            val auraTransition = rememberInfiniteTransition(label = "aura")
            val auraPhase by auraTransition.animateFloat(
                initialValue  = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
                label = "phase"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Centro de Clara — bottom center, 60dp desde el borde inferior
                val claraBottomPx = with(density) { claraPaddingBottom.toPx() } + with(density) { 40.dp.toPx() }
                val claraCenter = Offset(size.width / 2f, size.height - claraBottomPx)
                val baseRadius  = 42.dp.toPx()
                val expansion   = 38.dp.toPx()
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

        // ══════════════════════════════════════════════════════
        // CLARA — botón central
        // TAP       → abre/cierra AsistenteScreen
        // HOLD 500ms → ondas + ícono GastoExpress + vibra MEDIUM
        //              Al soltar → abre GastoExpress
        // ══════════════════════════════════════════════════════

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
                            clariEstado = ClariState.TOUCH_DOWN
                            vibrar(context, "light")

                            val startTime   = System.currentTimeMillis()
                            var isLongPress = false
                            var released    = false

                            while (!released) {
                                val event = withTimeoutOrNull(500L - (System.currentTimeMillis() - startTime)) {
                                    awaitPointerEvent()
                                }

                                if (event == null) {
                                    // ── HOLD confirmado ──────────────────
                                    if (estadoAntesTap == ClariState.ASISTENTE_OPEN) {
                                        // En chat: ignorar hold
                                        clariEstado = ClariState.ASISTENTE_OPEN
                                        released = true
                                    } else {
                                        isLongPress = true
                                        clariEstado = ClariState.HOLD
                                        vibrar(context, "medium")

                                        // Esperar que suelte el dedo
                                        var waiting = true
                                        while (waiting) {
                                            val holdEvent = awaitPointerEvent()
                                            val change = holdEvent.changes.firstOrNull() ?: break
                                            change.consume()
                                            if (!change.pressed) {
                                                waiting  = false
                                                released = true
                                                // ── Soltar → abrir GastoExpress ──
                                                clariEstado = ClariState.CONFIRMED
                                                vibrar(context, "heavy")
                                                abrirGastoRapido()
                                            }
                                        }
                                    }
                                } else {
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) {
                                        released = true
                                        if (!isLongPress) {
                                            // ── TAP ──────────────────────────
                                            if (estadoAntesTap == ClariState.ASISTENTE_OPEN) {
                                                clariEstado = ClariState.IDLE
                                                navController.popBackStack()
                                            } else {
                                                clariEstado = ClariState.ASISTENTE_OPEN
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
                Text("Gasto registrado", color = VerdePrimario,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        // ══════════════════════════════════════════════════════
        // CÍRCULO VERDE CONTRAYÉNDOSE — animación de cierre
        // Aparece grande (cubriendo pantalla) y se contrae
        // hasta el tamaño de Clara, indicando que "vuelve".
        // ══════════════════════════════════════════════════════
        if (mostrarCirculoVerde) {
            CirculoContrayendose(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }

        // ══════════════════════════════════════════════════════
        // GASTO EXPRESS — entra desde la derecha
        // ══════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = mostrarOverlay,
            enter   = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(240, easing = FastOutSlowInEasing)),
            exit    = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing))
        ) {
            GastoRapidoOverlay(
                onDismiss = { cerrarGastoRapido() },
                onConfirmar = { catBackend, monto ->
                    scope.launch {
                        try { RetrofitClient.create(SupabaseClient.instance).agregarMovimiento(MovimientoRequest("gasto", catBackend, "Gasto rápido", monto)) }
                        catch (_: Exception) {}
                    }
                    mensajePostFeedback = "Gasto registrado"
                    clariEstado = ClariState.POST_FEEDBACK
                    cerrarGastoRapido()
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CÍRCULO CONTRAYÉNDOSE
// Empieza a escala 30 (cubre pantalla) y se contrae a 1
// dando la sensación de que GastoExpress "vuelve" a Clara.
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

// ═══════════════════════════════════════════════════════════
// GASTO RÁPIDO
// ═══════════════════════════════════════════════════════════
@Composable
internal fun GastoRapidoOverlay(onDismiss: () -> Unit, onConfirmar: (String, Double) -> Unit) {
    var monto      by remember { mutableStateOf("") }
    var cat        by remember { mutableStateOf<CategoriaGastoRapido?>(null) }
    var confirmado by remember { mutableStateOf(false) }
    val listo = monto.isNotEmpty() && cat != null
    BackHandler { onDismiss() }

    if (confirmado) {
        Box(Modifier.fillMaxSize().background(FondoNegro), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = VerdePrimario, modifier = Modifier.size(90.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Check, null, tint = FondoNegro, modifier = Modifier.size(44.dp)) }
                }
                Spacer(Modifier.height(20.dp))
                Text("¡Registrado!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                Spacer(Modifier.height(6.dp))
                Text("$${fmtGR(monto)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = VerdePrimario)
                Spacer(Modifier.height(4.dp))
                Text(cat!!.nombre, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
        }
        LaunchedEffect(Unit) { delay(1500); onConfirmar(cat!!.categoriaBackend, monto.toDouble()) }
        return
    }

    // ── Responsive: ajusta tamaños según alto de pantalla ────────────────
    // Moto E22 y similares tienen ~595dp útiles → pantallaChica = true
    BoxWithConstraints(Modifier.fillMaxSize().background(FondoNegro)) {
        val pantallaChica = maxHeight < 700.dp
        val burbujaTam  = if (pantallaChica) 72.dp  else 96.dp
        val tecladoAlto = if (pantallaChica) 44.dp  else 54.dp
        val headerAlto  = if (pantallaChica) 80.dp  else 120.dp
        val espaciado   = if (pantallaChica) 4.dp   else 10.dp
        val paddingVert = if (pantallaChica) 4.dp   else 8.dp

        Column(Modifier.fillMaxSize()) {
            // ── Header — logo GastoExpress + X ───────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (pantallaChica) 8.dp else 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(id = R.drawable.gasto_express), "Gasto Express",
                    Modifier.height(headerAlto).padding(top = if (pantallaChica) 4.dp else 10.dp).offset(x = (-20).dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(75.dp).padding(top = 6.dp)) {
                    Icon(Icons.Rounded.Close, null, tint = TextoPrimario, modifier = Modifier.size(30.dp))
                }
            }

            // ── Step dots + texto guía ────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                val hA = monto.isNotEmpty(); val hC = cat != null
                StepDot(if (hA || hC) "done" else "active"); Spacer(Modifier.width(5.dp))
                StepDot(if (hA && hC) "done" else if (hA || hC) "active" else ""); Spacer(Modifier.width(5.dp))
                StepDot(if (hA && hC) "active" else ""); Spacer(Modifier.width(8.dp))
                Text(
                    when { hA && hC -> "Deslizá para confirmar"; hA -> "Elegí categoría"; hC -> "Ingresá monto"; else -> "Elegí monto y categoría" },
                    style = MaterialTheme.typography.labelSmall, color = TextoMuted
                )
            }

            // ── Monto ─────────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = paddingVert), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ARS $", style = MaterialTheme.typography.bodyMedium, color = TextoMuted)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (monto.isEmpty()) "0" else fmtGR(monto),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (monto.isNotEmpty()) VerdePrimario else TextoMuted
                )
            }

            // ── Categorías ────────────────────────────────────────────────
            Spacer(Modifier.height(espaciado))
            categoriasGastoRapido.chunked(3).forEach { fila ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    fila.forEach { c -> BurbujaCategoria(c, cat == c, burbujaTam) { cat = c } }
                }
                Spacer(Modifier.height(espaciado))
            }

            Spacer(Modifier.weight(0.05f))

            // ── Teclado ───────────────────────────────────────────────────
            val teclas = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("000","0","⌫"))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                teclas.forEach { fila ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        fila.forEach { btn ->
                            Button(
                                onClick = { if (btn == "⌫") { if (monto.isNotEmpty()) monto = monto.dropLast(1) } else if (monto.length < 9) monto += btn },
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

@Composable
private fun BurbujaCategoria(c: CategoriaGastoRapido, sel: Boolean, tamano: Dp = 96.dp, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .then(if (sel) Modifier.background(Color.White.copy(alpha = 0.04f)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Surface(
            shape    = CircleShape,
            color    = if (sel) VerdePrimario.copy(alpha = 0.1f) else Color(0xFF111111),
            border   = androidx.compose.foundation.BorderStroke(2.dp, if (sel) VerdePrimario else Divisor),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(c.icono, null, tint = if (sel) VerdePrimario else TextoSecundario, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(c.nombre, style = MaterialTheme.typography.labelMedium, color = if (sel) TextoPrimario else TextoSecundario, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun SwipeToConfirm(enabled: Boolean, onConfirmed: () -> Unit, modifier: Modifier = Modifier) {
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
        if (maxDrag > 0) Box(Modifier.fillMaxHeight().fillMaxWidth((offsetX / maxDrag).coerceIn(0f, 1f)).background(VerdePrimario.copy(alpha = 0.15f)))
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

@Composable
private fun StepDot(state: String) {
    Box(
        Modifier
            .then(if (state == "active") Modifier.width(22.dp).height(6.dp) else Modifier.size(6.dp))
            .clip(RoundedCornerShape(3.dp))
            .background(when (state) { "active" -> VerdePrimario; "done" -> VerdePrimario.copy(alpha = 0.5f); else -> TextoMuted.copy(alpha = 0.3f) })
    )
}

private fun fmtGR(v: String): String {
    val n = v.toLongOrNull() ?: return v
    return String.format("%,d", n).replace(",", ".")
}