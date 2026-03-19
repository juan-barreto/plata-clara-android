package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.model.MovimientoItem

// Categorías disponibles con su emoji
// Separadas por tipo para mostrar las correctas según lo que el usuario elige
val CATEGORIAS_INGRESO = listOf(
    "Sueldo", "Freelance", "Extra",
    "Inversión", "Otro ingreso"
)

val CATEGORIAS_GASTO = listOf(
    "Alquiler", "Supermercado", "Transporte",
    "Servicios", "Comida/Salidas", "Salud",
    "Tecnología", "Educación", "Entretenimiento",
    "Deudas/Cuotas", "Otro gasto"
)

// Mapa categoria → ícono de Material Icons
val ICONOS_CATEGORIA = mapOf(
    "Sueldo"          to Icons.Filled.Work,
    "Freelance"       to Icons.Filled.Laptop,
    "Extra"           to Icons.Filled.AddCircle,
    "Inversión"       to Icons.Filled.TrendingUp,
    "Otro ingreso"    to Icons.Filled.AttachMoney,
    "Alquiler"        to Icons.Filled.Home,
    "Supermercado"    to Icons.Filled.ShoppingCart,
    "Transporte"      to Icons.Filled.DirectionsBus,
    "Servicios"       to Icons.Filled.Bolt,
    "Comida/Salidas"  to Icons.Filled.Restaurant,
    "Salud"           to Icons.Filled.LocalHospital,
    "Tecnología"      to Icons.Filled.PhoneAndroid,
    "Educación"       to Icons.Filled.School,
    "Entretenimiento" to Icons.Filled.TheaterComedy,
    "Deudas/Cuotas"   to Icons.Filled.CreditCard,
    "Otro gasto"      to Icons.Filled.Category,
)

// Mapa categoria → color del círculo
val COLORES_CATEGORIA = mapOf(
    "Sueldo"          to Color(0xFF16A34A),
    "Freelance"       to Color(0xFF0891B2),
    "Extra"           to Color(0xFF7C3AED),
    "Inversión"       to Color(0xFFD97706),
    "Otro ingreso"    to Color(0xFF16A34A),
    "Alquiler"        to Color(0xFFDC2626),
    "Supermercado"    to Color(0xFFEA580C),
    "Transporte"      to Color(0xFF2563EB),
    "Servicios"       to Color(0xFFF59E0B),
    "Comida/Salidas"  to Color(0xFFDB2777),
    "Salud"           to Color(0xFF16A34A),
    "Tecnología"      to Color(0xFF6366F1),
    "Educación"       to Color(0xFF0891B2),
    "Entretenimiento" to Color(0xFF7C3AED),
    "Deudas/Cuotas"   to Color(0xFFDC2626),
    "Otro gasto"      to Color(0xFF6B7280),
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestoScreen() {

    val viewModel: PresupuestoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val filtroActual by viewModel.filtro.collectAsState()

    // Controla si el BottomSheet está abierto
    var mostrarBottomSheet by remember { mutableStateOf(false) }

    // Movimiento que se está editando — null si es uno nuevo
    // Equivalente en Python: movimiento_editando = None
    var movimientoEditando by remember { mutableStateOf<MovimientoItem?>(null) }

    // Estado del BottomSheet de confirmación de borrado
    var mostrarConfirmBorrado by remember { mutableStateOf(false) }
    var idParaBorrar by remember { mutableStateOf<Int?>(null) }

    // Toggle del gráfico — torta o línea
    var graficoDeTorta by remember { mutableStateOf(true) }

    // Filtros disponibles
    val filtros = listOf("semanal", "mensual", "anual")

    // Diálogo de confirmación de borrado
    if (mostrarConfirmBorrado && idParaBorrar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmBorrado = false },
            title = { Text("Borrar movimiento") },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarMovimiento(idParaBorrar!!)
                    mostrarConfirmBorrado = false
                    idParaBorrar = null
                }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // BottomSheet para agregar/editar
    if (mostrarBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarBottomSheet = false
                movimientoEditando = null
            }
        ) {
            FormularioMovimiento(
                movimientoInicial = movimientoEditando,
                onGuardar = { tipo, categoria, descripcion, monto ->
                    if (movimientoEditando != null) {
                        viewModel.editarMovimiento(
                            movimientoEditando!!.id,
                            tipo, categoria, descripcion, monto
                        )
                    } else {
                        viewModel.agregarMovimiento(tipo, categoria, descripcion, monto)
                    }
                    mostrarBottomSheet = false
                    movimientoEditando = null
                },
                onCancelar = {
                    mostrarBottomSheet = false
                    movimientoEditando = null
                }
            )
        }
    }

    // Contenido principal
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // — HEADER —
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Presupuesto Personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { mostrarBottomSheet = true }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Agregar movimiento",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // — FILTROS —
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filtros.forEach { filtro ->
                    FilterChip(
                        selected = filtroActual == filtro,
                        onClick = { viewModel.cambiarFiltro(filtro) },
                        label = {
                            Text(
                                text = filtro.replaceFirstChar { it.uppercase() }
                            )
                        }
                    )
                }
            }
        }

        // Contenido según el estado
        when (val estado = uiState) {

            is PresupuestoUiState.Cargando -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is PresupuestoUiState.Error -> {
                item {
                    Text(
                        text = "Error: ${estado.mensaje}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is PresupuestoUiState.Exito -> {
                val movimientos = estado.movimientos
                val ingresos = viewModel.calcularTotalIngresos(movimientos)
                val gastos = viewModel.calcularTotalGastos(movimientos)
                val balance = viewModel.calcularBalance(movimientos)
                val porCategoria = viewModel.calcularPorCategoria(movimientos)

                // — RESUMEN —
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Ingresos
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ingresos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", ingresos)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = VERDE
                                    )
                                }
                                // Gastos
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Gastos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", gastos)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                                // Balance
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Balance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${if (balance >= 0) "+" else ""}$${String.format("%,.0f", balance)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (balance >= 0) VERDE else Color(0xFFDC2626)
                                    )
                                }
                            }

                            // Emoji de estado según el balance
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    balance > 0 -> "✅ Vas bien — tenés un superávit"
                                    balance == 0.0 -> "⚠️ Justo — tus gastos igualan tus ingresos"
                                    else -> "🔴 Cuidado — estás gastando más de lo que ganás"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // — GRÁFICO —
                if (movimientos.isNotEmpty()) {
                    item {
                        // Botones toggle — reemplaza el Switch con emojis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !graficoDeTorta,
                                onClick = { graficoDeTorta = false },
                                label = { Text("📈 Balance") }
                            )
                            FilterChip(
                                selected = graficoDeTorta,
                                onClick = { graficoDeTorta = true },
                                label = { Text("📊 Por semana") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                if (graficoDeTorta) {
                                    GraficoBarrasPorSemana(movimientos = movimientos)
                                } else {
                                    GraficoBalanceAcumulado(movimientos = movimientos)
                                }
                            }
                        }
                    }
                }
                // — LISTA DE MOVIMIENTOS —
                item {
                    Text(
                        text = "Movimientos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (movimientos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Todavía no hay movimientos en este período.\nTocá el + para agregar uno.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(movimientos) { movimiento ->
                        FilaMovimiento(
                            movimiento = movimiento,
                            onEditar = {
                                movimientoEditando = movimiento
                                mostrarBottomSheet = true
                            },
                            onBorrar = {
                                idParaBorrar = movimiento.id
                                mostrarConfirmBorrado = true
                            }
                        )
                    }
                }

                // — INFO AL FINAL —
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Registrá todos tus ingresos y gastos para tener una " +
                                        "visión clara de tu situación financiera. " +
                                        "El balance se actualiza automáticamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fila individual de un movimiento
