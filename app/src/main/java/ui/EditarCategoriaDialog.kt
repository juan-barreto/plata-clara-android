package com.candlelabs.gestionpersonal.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.candlelabs.gestionpersonal.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Dialog para editar el presupuesto de una categoría individual
// Se abre al tocar una card de categoría en el Home
@Composable
fun EditarCategoriaDialog(
    categoria: CategoriaResumen,
    totalIngresos: Double,
    presupuestosOtrasCategorias: Double, // suma de presupuestos de las demás categorías
    onGuardar: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var monto by remember { mutableStateOf(categoria.presupuesto.toLong().toString()) }
    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    // Animación de vibración cuando se pasa del límite
    var sacudir by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Disponible = ingresos - lo que ya asignaron las otras categorías
    val disponibleParaEsta = totalIngresos - presupuestosOtrasCategorias
    val montoDouble = monto.toDoubleOrNull() ?: 0.0

    // Validar en tiempo real
    val excede = montoDouble > disponibleParaEsta && disponibleParaEsta > 0
    val excedeTotal = montoDouble > totalIngresos

    // Ejecutar vibración
    LaunchedEffect(sacudir) {
        if (sacudir) {
            // Animación de sacudida: izquierda-derecha-izquierda rápido
            repeat(3) {
                shakeOffset.animateTo(10f, tween(50))
                shakeOffset.animateTo(-10f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
            sacudir = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            color = FondoCard
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título con ícono de la categoría
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(iconoPara(categoria.nombre), null, tint = VerdePrimario, modifier = Modifier.size(24.dp))
                    Text(
                        nombreDisplay(categoria.nombre).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextoPrimario, letterSpacing = 1.sp
                    )
                }

                // Info disponible
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdePrimario.copy(alpha = 0.08f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Ingreso total", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            Text("$${fmtAR(totalIngresos)}", style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold, color = VerdePrimario)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Asignado a otras", style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                            Text("$${fmtAR(presupuestosOtrasCategorias)}", style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold, color = TextoMuted)
                        }
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Divisor)
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Disponible para esta", style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold, color = TextoSecundario)
                            Text("$${fmtAR(disponibleParaEsta)}", style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold, color = if (disponibleParaEsta > 0) VerdePrimario else RojoGasto)
                        }
                    }
                }

                // Campo monto con animación de vibración
                OutlinedTextField(
                    value = monto,
                    onValueChange = { nuevo ->
                        val limpio = nuevo.filter { it.isDigit() }
                        monto = limpio
                        mostrarError = false
                        val m = limpio.toDoubleOrNull() ?: 0.0
                        if (m > totalIngresos) sacudir = true
                    },
                    label = { Text("Presupuesto mensual", color = TextoSecundario) },
                    prefix = { Text("$", color = VerdePrimario, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = excede || excedeTotal,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (excede || excedeTotal) RojoGasto else TextoPrimario,
                        unfocusedTextColor = if (excede || excedeTotal) RojoGasto else TextoPrimario,
                        focusedBorderColor = if (excede || excedeTotal) RojoGasto else VerdePrimario,
                        unfocusedBorderColor = if (excede || excedeTotal) RojoGasto else Divisor,
                        cursorColor = VerdePrimario, errorBorderColor = RojoGasto
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                        .graphicsLayer { translationX = shakeOffset.value },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (excede || excedeTotal) RojoGasto else TextoPrimario
                    )
                )

                // Barra de uso
                if (totalIngresos > 0) {
                    val porcentaje = (montoDouble / totalIngresos).toFloat().coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Divisor)) {
                        Box(Modifier.fillMaxWidth(porcentaje).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                            .background(if (excede || excedeTotal) RojoGasto else VerdePrimario))
                    }
                    Text(
                        "${(porcentaje * 100).toInt()}% de tu ingreso",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (excede || excedeTotal) RojoGasto else TextoMuted
                    )
                }

                // Mensaje de error
                if (excedeTotal) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = RojoGasto.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = RojoGasto, modifier = Modifier.size(16.dp))
                            Text("Este monto supera tu ingreso", color = RojoGasto, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else if (excede) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Naranja.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Warning, null, tint = Naranja, modifier = Modifier.size(16.dp))
                            Text("Este monto supera tu dinero disponible", color = Naranja, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Botones
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val m = monto.toDoubleOrNull() ?: 0.0
                            if (m > totalIngresos) {
                                mensajeError = "Supera tu ingreso"
                                sacudir = true
                                mostrarError = true
                            } else {
                                onGuardar(m)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
                        enabled = monto.isNotBlank() && (monto.toDoubleOrNull() ?: 1.0) >=0
                    ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
