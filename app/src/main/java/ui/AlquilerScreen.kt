package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun AlquilerScreen(navController: NavController) {

    val viewModel: AlquilerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var alquiler by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFirma by remember { mutableStateOf("") }
    var indiceSeleccionado by remember { mutableStateOf("ipc") }
    var periodoSeleccionado by remember { mutableIntStateOf(3) }

    val indices = listOf("ipc", "icl")
    val periodos = listOf(3 to "Trimestral", 4 to "Cuatrimestral", 6 to "Semestral", 12 to "Anual")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
            .verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════════════
        // HEADER
        // ══════════════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.Apartment, null, tint = VerdePrimario, modifier = Modifier.size(28.dp))
                Column {
                    Text("Calculadora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text("de alquiler", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }
            }
            IconButton(onClick = { navController.navigate(Rutas.INFO) }) {
                Icon(Icons.Rounded.Info, "Info", tint = VerdePrimario, modifier = Modifier.size(24.dp))
            }
        }

        // ══════════════════════════════════════════════
        // BLOQUE CREMA
        // ══════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(FondoPrincipal, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── FORMULARIO ──
            Card(
                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Monto
                    OutlinedTextField(
                        value = alquiler, onValueChange = { alquiler = it },
                        label = { Text("Monto actual del alquiler", color = TextoSecundario) },
                        prefix = { Text("$", color = VerdePrimario, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Rounded.AttachMoney, null, tint = TextoSecundario) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                            focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor, cursorColor = VerdePrimario
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )

                    // Fecha último aumento
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it.filter { c -> c.isDigit() }.take(8) },
                        label = { Text("Fecha de último aumento", color = TextoSecundario) },
                        placeholder = { Text("AAAA-MM-DD", color = TextoMuted) },
                        leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null, tint = TextoSecundario) },
                        visualTransformation = FechaVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                            focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor, cursorColor = VerdePrimario
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )

                    // Fecha firma
                    OutlinedTextField(
                        value = fechaFirma,
                        onValueChange = { fechaFirma = it.filter { c -> c.isDigit() }.take(8) },
                        label = { Text("Fecha de firma del contrato", color = TextoSecundario) },
                        placeholder = { Text("AAAA-MM-DD", color = TextoMuted) },
                        leadingIcon = { Icon(Icons.Rounded.EditCalendar, null, tint = TextoSecundario) },
                        visualTransformation = FechaVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                            focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor, cursorColor = VerdePrimario
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }

            // ── ÍNDICE ──
            Card(
                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ÍNDICE DE AJUSTE", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        indices.forEach { indice ->
                            FilterChip(
                                selected = indiceSeleccionado == indice,
                                onClick = { indiceSeleccionado = indice },
                                label = { Text(indice.uppercase(), fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        if (indice == "ipc") Icons.Rounded.TrendingUp else Icons.Rounded.BarChart,
                                        null, modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerdePrimario, selectedLabelColor = FondoNegro,
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

            // ── PERÍODO ──
            Card(
                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FondoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PERÍODO DE AJUSTE", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        periodos.take(2).forEach { (meses, etiqueta) ->
                            FilterChip(
                                selected = periodoSeleccionado == meses,
                                onClick = { periodoSeleccionado = meses },
                                label = { Text(etiqueta) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerdePrimario, selectedLabelColor = FondoNegro
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        periodos.drop(2).forEach { (meses, etiqueta) ->
                            FilterChip(
                                selected = periodoSeleccionado == meses,
                                onClick = { periodoSeleccionado = meses },
                                label = { Text(etiqueta) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerdePrimario, selectedLabelColor = FondoNegro
                                )
                            )
                        }
                    }
                }
            }

            // ── BOTÓN CALCULAR ──
            Button(
                onClick = {
                    val monto = alquiler.toDoubleOrNull()
                    if (monto != null && fechaInicio.length == 8 && fechaFirma.length == 8) {
                        val fi = "${fechaInicio.substring(0,4)}-${fechaInicio.substring(4,6)}-${fechaInicio.substring(6)}"
                        val ff = "${fechaFirma.substring(0,4)}-${fechaFirma.substring(4,6)}-${fechaFirma.substring(6)}"
                        viewModel.calcular(monto, fi, ff, indiceSeleccionado, periodoSeleccionado)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro)
            ) {
                Icon(Icons.Rounded.Calculate, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Calcular ajuste", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // ── RESULTADO ──
            when (uiState) {
                is AlquilerUiState.Idle -> {}
                is AlquilerUiState.Cargando -> {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VerdePrimario)
                    }
                }
                is AlquilerUiState.Exito -> {
                    val historial = (uiState as AlquilerUiState.Exito).respuesta.historial

                    Text("RESULTADO", style = MaterialTheme.typography.labelSmall, color = TextoSobreCreme, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)

                    historial.forEach { periodo ->
                        Card(
                            Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = SombraCard, spotColor = SombraCard),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FondoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.DateRange, null, tint = VerdePrimario, modifier = Modifier.size(18.dp))
                                    Text(periodo.periodo, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                                }
                                Text(
                                    "$${String.format("%,.0f", periodo.alquiler).replace(",", ".")}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VerdePrimario
                                )
                            }
                        }
                    }
                }
                is AlquilerUiState.Error -> {
                    val msg = (uiState as AlquilerUiState.Error).mensaje
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RojoGasto.copy(alpha = 0.1f))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

// Transforma visualmente "20240601" → "2024-06-01"
class FechaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val result = buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                if (index == 3 || index == 5) append('-')
            }
        }
        val offsetMap = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 3 -> offset; offset <= 5 -> offset + 1; offset <= 8 -> offset + 2; else -> result.length
            }
            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 4 -> offset; offset <= 7 -> offset - 1; offset <= 10 -> offset - 2; else -> digits.length
            }
        }
        return TransformedText(AnnotatedString(result), offsetMap)
    }
}