@Composable
fun FilaMovimiento(
    movimiento: MovimientoItem,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    val esIngreso = movimiento.tipo == "ingreso"
    val icono = ICONOS_CATEGORIA[movimiento.categoria] ?: Icons.Filled.AttachMoney
    val colorIcono = COLORES_CATEGORIA[movimiento.categoria] ?: Color(0xFF6B7280)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo con ícono de la categoría
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = colorIcono.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = movimiento.categoria,
                        tint = colorIcono,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Categoría, descripción y fecha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movimiento.categoria,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!movimiento.descripcion.isNullOrBlank()) {
                    Text(
                        text = movimiento.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = movimiento.fecha.substring(0, 10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Monto con color
            Text(
                text = "${if (esIngreso) "+" else "-"}$${String.format("%,.0f", movimiento.monto)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (esIngreso) Color(0xFF16A34A) else Color(0xFFDC2626)
            )

            // Botones
            Row {
                IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onBorrar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Borrar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Formulario dentro del BottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMovimiento(
    movimientoInicial: MovimientoItem?,
    onGuardar: (String, String, String, Double) -> Unit,
    onCancelar: () -> Unit
) {
    // Si estamos editando, pre-llenamos los campos
    var tipo by remember { mutableStateOf(movimientoInicial?.tipo ?: "gasto") }
    var categoria by remember { mutableStateOf(movimientoInicial?.categoria ?: "") }
    var descripcion by remember { mutableStateOf(movimientoInicial?.descripcion ?: "") }
    var monto by remember { mutableStateOf(movimientoInicial?.monto?.toString() ?: "") }
    var expandirCategorias by remember { mutableStateOf(false) }

    // Las categorías cambian según el tipo seleccionado
    val categorias = if (tipo == "ingreso") CATEGORIAS_INGRESO else CATEGORIAS_GASTO

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (movimientoInicial != null) "Editar movimiento" else "Nuevo movimiento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Toggle ingreso / gasto
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ingreso", "gasto").forEach { opcion ->
                FilterChip(
                    selected = tipo == opcion,
                    onClick = {
                        tipo = opcion
                        categoria = "" // limpia la categoría al cambiar tipo
                    },
                    label = {
                        Text(if (opcion == "ingreso") "🟢 Ingreso" else "🔴 Gasto")
                    }
                )
            }
        }

        // Selector de categoría
        ExposedDropdownMenuBox(
            expanded = expandirCategorias,
            onExpandedChange = { expandirCategorias = it }
        ) {
            OutlinedTextField(
                value = categoria,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirCategorias) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandirCategorias,
                onDismissRequest = { expandirCategorias = false }
            ) {
                categorias.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            categoria = cat
                            expandirCategorias = false
                        }
                    )
                }
            }
        }

        // Campo descripción opcional
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo monto
        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            label = { Text("Monto") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    if (categoria.isNotBlank() && montoDouble != null && montoDouble > 0) {
                        onGuardar(tipo, categoria, descripcion, montoDouble)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = categoria.isNotBlank() && monto.toDoubleOrNull() != null
            ) {
                Text("Guardar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Constante de color verde para usar en la Screen
// La definimos acá porque no podemos usar MaterialTheme fuera de un @Composable
private val VERDE = Color(0xFF16A34A)