package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Estado del dialog de edición de categoría
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
            val datos = uiState as HomeUiState.Exito
            val visible by viewModel.balanceVisible.collectAsState()

            // Dialog de edición de categoría
            categoriaEditando?.let { cat ->
                val presupuestosOtras = datos.categorias
                    .filter { it.nombre != cat.nombre }
                    .sumOf {
                        // Lee el presupuesto personalizado de SharedPrefs o usa el default
                        val prefs = context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
                        val custom = prefs.getFloat("presupuesto_${it.nombre}", -1f)
                        if (custom >= 0f) custom.toDouble() else it.presupuesto
                    }

                EditarCategoriaDialog(
                    categoria = cat,
                    totalIngresos = datos.totalIngresos,
                    presupuestosOtrasCategorias = presupuestosOtras,
                    onGuardar = { nuevoMonto ->
                        // Guardamos el presupuesto personalizado en SharedPreferences
                        context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE).edit()
                            .putFloat("presupuesto_${cat.nombre}", nuevoMonto.toFloat())
                            .apply()
                        categoriaEditando = null
                        // Recargamos para que las cards se actualicen
                        viewModel.recargar()
                    },
                    onDismiss = { categoriaEditando = null }
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.recargar() },
                modifier = Modifier.fillMaxSize().background(FondoNegro)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                ) {
                    // HEADER
                    Box(
                        modifier = Modifier.fillMaxWidth().background(FondoNegro)
                            .padding(horizontal = 16.dp).padding(top = 0.dp, bottom = 40.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Hola, ${datos.nombre}", style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold, color = TextoPrimario)
                                Text("Tu panorama financiero hoy", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            }
                            Image(painter = painterResource(id = R.drawable.logo_plata_clara),
                                contentDescription = "Plata Clara", modifier = Modifier.height(140.dp), contentScale = ContentScale.Fit)
                        }
                    }

                    // BLOQUE CREMA
                    Column(
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 700.dp)
                            .padding(horizontal = 16.dp)
                            .background(FondoPrincipal, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .padding(horizontal = 14.dp).padding(top = 8.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // HERO CARD
                        HeroCard(
                            balance = datos.balance, totalIngresos = datos.totalIngresos,
                            totalGastos = datos.totalGastos, visible = visible,
                            onToggle = { viewModel.toggleBalanceVisible() },
                            ipcUltimo = datos.ipcUltimo, ipcAnterior = datos.ipcAnterior,
                            modifier = Modifier.offset(y = (-40).dp)
                        )

                        // CATEGORÍAS — ahora son tocables
                        if (datos.categorias.isNotEmpty()) {
                            Text("TU PRESUPUESTO", style = MaterialTheme.typography.labelSmall,
                                color = TextoSobreCreme, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp).offset(y = (-28).dp))

                            // Lee presupuestos personalizados de SharedPrefs
                            val prefs = context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
                            val categoriasConCustom = datos.categorias.map { cat ->
                                val custom = prefs.getFloat("presupuesto_${cat.nombre}", -1f)
                                if (custom >= 0f) cat.copy(presupuesto = custom.toDouble()) else cat
                            }

                            categoriasConCustom.chunked(2).forEach { fila ->
                                Row(Modifier.fillMaxWidth().offset(y = (-28).dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    fila.forEach { cat ->
                                        CategoriaCard(
                                            cat = cat, visible = visible,
                                            modifier = Modifier.weight(1f),
                                            onClick = { categoriaEditando = cat }
                                        )
                                    }
                                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        // CONSEJO
                        ConsejoCard(datos.consejo, Modifier.offset(y = (-28).dp))
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("DISPONIBLE ESTE MES", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 1.sp)
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
                }
            }
            Text(if (visible) "$${fmtAR(balance)}" else "$ ••••••", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = if (visible) VerdePrimario else TextoMuted)
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
                Spacer(Modifier.height(14.dp)); HorizontalDivider(color = Divisor); Spacer(Modifier.height(12.dp))
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
// CATEGORÍA CARD — ahora tocable
// ═══════════════════════════════════════════════════════════
@Composable
private fun CategoriaCard(cat: CategoriaResumen, visible: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(nombreDisplay(cat.nombre).uppercase(), style = MaterialTheme.typography.labelSmall,
                    color = TextoPrimario, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Icon(iconoPara(cat.nombre), null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            if (visible) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("$${fmtAR(cat.disponible)}", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text(if (cat.excedido) "▼" else "▲", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (cat.excedido) TextoMuted.copy(alpha = 0.5f) else VerdePrimario)
                }
                if (cat.excedido) {
                    Text("Excediste el presupuesto", style = MaterialTheme.typography.labelSmall,
                        color = RojoGasto, fontSize = 10.sp)
                }
            } else {
                Text("$ ••••", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoMuted)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Divisor)) {
                if (visible) {
                    Box(Modifier.fillMaxWidth(cat.porcentaje.coerceIn(0f, 1f)).fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp)).background(VerdePrimario))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CONSEJO
// ═══════════════════════════════════════════════════════════
@Composable
private fun ConsejoCard(consejo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape = RoundedCornerShape(18.dp),
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

@Composable
private fun IpcPulseDot() {
    val t = rememberInfiniteTransition(label = "ipc")
    val a by t.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse), label = "a")
    Box(Modifier.size(7.dp).clip(CircleShape).background(VerdePrimario.copy(alpha = a)))
}

// Funciones internas — usadas por HomeScreen y EditarCategoriaDialog
internal fun fmtAR(v: Double): String = String.format("%,.0f", v).replace(",", ".")
internal fun nombreDisplay(cat: String): String = when (cat.lowercase()) {
    "supermercado" -> "Comida"; "comida/salidas" -> "Salidas"; "varios" -> "Varios"
    else -> cat.replaceFirstChar { it.uppercase() }
}
internal fun iconoPara(cat: String): ImageVector = when (cat.lowercase()) {
    "supermercado" -> Icons.Rounded.ShoppingCart; "transporte" -> Icons.Rounded.DirectionsBus
    "comida/salidas" -> Icons.Rounded.Restaurant; "servicios" -> Icons.Rounded.PhoneAndroid
    "salud" -> Icons.Rounded.LocalPharmacy; "varios" -> Icons.Rounded.FolderOpen
    else -> Icons.Rounded.MoreHoriz
}