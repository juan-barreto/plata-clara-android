package com.candlelabs.gestionpersonal.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import com.candlelabs.gestionpersonal.R



// ═══════════════════════════════════════════════════════════
// MERCADO PAGO SCREEN
// Pantalla para conectar la cuenta de MP e importar
// movimientos automáticamente al presupuesto.
// ═══════════════════════════════════════════════════════════
@Composable
fun MercadoPagoScreen() {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // ── Obtener userId de Supabase ─────────────────────────
    val userId = try {
        SupabaseClient.instance.auth.currentUserOrNull()?.id ?: ""
    } catch (e: Exception) { "" }

    // ── Estado de la pantalla ──────────────────────────────
    var conectado        by remember { mutableStateOf(false) }
    var cargando         by remember { mutableStateOf(false) }
    var movimientos      by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var error            by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmDesconectar by remember { mutableStateOf(false) }

    // ── Verificar si ya tiene MP conectado al entrar ───────
    LaunchedEffect(Unit) {
        try {
            cargando = true
            val estado = RetrofitClient.create(SupabaseClient.instance).getMpEstado()
            conectado = estado["conectado"] == true
        } catch (e: Exception) {
            conectado = false
        } finally {
            cargando = false
        }
    }

    // ── Dialog confirmar desconectar ───────────────────────
    if (mostrarConfirmDesconectar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmDesconectar = false },
            title = { Text("Desconectar Mercado Pago", color = TextoPrimario) },
            text  = { Text("¿Seguro? Vas a tener que volver a autorizar para importar movimientos.", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            RetrofitClient.create(SupabaseClient.instance).desconectarMp()
                            conectado = false
                            movimientos = emptyList()
                        } catch (_: Exception) {}
                        mostrarConfirmDesconectar = false
                    }
                }) { Text("Desconectar", color = RojoGasto) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmDesconectar = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
    ) {
        // ── Header ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text("Mercado Pago", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = TextoPrimario)
                Text("Importá tus movimientos", style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario)
            }
        }

        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzulMP)
            }
            return@Column
        }

        if (!conectado) {
            // ── Pantalla sin conectar ──────────────────────
            PantallaConectar(
                userId = userId,
                onConectar = {
                    // Abre el OAuth en el navegador
                    val url = "https://backend-gestion-personal.onrender.com/mp/auth?user_id=$userId"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        } else {
            // ── Pantalla conectada ─────────────────────────
            PantallaConectada(
                movimientos     = movimientos,
                cargandoMovs    = cargando,
                onImportar      = {
                    scope.launch {
                        cargando = true
                        try {
                            movimientos = RetrofitClient.create(SupabaseClient.instance).getMpMovimientos()
                        } catch (e: Exception) {
                            error = "Error al importar movimientos"
                        } finally {
                            cargando = false
                        }
                    }
                },
                onDesconectar   = { mostrarConfirmDesconectar = true }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA SIN CONECTAR
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaConectar(userId: String, onConectar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Ícono azul MP ──────────────────────────────────
        Surface(
            shape = CircleShape,
            color = AzulMP.copy(alpha = 0.15f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painterResource(R.drawable.plataclara_mp),
                    contentDescription = "Plata Clara",
                    modifier = Modifier.size(68.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Conectá tu cuenta de\nMercado Pago",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp)

        Spacer(Modifier.height(8.dp))

        Text("Importá tus pagos automáticamente\ncomo gastos en Plata Clara.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp)

        Spacer(Modifier.height(32.dp))

        // ── Card de garantías ──────────────────────────────
        Card(
            Modifier.fillMaxWidth(),
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GarantiaItem(Icons.Rounded.Lock,         "Tu contraseña nunca se comparte")
                GarantiaItem(Icons.Rounded.ShoppingCart, "Importá pagos como gastos")
                GarantiaItem(Icons.Rounded.Cancel,       "Desconectá cuando quieras")
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── Botón conectar ─────────────────────────────────
        Button(
            onClick  = onConectar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = AzulMP,
                contentColor   = Color.White
            )
        ) {
            Icon(Icons.Rounded.Link, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Conectar con Mercado Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun GarantiaItem(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, null, tint = AzulMP, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA CONECTADA
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaConectada(
    movimientos:  List<Map<String, Any>>,
    cargandoMovs: Boolean,
    onImportar:   () -> Unit,
    onDesconectar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Card estado conectado ──────────────────────────
        Card(
            Modifier.fillMaxWidth(),
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AzulMP.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, AzulMP.copy(alpha = 0.3f))
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = AzulMP.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = AzulMP,
                            modifier = Modifier.size(22.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("Mercado Pago conectado", style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text("Podés importar tus transacciones",
                        style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                }
            }
        }

        // ── Botones ────────────────────────────────────────
        Button(
            onClick  = onImportar,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AzulMP, contentColor = Color.White),
            enabled  = !cargandoMovs
        ) {
            if (cargandoMovs) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Download, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Importar transacciones", fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(
            onClick  = onDesconectar,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = RojoGasto),
            border   = androidx.compose.foundation.BorderStroke(1.dp, RojoGasto.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Rounded.LinkOff, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Desconectar", fontWeight = FontWeight.Medium)
        }

        // ── Lista de movimientos ───────────────────────────
        if (movimientos.isNotEmpty()) {
            Text("Movimientos recientes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario,
                modifier = Modifier.padding(top = 4.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(movimientos) { mov ->
                    FilaMovimientoMP(mov)
                }
            }
        } else {
            Box(
                Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.ReceiptLong, null, tint = TextoMuted,
                        modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No hay movimientos todavía",
                        style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    Text("Tocá Importar para traerlos",
                        style = MaterialTheme.typography.bodySmall, color = TextoMuted)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FILA MOVIMIENTO MP
// ═══════════════════════════════════════════════════════════
@Composable
private fun FilaMovimientoMP(mov: Map<String, Any>) {
    val monto    = (mov["monto"] as? Double) ?: 0.0
    val fecha    = mov["fecha"]?.toString() ?: ""
    val detalle  = mov["nombre"]?.toString() ?: "Pago"
    val esIngreso = mov["es_gasto"] as? Boolean == false
    val status   = mov["status"]?.toString()

    if (status != "approved") return

    // Card igual que antes...
    Card(
        Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(14.dp),
            ambientColor = SombraCard, spotColor = SombraCard),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (esIngreso) VerdePrimario.copy(alpha = 0.15f) else AzulMP.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (esIngreso) Icons.Rounded.TrendingUp else Icons.Rounded.ShoppingCart,
                        null,
                        tint = if (esIngreso) VerdePrimario else AzulMP,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(detalle, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, color = TextoPrimario)
                Text(fecha, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
            }
            Text(
                "${if (esIngreso) "+" else ""}$${String.format("%,.0f", Math.abs(monto)).replace(",", ".")}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (esIngreso) VerdePrimario else TextoPrimario
            )
        }
    }
}
