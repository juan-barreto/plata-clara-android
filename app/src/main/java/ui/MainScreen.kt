package com.candlelabs.gestionpersonal.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.roundToInt
import com.candlelabs.gestionpersonal.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import androidx.activity.compose.BackHandler

data class ItemNavegacion(val ruta: String, val iconoRes: Int, val etiqueta: String)
data class ItemMas(val ruta: String, val icono: ImageVector, val titulo: String, val subtitulo: String)
data class CategoriaGastoRapido(val nombre: String, val categoriaBackend: String, val icono: ImageVector)

private val categoriasGastoRapido = listOf(
    CategoriaGastoRapido("Súper", "supermercado", Icons.Rounded.ShoppingCart),
    CategoriaGastoRapido("Transporte", "transporte", Icons.Rounded.DirectionsBus),
    CategoriaGastoRapido("Salidas", "comida/salidas", Icons.Rounded.Restaurant),
    CategoriaGastoRapido("Servicios", "servicios", Icons.Rounded.PhoneAndroid),
    CategoriaGastoRapido("Salud", "salud", Icons.Rounded.LocalPharmacy),
    CategoriaGastoRapido("Varios", "varios", Icons.Rounded.FolderOpen),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onCerrarSesion: () -> Unit = {}, abrirGastoExpress: Boolean = false) {
    val navController = rememberNavController()
    var mostrarMenuMas by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var mostrarCirculoVerde by remember { mutableStateOf(false) }
    var mostrarOverlay by remember { mutableStateOf(false) }

    var mostrarFabIcon by remember { mutableStateOf(true) }
    var circuloExpandiendo by remember { mutableStateOf(true) }
    var fabOffsetY by remember { mutableFloatStateOf(0f) }
    val items = listOf(
        ItemNavegacion(Rutas.HOME, R.drawable.icon_home, "Inicio"),
        ItemNavegacion(Rutas.DOLAR, R.drawable.icon_dolar, "Dólar"),
        ItemNavegacion(Rutas.ALQUILER, R.drawable.icon_alquiler, "Alquiler"),
        ItemNavegacion(Rutas.ASISTENTE, R.drawable.icon_clarai, "ClarAI"),
    )
    val itemsMas = listOf(
        ItemMas(Rutas.PRESUPUESTO, Icons.Filled.AccountBalanceWallet, "Presupuesto", "Ingresos y gastos"),
        ItemMas(Rutas.HISTORIAL, Icons.Filled.History, "Historial", "Cálculos de alquiler"),
        ItemMas(Rutas.INFO, Icons.Filled.Info, "¿Cómo funciona?", "Guía de contratos"),
        ItemMas(Rutas.PERFIL, Icons.Filled.Person, "Perfil", "Tu cuenta y configuración"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route
    val mostrarFab = rutaActual != Rutas.PRESUPUESTO

    fun abrirGastoRapido() {
        mostrarFabIcon = false
        circuloExpandiendo = true
        mostrarCirculoVerde = true
        scope.launch {
            delay(400)
            mostrarOverlay = true
            mostrarCirculoVerde = false
        }
    }
    // Si viene del widget, abre Gasto Express automáticamente
    LaunchedEffect(abrirGastoExpress) {
        if (abrirGastoExpress) {
            delay(300)
            abrirGastoRapido()
        }
    }

    fun cerrarGastoRapido() {
        mostrarOverlay = false
        circuloExpandiendo = false
        mostrarCirculoVerde = true
        scope.launch {
            delay(350)
            mostrarCirculoVerde = false
            mostrarFabIcon = true
        }
    }

    // Bottom Sheet "Más"
    if (mostrarMenuMas) {
        ModalBottomSheet(onDismissRequest = { mostrarMenuMas = false }, containerColor = Color(0xFF111111)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Más opciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario, modifier = Modifier.padding(bottom = 8.dp))
                itemsMas.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().clickable { navController.navigate(item.ruta) { launchSingleTop = true }; mostrarMenuMas = false }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = VerdeOscuro, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(imageVector = item.icono, contentDescription = null, tint = VerdePrimario, modifier = Modifier.size(24.dp)) }
                        }
                        Column {
                            Text(item.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = TextoPrimario)
                            Text(item.subtitulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
                    }
                    if (item != itemsMas.last()) HorizontalDivider(color = Divisor, modifier = Modifier.padding(horizontal = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Layout principal
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor = FondoNegro,
            modifier = Modifier.systemBarsPadding(),
            bottomBar = {
                Row(
                    Modifier.fillMaxWidth().height(70.dp).navigationBarsPadding().shadow(16.dp).background(FondoPrincipal).padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val sel = rutaActual == item.ruta
                        Column(Modifier.weight(1f).clickable { navController.navigate(item.ruta) { launchSingleTop = true } }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(painter = painterResource(item.iconoRes), contentDescription = item.etiqueta, modifier = Modifier.size(58.dp), tint = if (sel) VerdePrimario else TextoSobreCreme)
                            Text(item.etiqueta, style = MaterialTheme.typography.labelSmall, modifier = Modifier.offset(y = (-15).dp), color = if (sel) VerdePrimario else TextoSobreCreme)
                        }
                    }
                    val masSel = rutaActual in listOf(Rutas.PRESUPUESTO, Rutas.HISTORIAL, Rutas.INFO, Rutas.PERFIL)
                    Column(Modifier.weight(1f).clickable { mostrarMenuMas = true }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(R.drawable.icon_mas), contentDescription = "Más", modifier = Modifier.size(58.dp), tint = if (masSel) VerdePrimario else TextoSobreCreme)
                        Text("Más", style = MaterialTheme.typography.labelSmall, modifier = Modifier.offset(y = (-15).dp), color = if (masSel) VerdePrimario else TextoSobreCreme)
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, Rutas.HOME, Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
                composable(Rutas.HOME) { HomeScreen(navController = navController) }
                composable(Rutas.DOLAR) { DolarScreen(navController = navController) }
                composable(Rutas.ALQUILER) { AlquilerScreen(navController = navController) }
                composable(Rutas.HISTORIAL) { HistorialScreen() }
                composable(Rutas.ASISTENTE) { AsistenteScreen() }
                composable(Rutas.INFO) { InfoScreen() }
                composable(Rutas.DOLAR_DETALLE) { back ->
                    DolarDetalleScreen(back.arguments?.getString("casa") ?: "", back.arguments?.getString("nombre") ?: "", navController)
                }
                composable(Rutas.PRESUPUESTO) { PresupuestoScreen() }
                composable(Rutas.PERFIL) {
                    PerfilScreen(
                        onCerrarSesion = {
                            scope.launch {
                                try {
                                    SupabaseClient.instance.auth.signOut()
                                } catch (_: Exception) {}
                                onCerrarSesion()
                            }
                        }
                    )
                }
            }
        }

        // FAB Burbuja draggable
        if (mostrarFab && mostrarFabIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 84.dp)
                    .offset { IntOffset(0, fabOffsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            fabOffsetY += dragAmount.y
                        }
                    }
            ) {
                FloatingActionButton(
                    onClick = { abrirGastoRapido() },
                    shape = CircleShape,
                    containerColor = VerdePrimario,
                    contentColor = FondoNegro,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier
                        .size(61.dp)
                        .border(2.dp, FondoNegro, CircleShape)
                        .padding(end = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.x_negra),
                        contentDescription = "Gasto Express",
                        modifier = Modifier.size(45.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Círculo verde expandiéndose/colapsando
        if (mostrarCirculoVerde) {
            CirculoExpandible(
                expandir = circuloExpandiendo,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 84.dp)
                    .offset { IntOffset(0, fabOffsetY.roundToInt()) }
            )
        }

        // Gasto Rápido overlay
        if (mostrarOverlay) {
            GastoRapidoOverlay(
                onDismiss = { cerrarGastoRapido() },
                onConfirmar = { catBackend, monto ->
                    scope.launch {
                        try {
                            RetrofitClient.create(SupabaseClient.instance).agregarMovimiento(
                                MovimientoRequest("gasto", catBackend, "Gasto rápido", monto)
                            )
                        } catch (_: Exception) {}
                    }
                    cerrarGastoRapido()
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CÍRCULO VERDE EXPANDIBLE
// ═══════════════════════════════════════════════════════════
@Composable
private fun CirculoExpandible(expandir: Boolean, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(if (expandir) 1f else 30f) }
    LaunchedEffect(expandir) {
        scale.animateTo(
            targetValue = if (expandir) 30f else 1f,
            animationSpec = tween(if (expandir) 400 else 350, easing = FastOutSlowInEasing)
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

// ═══════════════════════════════════════════════════════════
// GASTO RÁPIDO — pantalla completa
// ═══════════════════════════════════════════════════════════
@Composable
internal fun GastoRapidoOverlay(
    onDismiss: () -> Unit,
    onConfirmar: (String, Double) -> Unit
) {
    var monto by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf<CategoriaGastoRapido?>(null) }
    var confirmado by remember { mutableStateOf(false) }
    val listo = monto.isNotEmpty() && cat != null
    BackHandler { onDismiss() }
    // Pantalla de éxito
    if (confirmado) {
        Box(Modifier.fillMaxSize().background(FondoNegro), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = VerdePrimario, modifier = Modifier.size(90.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = FondoNegro, modifier = Modifier.size(44.dp)) }
                }
                Spacer(Modifier.height(20.dp))
                Text("¡Registrado!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                Spacer(Modifier.height(6.dp))
                Text("$${fmtGR(monto)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = VerdePrimario)
                Spacer(Modifier.height(4.dp))
                Text(cat!!.nombre, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
        }
        LaunchedEffect(Unit) {
            delay(1500)
            onConfirmar(cat!!.categoriaBackend, monto.toDouble())
        }
        return
    }

    // Panel principal
    Column(Modifier.fillMaxSize().background(FondoNegro)) {

        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.gasto_express),
                contentDescription = "Gasto Express",
                modifier = Modifier.height(120.dp)
                    .padding(top = 10.dp)
                    .offset(x = -20.dp),
                contentScale = ContentScale.Fit
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(75.dp).padding(top = 6.dp)) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = null, tint = TextoPrimario, modifier = Modifier.size(30.dp))
            }
        }

        // Steps
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            val hA = monto.isNotEmpty(); val hC = cat != null
            StepDot(if (hA || hC) "done" else "active"); Spacer(Modifier.width(5.dp))
            StepDot(if (hA && hC) "done" else if (hA || hC) "active" else ""); Spacer(Modifier.width(5.dp))
            StepDot(if (hA && hC) "active" else ""); Spacer(Modifier.width(8.dp))
            Text(
                when { hA && hC -> "Deslizá para confirmar"; hA -> "Elegí categoría"; hC -> "Ingresá monto"; else -> "Elegí monto y categoría" },
                style = MaterialTheme.typography.labelSmall, color = TextoMuted
            )
        }

        // Monto
        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ARS $", style = MaterialTheme.typography.bodyMedium, color = TextoMuted)
            Spacer(Modifier.height(2.dp))
            Text(
                if (monto.isEmpty()) "0" else fmtGR(monto),
                style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold,
                color = if (monto.isNotEmpty()) VerdePrimario else TextoMuted
            )
        }

        // Burbujas 3x2 (96dp)
        Spacer(Modifier.height(12.dp))
        categoriasGastoRapido.chunked(3).forEach { fila ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                fila.forEach { c -> BurbujaCategoria(c, cat == c) { cat = c } }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Espacio chico — sube numpad+swipe al centro
        Spacer(Modifier.weight(0.05f))

        // Numpad
        val teclas = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("000","0","⌫"))
        Column(Modifier.padding(horizontal = 14.dp)) {
            teclas.forEach { fila ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    fila.forEach { btn ->
                        Button(
                            onClick = {
                                if (btn == "⌫") { if (monto.isNotEmpty()) monto = monto.dropLast(1) }
                                else if (monto.length < 9) monto += btn
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (btn == "⌫") RojoGasto.copy(alpha = 0.1f) else Color(0xFF111111),
                                contentColor = if (btn == "⌫") RojoGasto else TextoPrimario
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (btn == "⌫") Icon(imageVector = Icons.Rounded.Backspace, contentDescription = null, modifier = Modifier.size(22.dp))
                            else Text(btn, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
            }
        }

        // Espacio chico entre numpad y swipe
        Spacer(Modifier.height(10.dp))

        // Swipe
        SwipeToConfirm(listo, { confirmado = true }, Modifier.padding(horizontal = 20.dp))

        // Espacio restante se va abajo
        Spacer(Modifier.weight(0.15f))
    }
}

// ═══════════════════════════════════════════════════════════
// BURBUJA CATEGORÍA (96dp)
// ═══════════════════════════════════════════════════════════
@Composable
private fun BurbujaCategoria(c: CategoriaGastoRapido, sel: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .then(if (sel) Modifier.background(Color.White.copy(alpha = 0.04f)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (sel) VerdePrimario.copy(alpha = 0.1f) else Color(0xFF111111),
            border = androidx.compose.foundation.BorderStroke(2.dp, if (sel) VerdePrimario else Divisor),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = c.icono, contentDescription = null, tint = if (sel) VerdePrimario else TextoSecundario, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(c.nombre, style = MaterialTheme.typography.labelMedium, color = if (sel) TextoPrimario else TextoSecundario, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
    }
}

// ═══════════════════════════════════════════════════════════
// SWIPE TO CONFIRM
// ═══════════════════════════════════════════════════════════
@Composable
private fun SwipeToConfirm(enabled: Boolean, onConfirmed: () -> Unit, modifier: Modifier = Modifier) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var trackW by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val thumbPx = with(density) { 54.dp.toPx() }
    val maxDrag = (trackW - thumbPx - 8f).coerceAtLeast(0f)

    Box(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(if (enabled) Color(0xFF111111) else Color(0xFF0A0A0A))
            .onSizeChanged { trackW = it.width }
    ) {
        if (maxDrag > 0) {
            Box(Modifier.fillMaxHeight().fillMaxWidth((offsetX / maxDrag).coerceIn(0f, 1f)).background(VerdePrimario.copy(alpha = 0.15f)))
        }
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
                .size(54.dp)
                .clip(CircleShape)
                .background(if (enabled) VerdePrimario else VerdePrimario.copy(alpha = 0.3f))
                .then(
                    if (enabled) Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX >= maxDrag * 0.9f) onConfirmed()
                                else offsetX = 0f
                            },
                            onHorizontalDrag = { _, d -> offsetX = (offsetX + d).coerceIn(0f, maxDrag) }
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, tint = FondoNegro, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun StepDot(state: String) {
    Box(
        Modifier
            .then(if (state == "active") Modifier.width(22.dp).height(6.dp) else Modifier.size(6.dp))
            .clip(RoundedCornerShape(3.dp))
            .background(
                when (state) {
                    "active" -> VerdePrimario
                    "done" -> VerdePrimario.copy(alpha = 0.5f)
                    else -> TextoMuted.copy(alpha = 0.3f)
                }
            )
    )
}

private fun fmtGR(v: String): String {
    val n = v.toLongOrNull() ?: return v
    return String.format("%,d", n).replace(",", ".")
}