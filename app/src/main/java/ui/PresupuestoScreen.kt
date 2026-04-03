package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.imePadding
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.model.MovimientoItem
import com.candlelabs.gestionpersonal.ui.theme.*

// ── Categorías de ingreso — sincronizadas con AuthScreen ──
val CATEGORIAS_INGRESO = listOf("Sueldo", "Freelance", "Negocio propio", "Changas", "Jubilación", "Otro ingreso")

// ── Categorías de gasto ──
val CATEGORIAS_GASTO = listOf(
    "Alquiler", "Supermercado", "Transporte", "Servicios", "Comida/Salidas",
    "Salud", "Tecnología", "Educación", "Entretenimiento", "Deudas/Cuotas", "Otro gasto"
)

// ── Iconos Material Design por categoría ──
val ICONOS_CATEGORIA = mapOf(
    "Sueldo" to Icons.Rounded.Work,
    "Freelance" to Icons.Rounded.Laptop,
    "Negocio propio" to Icons.Rounded.Store,
    "Changas" to Icons.Rounded.Build,
    "Jubilación" to Icons.Rounded.SelfImprovement,
    "Otro ingreso" to Icons.Rounded.AttachMoney,
    "Supermercado" to Icons.Rounded.ShoppingCart,
    "Transporte" to Icons.Rounded.DirectionsBus,
    "Servicios" to Icons.Rounded.PhoneAndroid,
    "Comida/Salidas" to Icons.Rounded.Restaurant,
    "Salud" to Icons.Rounded.LocalPharmacy,
    "Tecnología" to Icons.Rounded.Devices,
    "Educación" to Icons.Rounded.School,
    "Entretenimiento" to Icons.Rounded.SportsEsports,
    "Deudas/Cuotas" to Icons.Rounded.CreditCard,
    "Otro gasto" to Icons.Rounded.Category,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestoScreen() {
    val viewModel: PresupuestoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val filtroActual by viewModel.filtro.collectAsState()
    val context = LocalContext.current
    val exportando by viewModel.exportando.collectAsState()
    val mensajeExport by viewModel.mensajeExport.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var movimientoEditando by remember { mutableStateOf<MovimientoItem?>(null) }
    var mostrarConfirmBorrado by remember { mutableStateOf(false) }
    var idParaBorrar by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmReset by remember { mutableStateOf(false) }
    var mostrarMenuExport by remember { mutableStateOf(false) }
    var graficoDeTorta by remember { mutableStateOf(true) }

    val filtros = listOf("semanal", "mensual", "anual")

    // ── Diálogo borrar un movimiento ──
    if (mostrarConfirmBorrado && idParaBorrar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmBorrado = false },
            title = { Text("Borrar movimiento", color = TextoPrimario) },
            text = { Text("Esta acción no se puede deshacer.", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarMovimiento(idParaBorrar!!)
                    mostrarConfirmBorrado = false
                    idParaBorrar = null
                }) { Text("Borrar", color = RojoGasto) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmBorrado = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    // ── Diálogo reset todos ──
    if (mostrarConfirmReset) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmReset = false },
            title = { Text("Resetear movimientos", color = TextoPrimario) },
            text = { Text("Se borrarán TODOS los movimientos del presupuesto. Esta acción no se puede deshacer.", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetMovimientos()
                    mostrarConfirmReset = false
                }) { Text("Borrar todo", color = RojoGasto) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmReset = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    // ── Diálogo exportar ──
    if (mostrarMenuExport) {
        AlertDialog(
            onDismissRequest = { mostrarMenuExport = false },
            title = { Text("Exportar presupuesto", color = TextoPrimario) },
            text = { Text("Formato para el período $filtroActual:", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.exportarExcel(context, filtroActual); mostrarMenuExport = false },
                        enabled = !exportando,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
                    ) {
                        Icon(Icons.Rounded.TableChart, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Excel")
                    }
                    Button(
                        onClick = { viewModel.exportarPdf(context, filtroActual); mostrarMenuExport = false },
                        enabled = !exportando,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarMenuExport = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    // ── Dialog agregar/editar ──
    if (mostrarBottomSheet) {
        Dialog(
            onDismissRequest = { mostrarBottomSheet = false; movimientoEditando = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = FondoCard
            ) {
                FormularioMovimiento(
                    movimientoInicial = movimientoEditando,
                    onGuardar = { tipo, categoria, descripcion, monto ->
                        if (movimientoEditando != null) {
                            viewModel.editarMovimiento(movimientoEditando!!.id, tipo, categoria, descripcion, monto)
                        } else {
                            viewModel.agregarMovimiento(tipo, categoria, descripcion, monto)
                        }
                        mostrarBottomSheet = false
                        movimientoEditando = null
                    },
                    onCancelar = { mostrarBottomSheet = false; movimientoEditando = null }
                )
            }
        }
    }

    // Limpiar mensaje export después de 3 segundos
    mensajeExport?.let { LaunchedEffect(it) { kotlinx.coroutines.delay(3000); viewModel.limpiarMensajeExport() } }

    // ── Contenido principal con Pull to Refresh ──
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.recargar() },
        modifier = Modifier.fillMaxSize().background(FondoPrincipal)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Snackbar export
            mensajeExport?.let { msg ->
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = FondoCard)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                            Text(msg, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                        }
                    }
                }
            }

            // ── HEADER ──
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = VerdePrimario, modifier = Modifier.size(28.dp))
                        Text("Presupuesto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoSobreCreme)
                    }
                    Row {
                        IconButton(onClick = { mostrarConfirmReset = true }) {
                            Icon(Icons.Rounded.RestartAlt, "Resetear", tint = TextoSecundario, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = { mostrarMenuExport = true }) {
                            Icon(Icons.Rounded.FileDownload, "Exportar", tint = VerdePrimario, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = { mostrarBottomSheet = true }) {
                            Icon(Icons.Rounded.Add, "Agregar", tint = VerdePrimario, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // ── FILTROS ──
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filtros.forEach { filtro ->
                        FilterChip(
                            selected = filtroActual == filtro,
                            onClick = { viewModel.cambiarFiltro(filtro) },
                            label = { Text(filtro.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdePrimario,
                                selectedLabelColor = FondoNegro
                            )
                        )
                    }
                }
            }

            when (val estado = uiState) {
                is PresupuestoUiState.Cargando -> {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VerdePrimario)
                        }
                    }
                }
                is PresupuestoUiState.Error -> {
                    item { Text("Error: ${estado.mensaje}", color = RojoGasto) }
                }
                is PresupuestoUiState.Exito -> {
                    val movimientos = estado.movimientos
                    val ingresos = viewModel.calcularTotalIngresos(movimientos)
                    val gastos = viewModel.calcularTotalGastos(movimientos)
                    val balance = viewModel.calcularBalance(movimientos)

                    // ── RESUMEN ──
                    item {
                        Card(
                            Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                                    ResumenItem("INGRESOS", ingresos, VerdePrimario)
                                    ResumenItem("GASTOS", gastos, RojoGasto)
                                    ResumenItem("BALANCE", balance, if (balance >= 0) VerdePrimario else RojoGasto)
                                }
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = Divisor)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        when {
                                            balance > 0 -> Icons.Rounded.CheckCircle
                                            balance == 0.0 -> Icons.Rounded.Warning
                                            else -> Icons.Rounded.ErrorOutline
                                        },
                                        null,
                                        tint = when {
                                            balance > 0 -> VerdePrimario
                                            balance == 0.0 -> Naranja
                                            else -> RojoGasto
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        when {
                                            balance > 0 -> "Vas bien — tenés un superávit"
                                            balance == 0.0 -> "Justo — gastos igualan ingresos"
                                            else -> "Cuidado — estás gastando más de lo que ganás"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoMuted
                                    )
                                }
                            }
                        }
                    }

                    // ── GRÁFICO ──
                    if (movimientos.isNotEmpty()) {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !graficoDeTorta,
                                    onClick = { graficoDeTorta = false },
                                    leadingIcon = { Icon(Icons.Rounded.ShowChart, null, Modifier.size(16.dp)) },
                                    label = { Text("Balance") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VerdePrimario,
                                        selectedLabelColor = FondoNegro,
                                        selectedLeadingIconColor = FondoNegro
                                    )
                                )
                                FilterChip(
                                    selected = graficoDeTorta,
                                    onClick = { graficoDeTorta = true },
                                    leadingIcon = { Icon(Icons.Rounded.BarChart, null, Modifier.size(16.dp)) },
                                    label = { Text("Por semana") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VerdePrimario,
                                        selectedLabelColor = FondoNegro,
                                        selectedLeadingIconColor = FondoNegro
                                    )
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Card(
                                Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = FondoCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                            ) {
                                Box(Modifier.padding(16.dp)) {
                                    if (graficoDeTorta) GraficoBarrasPorSemana(movimientos = movimientos)
                                    else GraficoBalanceAcumulado(movimientos = movimientos)
                                }
                            }
                        }
                    }

                    // ── MOVIMIENTOS ──
                    item {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Movimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoSobreCreme)
                            Text("${movimientos.size} registros", style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                        }
                    }

                    if (movimientos.isEmpty()) {
                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = FondoCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.ReceiptLong, null, tint = TextoMuted, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Sin movimientos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextoPrimario)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Tocá + para agregar uno", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                                }
                            }
                        }
                    } else {
                        items(movimientos, key = { it.id }) { movimiento ->
                            FilaMovimiento(
                                movimiento = movimiento,
                                onEditar = { movimientoEditando = movimiento; mostrarBottomSheet = true },
                                onBorrar = { idParaBorrar = movimiento.id; mostrarConfirmBorrado = true }
                            )
                        }
                    }

                    // ── INFO ──
                    item {
                        Spacer(Modifier.height(4.dp))
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Info, null, tint = VerdePrimario, modifier = Modifier.size(16.dp))
                                Text(
                                    "Registrá todos tus ingresos y gastos para tener una visión clara de tu situación financiera.",
                                    style = MaterialTheme.typography.bodySmall, color = TextoMuted, lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// RESUMEN ITEM — columna de Ingresos/Gastos/Balance
// ═══════════════════════════════════════════════════════════
@Composable
private fun ResumenItem(label: String, valor: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "${if (valor >= 0 && label == "BALANCE") "+" else ""}$${String.format("%,.0f", valor).replace(",", ".")}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ═══════════════════════════════════════════════════════════
// FILA MOVIMIENTO — card con ícono de categoría, info y monto
// ═══════════════════════════════════════════════════════════
@Composable
fun FilaMovimiento(movimiento: MovimientoItem, onEditar: () -> Unit, onBorrar: () -> Unit) {
    val esIngreso = movimiento.tipo == "ingreso"

    // Detecta si el movimiento vino de Gasto Express
    // Gasto Express siempre guarda "Gasto rápido" como descripción
    val esGastoExpress = movimiento.descripcion?.trim() == "Gasto rápido"

    // Ícono de Material Design según la categoría — siempre se muestra en el círculo
    val icono = ICONOS_CATEGORIA[movimiento.categoria] ?: Icons.Rounded.MoreHoriz

    Card(
        Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = SombraCard, spotColor = SombraCard),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Ícono circular — siempre muestra el ícono de la categoría ──
            Surface(
                shape = CircleShape,
                color = if (esIngreso) VerdePrimario.copy(alpha = 0.15f) else TextoSecundario.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icono,
                        contentDescription = movimiento.categoria,
                        tint = if (esIngreso) VerdePrimario else TextoMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── Info — categoría, descripción y fecha ──
            Column(Modifier.weight(1f)) {
                // Nombre de la categoría — siempre visible
                Text(
                    movimiento.categoria.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextoPrimario
                )

                // Descripción — si es Gasto Express muestra el logo PNG,
                // si no muestra el texto normal de la descripción
                if (!movimiento.descripcion.isNullOrBlank()) {
                    if (esGastoExpress) {
                        // PNG del logo de Gasto Express en vez del texto "Gasto rápido"
                        Image(
                            painter = painterResource(id = R.drawable.gasto_express2),
                            contentDescription = "Gasto Express",
                            modifier = Modifier
                                .height(14.dp)
                                .padding(bottom = 2.dp)
                                .wrapContentWidth(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Descripción libre escrita por el usuario
                        Text(
                            movimiento.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }

                // Fecha del movimiento
                Text(
                    movimiento.fecha.substring(0, 10),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoMuted
                )
            }

            // ── Monto — verde si ingreso, rojo si gasto ──
            Text(
                "${if (esIngreso) "+" else "-"}$${String.format("%,.0f", movimiento.monto).replace(",", ".")}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (esIngreso) VerdePrimario else RojoGasto
            )

            // ── Acciones — editar y borrar ──
            Column {
                IconButton(onClick = onEditar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Edit, "Editar", tint = VerdePrimario, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onBorrar, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Delete, "Borrar", tint = TextoMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FORMULARIO — Dialog para agregar/editar movimiento
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMovimiento(
    movimientoInicial: MovimientoItem?,
    onGuardar: (String, String, String, Double) -> Unit,
    onCancelar: () -> Unit
) {
    var tipo by remember { mutableStateOf(movimientoInicial?.tipo ?: "gasto") }
    var categoria by remember { mutableStateOf(movimientoInicial?.categoria ?: "") }
    var descripcion by remember { mutableStateOf(movimientoInicial?.descripcion ?: "") }
    var monto by remember { mutableStateOf(movimientoInicial?.monto?.toString() ?: "") }
    var expandirCategorias by remember { mutableStateOf(false) }

    val categorias = if (tipo == "ingreso") CATEGORIAS_INGRESO else CATEGORIAS_GASTO

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            if (movimientoInicial != null) "Editar movimiento" else "Nuevo movimiento",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrimario
        )

        // Toggle ingreso/gasto
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ingreso", "gasto").forEach { opcion ->
                FilterChip(
                    selected = tipo == opcion,
                    onClick = { tipo = opcion; categoria = "" },
                    leadingIcon = {
                        Icon(
                            if (opcion == "ingreso") Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            null, modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(if (opcion == "ingreso") "Ingreso" else "Gasto") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (opcion == "ingreso") VerdePrimario else RojoGasto,
                        selectedLabelColor = if (opcion == "ingreso") FondoNegro else TextoPrimario,
                        selectedLeadingIconColor = if (opcion == "ingreso") FondoNegro else TextoPrimario
                    )
                )
            }
        }

        // Dropdown de categoría
        ExposedDropdownMenuBox(expanded = expandirCategorias, onExpandedChange = { expandirCategorias = it }) {
            OutlinedTextField(
                value = categoria, onValueChange = {}, readOnly = true,
                label = { Text("Categoría", color = TextoSecundario) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandirCategorias) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                    focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor,
                    cursorColor = VerdePrimario
                ),
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandirCategorias, onDismissRequest = { expandirCategorias = false }) {
                categorias.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        leadingIcon = {
                            Icon(ICONOS_CATEGORIA[cat] ?: Icons.Rounded.MoreHoriz, null, modifier = Modifier.size(18.dp))
                        },
                        onClick = { categoria = cat; expandirCategorias = false }
                    )
                }
            }
        }

        // Campo descripción (opcional)
        OutlinedTextField(
            value = descripcion, onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)", color = TextoSecundario) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor,
                cursorColor = VerdePrimario
            ),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        // Campo monto
        OutlinedTextField(
            value = monto, onValueChange = { monto = it },
            label = { Text("Monto", color = TextoSecundario) },
            prefix = { Text("$", color = VerdePrimario, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor,
                cursorColor = VerdePrimario
            ),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        // Botones cancelar / guardar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCancelar, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
            ) { Text("Cancelar") }
            Button(
                onClick = {
                    val m = monto.toDoubleOrNull()
                    if (categoria.isNotBlank() && m != null && m > 0) onGuardar(tipo, categoria, descripcion, m)
                },
                modifier = Modifier.weight(1f),
                enabled = categoria.isNotBlank() && monto.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(8.dp))
    }
}