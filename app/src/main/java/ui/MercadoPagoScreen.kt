package com.candlelabs.gestionpersonal.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candlelabs.gestionpersonal.network.RetrofitClient
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import com.candlelabs.gestionpersonal.R


// ═══════════════════════════════════════════════════════════
// MERCADO PAGO SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
fun MercadoPagoScreen() {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val userId = try {
        SupabaseClient.instance.auth.currentUserOrNull()?.id ?: ""
    } catch (e: Exception) { "" }

    var conectado by remember { mutableStateOf(false) }
    var cargando  by remember { mutableStateOf(false) }
    var mostrarConfirmDesconectar by remember { mutableStateOf(false) }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
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
            PantallaConectar(
                userId = userId,
                onConectar = {
                    val url = "https://backend-gestion-personal.onrender.com/mp/auth?user_id=$userId"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        } else {
            PantallaConectada(onDesconectar = { mostrarConfirmDesconectar = true })
        }
    }
}


// ═══════════════════════════════════════════════════════════
// PANTALLA SIN CONECTAR
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaConectar(userId: String, onConectar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

        Button(
            onClick  = onConectar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AzulMP, contentColor = Color.White)
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
// PANTALLA CONECTADA — próximamente
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaConectada(onDesconectar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = VerdePrimario.copy(alpha = 0.15f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.CheckCircle, null,
                    tint = VerdePrimario, modifier = Modifier.size(48.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("¡Cuenta conectada!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            textAlign = TextAlign.Center)

        Spacer(Modifier.height(8.dp))

        Text("La importación automática de movimientos\nestá llegando muy pronto.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp)

        Spacer(Modifier.height(32.dp))

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
    }
}