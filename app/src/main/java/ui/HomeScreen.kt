package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

data class ElementoBounds(
    val top: Float = 0f, val left: Float = 0f,
    val right: Float = 0f, val bottom: Float = 0f
) {
    val centerX get() = (left + right) / 2f
    val centerY get() = (top + bottom) / 2f
    val width    get() = right - left
    val height   get() = bottom - top
    val isEmpty  get() = top == 0f && bottom == 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    claraPaddingBottom: Dp = 16.dp,
    onBoundsCardsChanged: (ElementoBounds) -> Unit = {},
    pasoOnboarding: Int = 2
) {
    val context      = LocalContext.current
    val uiState      by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val ingresoSugerido by viewModel.ingresoSugerido.collectAsState()

    var categoriaEditando by remember { mutableStateOf<CategoriaResumen?>(null) }

    when (uiState) {

        is HomeUiState.Cargando -> {
            Box(Modifier.fillMaxSize().background(FondoNegro), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VerdePrimario)
            }
        }

        is HomeUiState.Error -> {
            val msg = (uiState as HomeUiState.Error).mensaje
            Box(Modifier.fillMaxSize().background(FondoNegro), contentAlignment = Alignment.Center) {
                Text("Error: $msg", color = RojoGasto)
            }
        }

        is HomeUiState.Exito -> {

            val datos   = uiState as HomeUiState.Exito
            val visible by viewModel.balanceVisible.collectAsState()
            val userId  = SupabaseClient.instance.auth.currentUserOrNull()?.id ?: "anonimo"

            var mostrarDialogIngreso by remember { mutableStateOf(false) }

            if (mostrarDialogIngreso) {
                EditarIngresoDialog(
                    ingresoActual = datos.totalIngresos,
                    onGuardar = { nuevoMonto ->
                        mostrarDialogIngreso = false
                        viewModel.editarIngresoBase(nuevoMonto = nuevoMonto, onExito = {}, onError = {})
                    },
                    onDismiss = { mostrarDialogIngreso = false }
                )
            }

            categoriaEditando?.let { cat ->
                val presupuestosOtras = datos.categorias
                    .filter { it.nombre != cat.nombre }
                    .sumOf { it.presupuesto }
                EditarCategoriaDialog(
                    categoria   = cat,
                    totalIngresos = datos.totalIngresos,
                    presupuestosOtrasCategorias = presupuestosOtras,
                    onGuardar = { nuevoMonto ->
                        viewModel.guardarPresupuestoCategoria(cat.nombre, nuevoMonto)
                        categoriaEditando = null
                    },
                    onDismiss = { categoriaEditando = null }
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh    = { viewModel.recargar() },
                modifier     = Modifier.fillMaxSize().background(FondoNegro)
            ) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth().background(FondoNegro)
                            .padding(horizontal = 16.dp)
                            .padding(top = 0.dp, bottom = 40.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Hola, ${datos.nombre}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = TextoPrimario)
                                Text("Tu panorama financiero hoy", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                            Image(painterResource(id = R.drawable.logo_plata_clara), "Plata Clara", Modifier.height(140.dp), contentScale = ContentScale.Fit)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth().padding(horizontal = 16.dp)
                            .background(FondoNegro).padding(horizontal = 14.dp)
                            .padding(top = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeroCard(
                            balance = datos.balance, totalIngresos = datos.totalIngresos,
                            totalGastos = datos.totalGastos, visible = visible,
                            onToggle = { viewModel.toggleBalanceVisible() },
                            ipcUltimo = datos.ipcUltimo, ipcAnterior = datos.ipcAnterior,
                            modifier = Modifier.offset(y = (-40).dp),
                            onClick  = { mostrarDialogIngreso = true }
                        )

                        // ── Banner ingreso recurrente ─────────────────────────
                        // Aparece cuando hay un ingreso del mes anterior pero
                        // aún no se registró ingreso este mes.
                        // El usuario puede confirmar con el mismo monto o modificarlo.
                        ingresoSugerido?.let { monto ->
                            BannerIngresoNuevoMes(
                                monto      = monto,
                                modifier   = Modifier.offset(y = (-28).dp),
                                onConfirmar = {
                                    viewModel.descartarIngresoSugerido()
                                    viewModel.editarIngresoBase(monto, onExito = {}, onError = {})
                                },
                                onCambiar  = {
                                    viewModel.descartarIngresoSugerido()
                                    mostrarDialogIngreso = true
                                }
                            )
                        }

                        if (datos.categorias.isNotEmpty()) {
                            Text(
                                "TU PRESUPUESTO",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSobreCreme, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp).offset(y = (-28).dp)
                            )

                            val prefs = context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
                            val categoriasConCustom = datos.categorias.map { cat ->
                                val custom = prefs.getFloat("presupuesto_${userId}_${cat.nombre}", -1f)
                                if (custom >= 0f) cat.copy(presupuesto = custom.toDouble()) else cat
                            }

                            Column(
                                modifier = Modifier
                                    .offset(y = (-28).dp)
                                    .onGloballyPositioned { coords ->
                                        val b = coords.boundsInRoot()
                                        onBoundsCardsChanged(ElementoBounds(top = b.top, left = b.left, right = b.right, bottom = b.bottom))
                                    },
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categoriasConCustom.chunked(2).forEach { fila ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        fila.forEach { cat ->
                                            CategoriaCard(cat = cat, visible = visible, modifier = Modifier.weight(1f), onClick = { categoriaEditando = cat })
                                        }
                                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        ConsejoCard(datos.consejo, Modifier.offset(y = (-28).dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BANNER INGRESO NUEVO MES
// Aparece al inicio del mes si hay un ingreso base del mes anterior.
// Permite confirmar el mismo monto o modificarlo.
// ═══════════════════════════════════════════════════════════
@Composable
private fun BannerIngresoNuevoMes(
    monto: Double,
    modifier: Modifier = Modifier,
    onConfirmar: () -> Unit,
    onCambiar: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = VerdePrimario.copy(alpha = 0.08f)),
        border   = androidx.compose.foundation.BorderStroke(1.dp, VerdePrimario.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.CalendarMonth, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                Text("Nuevo mes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = VerdePrimario)
            }
            Text(
                "¿Tu ingreso sigue siendo $${fmtAR(monto)}?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoPrimario
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onCambiar,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
                ) { Text("Cambiar") }
                Button(
                    onClick  = onConfirmar,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
                ) { Text("Sí, registrar", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ONBOARDING SPOTLIGHT
// ═══════════════════════════════════════════════════════════
@Composable
fun OnboardingSpotlight(
    paso: Int,
    boundsCards: ElementoBounds,
    claraPaddingBottom: Dp,
    onSiguiente: () -> Unit
) {
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(pass = PointerEventPass.Final)
                            .changes.forEach { it.consume() }
                    }
                }
            }
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.82f))
            when (paso) {
                0 -> {
                    val claraPadPx    = with(density) { claraPaddingBottom.toPx() }
                    val claraRadiusPx = with(density) { 52.dp.toPx() }
                    val centerX       = size.width / 2f
                    val centerY       = size.height - claraPadPx - with(density) { 40.dp.toPx() }
                    drawCircle(color = Color.Transparent, radius = claraRadiusPx, center = Offset(centerX, centerY), blendMode = BlendMode.Clear)
                }
                1 -> {
                    if (!boundsCards.isEmpty) {
                        val pad = with(density) { 8.dp.toPx() }
                        val path = Path().apply {
                            addRoundRect(RoundRect(
                                rect = Rect(boundsCards.left - pad, boundsCards.top - pad, boundsCards.right + pad, boundsCards.bottom + pad),
                                cornerRadius = CornerRadius(with(density) { 18.dp.toPx() })
                            ))
                        }
                        drawPath(path = path, color = Color.Transparent, blendMode = BlendMode.Clear)
                    }
                }
            }
        }

        when (paso) {
            0 -> {
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = claraPaddingBottom + 120.dp)
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape  = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = FondoCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("El botón Clara", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextoPrimario)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Rounded.TouchApp, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                                Text("Tocá para abrir el asistente financiero", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Rounded.PanTool, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                                Text("Mantené presionado para registrar un gasto rápido", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                        }
                    }
                    Button(
                        onClick = onSiguiente,
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
                    ) { Text("Entendido", fontWeight = FontWeight.Bold) }
                }
            }

            1 -> {
                if (!boundsCards.isEmpty) {
                    val cardsBottomDp = with(density) { boundsCards.bottom.toDp() }
                    Column(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = cardsBottomDp + 24.dp)
                            .padding(horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape  = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Tu presupuesto mensual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextoPrimario)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Rounded.TouchApp, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                                    Text("Tocá cualquier categoría para asignarle cuánto querés gastar este mes", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Rounded.BarChart, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                                    Text("La barra te muestra cuánto del presupuesto ya usaste", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                }
                            }
                        }
                        Button(
                            onClick = onSiguiente,
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
                        ) { Text("¡Listo, empecemos!", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// HERO CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun HeroCard(
    balance: Double, totalIngresos: Double, totalGastos: Double,
    visible: Boolean, onToggle: () -> Unit,
    ipcUltimo: String?, ipcAnterior: String?,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(Modifier.fillMaxWidth().clickable { onClick() }.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("DISPONIBLE ESTE MES", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 1.sp)
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
                }
            }
            Text(if (visible) "$${fmtAR(balance)}" else "$ ••••••", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = if (visible) VerdePrimario else TextoMuted)
            if (visible) {
                Spacer(Modifier.height(4.dp))
                Row {
                    Text("Gastaste ", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    Text("$${fmtAR(totalGastos)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RojoGasto)
                    Text(" de ", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    Text("$${fmtAR(totalIngresos)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VerdePrimario)
                }
            }
            if (ipcUltimo != null) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Divisor)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IpcPulseDot()
                        Text("INFLACIÓN", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.5.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(ipcUltimo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = VerdePrimario)
                        if (ipcAnterior != null) Text(ipcAnterior, style = MaterialTheme.typography.labelSmall, color = TextoMuted)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CATEGORÍA CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun CategoriaCard(cat: CategoriaResumen, visible: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard).clickable { onClick() },
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(nombreDisplay(cat.nombre).uppercase(), style = MaterialTheme.typography.labelSmall, color = TextoPrimario, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Icon(iconoPara(cat.nombre), null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            if (cat.presupuesto > 0) {
                if (visible) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("$${fmtAR(cat.disponible)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                        Text(if (cat.excedido) "▼" else "▲", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (cat.excedido) TextoMuted.copy(alpha = 0.5f) else VerdePrimario)
                    }
                    if (cat.excedido) Text("Excediste el presupuesto", style = MaterialTheme.typography.labelSmall, color = RojoGasto, fontSize = 10.sp)
                } else {
                    Text("$ ••••", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoMuted)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Divisor)) {
                    if (visible) Box(Modifier.fillMaxWidth(cat.porcentaje.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(VerdePrimario))
                }
            } else {
                Text("Tocá para asignar", style = MaterialTheme.typography.bodySmall, color = TextoMuted)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CONSEJO DEL DÍA
// ═══════════════════════════════════════════════════════════
@Composable
private fun ConsejoCard(consejo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("CONSEJO DEL DÍA", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(6.dp))
            Row {
                Icon(Icons.Rounded.Lightbulb, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(consejo, style = MaterialTheme.typography.bodyMedium, color = TextoMuted, lineHeight = 20.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// IPC PULSE DOT
// ═══════════════════════════════════════════════════════════
@Composable
private fun IpcPulseDot() {
    val t = rememberInfiniteTransition(label = "ipc")
    val a by t.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse), label = "a")
    Box(Modifier.size(7.dp).clip(CircleShape).background(VerdePrimario.copy(alpha = a)))
}

// ═══════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════
internal fun fmtAR(v: Double): String = String.format("%,.0f", v).replace(",", ".")

internal fun nombreDisplay(cat: String): String = when (cat.lowercase()) {
    "supermercado"   -> "Comida"
    "comida/salidas" -> "Salidas"
    "varios"         -> "Varios"
    else             -> cat.replaceFirstChar { it.uppercase() }
}

internal fun iconoPara(cat: String): ImageVector = when (cat.lowercase()) {
    "supermercado"   -> Icons.Rounded.ShoppingCart
    "transporte"     -> Icons.Rounded.DirectionsBus
    "comida/salidas" -> Icons.Rounded.Restaurant
    "servicios"      -> Icons.Rounded.PhoneAndroid
    "salud"          -> Icons.Rounded.LocalPharmacy
    "varios"         -> Icons.Rounded.FolderOpen
    else             -> Icons.Rounded.MoreHoriz
}

// ═══════════════════════════════════════════════════════════
// EDITAR INGRESO DIALOG
// ═══════════════════════════════════════════════════════════
@Composable
private fun EditarIngresoDialog(ingresoActual: Double, onGuardar: (Double) -> Unit, onDismiss: () -> Unit) {
    var texto by remember { mutableStateOf("") }
    val montoValido = texto.toLongOrNull() != null && (texto.toLongOrNull() ?: 0L) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF111111),
        title = { Text("Ingreso base mensual", fontWeight = FontWeight.Bold, color = TextoPrimario) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Modificá tu sueldo o ingreso principal del mes.", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                OutlinedTextField(
                    value         = texto,
                    onValueChange = { if (it.length <= 10) texto = it.filter { c -> c.isDigit() } },
                    label         = { Text("Monto en ARS") },
                    prefix        = { Text("$ ") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = VerdePrimario,
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedLabelColor    = VerdePrimario,
                        cursorColor          = VerdePrimario,
                        focusedTextColor     = TextoPrimario,
                        unfocusedTextColor   = TextoPrimario
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { texto.toLongOrNull()?.let { onGuardar(it.toDouble()) } }, enabled = montoValido) {
                Text("Guardar", color = if (montoValido) VerdePrimario else TextoMuted, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}