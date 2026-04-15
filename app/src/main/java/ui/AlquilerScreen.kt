package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ═══════════════════════════════════════════════════════════
// ALQUILER SCREEN
// Calculadora de ajuste de alquiler por IPC/ICL.
// Los campos de fecha usan DatePickerDialog de Material3.
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlquilerScreen(navController: NavController) {
    val viewModel: AlquilerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var alquiler            by remember { mutableStateOf("") }
    var fechaInicio         by remember { mutableStateOf("") } // formato YYYY-MM-DD
    var fechaFirma          by remember { mutableStateOf("") } // formato YYYY-MM-DD
    var indiceSeleccionado  by remember { mutableStateOf("ipc") }
    var periodoSeleccionado by remember { mutableIntStateOf(3) }

    // ── Estado de los date pickers ─────────────────────────
    var mostrarPickerInicio by remember { mutableStateOf(false) }
    var mostrarPickerFirma  by remember { mutableStateOf(false) }
    val datePickerStateInicio = rememberDatePickerState()
    val datePickerStateFirma  = rememberDatePickerState()

    // ── Formateador de milisegundos → YYYY-MM-DD ───────────
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun millisToFecha(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate().format(formatter)

    val indices = listOf("ipc", "icl")
    val periodos = listOf(3 to "Trimestral", 4 to "Cuatrimestral", 6 to "Semestral", 12 to "Anual")

    // ── DatePickerDialog — Fecha inicio ────────────────────
    if (mostrarPickerInicio) {
        DatePickerDialog(
            onDismissRequest = { mostrarPickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerStateInicio.selectedDateMillis?.let {
                        fechaInicio = millisToFecha(it)
                    }
                    mostrarPickerInicio = false
                }) { Text("Confirmar", color = VerdePrimario) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPickerInicio = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = FondoCard
            )
        ) {
            DatePicker(
                state = datePickerStateInicio,
                colors = DatePickerDefaults.colors(
                    containerColor          = FondoCard,
                    titleContentColor       = TextoSecundario,
                    headlineContentColor    = TextoPrimario,
                    weekdayContentColor     = TextoSecundario,
                    subheadContentColor     = TextoSecundario,
                    dayContentColor         = TextoPrimario,
                    selectedDayContentColor = FondoNegro,
                    selectedDayContainerColor = VerdePrimario,
                    todayContentColor       = VerdePrimario,
                    todayDateBorderColor    = VerdePrimario,
                    navigationContentColor  = TextoPrimario,
                    yearContentColor        = TextoPrimario,
                    currentYearContentColor = VerdePrimario,
                    selectedYearContentColor = FondoNegro,
                    selectedYearContainerColor = VerdePrimario
                )
            )
        }
    }

    // ── DatePickerDialog — Fecha firma ─────────────────────
    if (mostrarPickerFirma) {
        DatePickerDialog(
            onDismissRequest = { mostrarPickerFirma = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerStateFirma.selectedDateMillis?.let {
                        fechaFirma = millisToFecha(it)
                    }
                    mostrarPickerFirma = false
                }) { Text("Confirmar", color = VerdePrimario) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPickerFirma = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = FondoCard)
        ) {
            DatePicker(
                state = datePickerStateFirma,
                colors = DatePickerDefaults.colors(
                    containerColor          = FondoCard,
                    titleContentColor       = TextoSecundario,
                    headlineContentColor    = TextoPrimario,
                    weekdayContentColor     = TextoSecundario,
                    subheadContentColor     = TextoSecundario,
                    dayContentColor         = TextoPrimario,
                    selectedDayContentColor = FondoNegro,
                    selectedDayContainerColor = VerdePrimario,
                    todayContentColor       = VerdePrimario,
                    todayDateBorderColor    = VerdePrimario,
                    navigationContentColor  = TextoPrimario,
                    yearContentColor        = TextoPrimario,
                    currentYearContentColor = VerdePrimario,
                    selectedYearContentColor = FondoNegro,
                    selectedYearContainerColor = VerdePrimario
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("Calculadora", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text("de alquiler", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }
            }
            IconButton(onClick = { navController.navigate(Rutas.INFO) }) {
                Icon(Icons.Rounded.Info, "Info", tint = VerdePrimario, modifier = Modifier.size(24.dp))
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Formulario ───────────────────────────────────
            Card(
                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape  = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // ── Campo monto ───────────────────────────
                    OutlinedTextField(
                        value = alquiler, onValueChange = { alquiler = it },
                        label = { Text("Monto actual del alquiler", color = TextoSecundario) },
                        prefix = { Text("$", color = VerdePrimario, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Rounded.AttachMoney, null, tint = TextoSecundario) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor    = TextoPrimario, unfocusedTextColor = TextoPrimario,
                            focusedBorderColor  = VerdePrimario, unfocusedBorderColor = Divisor,
                            cursorColor         = VerdePrimario
                        ),
                        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), singleLine = true
                    )

                    // ── Campo fecha inicio — abre DatePicker ──
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de último aumento", color = TextoSecundario) },
                        placeholder = { Text("Tocá para elegir", color = TextoMuted) },
                        leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null, tint = TextoSecundario) },
                        trailingIcon = {
                            Icon(Icons.Rounded.DateRange, null, tint = VerdePrimario,
                                modifier = Modifier.clickable { mostrarPickerInicio = true })
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor    = TextoPrimario, unfocusedTextColor = TextoPrimario,
                            focusedBorderColor  = VerdePrimario, unfocusedBorderColor = Divisor,
                            cursorColor         = VerdePrimario,
                            disabledTextColor   = TextoPrimario,
                            disabledBorderColor = Divisor,
                            disabledLeadingIconColor = TextoSecundario,
                            disabledTrailingIconColor = VerdePrimario,
                            disabledLabelColor  = TextoSecundario
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarPickerInicio = true },
                        enabled = false,
                        singleLine = true
                    )

                    // ── Campo fecha firma — abre DatePicker ───
                    OutlinedTextField(
                        value = fechaFirma,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de firma del contrato", color = TextoSecundario) },
                        placeholder = { Text("Tocá para elegir", color = TextoMuted) },
                        leadingIcon = { Icon(Icons.Rounded.EditCalendar, null, tint = TextoSecundario) },
                        trailingIcon = {
                            Icon(Icons.Rounded.DateRange, null, tint = VerdePrimario,
                                modifier = Modifier.clickable { mostrarPickerFirma = true })
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor    = TextoPrimario, unfocusedTextColor = TextoPrimario,
                            focusedBorderColor  = VerdePrimario, unfocusedBorderColor = Divisor,
                            cursorColor         = VerdePrimario,
                            disabledTextColor   = TextoPrimario,
                            disabledBorderColor = Divisor,
                            disabledLeadingIconColor = TextoSecundario,
                            disabledTrailingIconColor = VerdePrimario,
                            disabledLabelColor  = TextoSecundario
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarPickerFirma = true },
                        enabled = false,
                        singleLine = true
                    )
                }
            }

            // ── Índice ───────────────────────────────────────
            Card(
                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape  = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ÍNDICE DE AJUSTE", style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        indices.forEach { indice ->
                            FilterChip(
                                selected = indiceSeleccionado == indice,
                                onClick  = { indiceSeleccionado = indice },
                                label    = { Text(indice.uppercase(), fontWeight = FontWeight.Bold) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor   = VerdePrimario,
                                    selectedLabelColor       = FondoNegro,
                                    selectedLeadingIconColor = FondoNegro
                                )
                            )
                        }
                    }
                    if (indiceSeleccionado == "icl") {
                        Text(
                            "El ICL es obligatorio para contratos firmados antes del 17/10/2023. Para contratos más nuevos, las partes lo acuerdan libremente.",
                            style = MaterialTheme.typography.bodySmall, color = VerdePrimario, lineHeight = 18.sp
                        )
                    }
                }
            }

            // ── Período ──────────────────────────────────────
            Card(
                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape  = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PERÍODO DE AJUSTE", style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        periodos.take(2).forEach { (meses, etiqueta) ->
                            FilterChip(
                                selected = periodoSeleccionado == meses,
                                onClick  = { periodoSeleccionado = meses },
                                label    = { Text(etiqueta) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerdePrimario,
                                    selectedLabelColor     = FondoNegro
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        periodos.drop(2).forEach { (meses, etiqueta) ->
                            FilterChip(
                                selected = periodoSeleccionado == meses,
                                onClick  = { periodoSeleccionado = meses },
                                label    = { Text(etiqueta) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerdePrimario,
                                    selectedLabelColor     = FondoNegro
                                )
                            )
                        }
                    }
                }
            }

            // ── Botón calcular ────────────────────────────────
            Button(
                onClick = {
                    val monto = alquiler.toDoubleOrNull()
                    // Las fechas ya vienen en formato YYYY-MM-DD desde el picker
                    if (monto != null && fechaInicio.length == 10 && fechaFirma.length == 10) {
                        viewModel.calcular(monto, fechaInicio, fechaFirma, indiceSeleccionado, periodoSeleccionado)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
            ) {
                Icon(Icons.Rounded.Calculate, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Calcular ajuste", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // ── Resultado ─────────────────────────────────────
            when (uiState) {
                is AlquilerUiState.Idle -> {}
                is AlquilerUiState.Cargando -> {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdePrimario)
                    }
                }
                is AlquilerUiState.Exito -> {
                    val historial = (uiState as AlquilerUiState.Exito).respuesta.historial
                    Text("RESULTADO", style = MaterialTheme.typography.labelSmall,
                        color = TextoPrimario, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    historial.forEach { periodo ->
                        Card(
                            Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = SombraCard, spotColor = SombraCard),
                            shape  = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp),
                                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.DateRange, null, tint = VerdePrimario,
                                        modifier = Modifier.size(18.dp))
                                    Text(periodo.periodo, style = MaterialTheme.typography.bodyMedium,
                                        color = TextoPrimario)
                                }
                                Text(
                                    "$${String.format("%,.0f", periodo.alquiler).replace(",", ".")}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = VerdePrimario
                                )
                            }
                        }
                    }
                }
                is AlquilerUiState.Error -> {
                    val msg = (uiState as AlquilerUiState.Error).mensaje
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RojoGasto.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = RojoGasto, modifier = Modifier.size(18.dp))
                            Text(msg, style = MaterialTheme.typography.bodySmall, color = RojoGasto)
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}