package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.model.CategoriaContexto
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun AsistenteScreen(homeViewModel: HomeViewModel? = null) {

    val context   = LocalContext.current
    val viewModel: AsistenteViewModel = viewModel(factory = AsistenteViewModel.factory(context))
    val mensajes  by viewModel.mensajes.collectAsState()
    val esperando by viewModel.esperando.collectAsState()
    var input     by remember { mutableStateOf("") }

    // ── Inyectar contexto financiero desde HomeViewModel ─────────
    // Cada vez que el HomeViewModel tiene datos nuevos, se los pasamos
    // a Clara para que pueda responder con información real del usuario
    val uiStateHome by (homeViewModel?.uiState ?: return).collectAsState()
    LaunchedEffect(uiStateHome) {
        if (uiStateHome is HomeUiState.Exito) {
            val datos = uiStateHome as HomeUiState.Exito
            viewModel.actualizarContexto(
                ingreso    = datos.totalIngresos,
                gastos     = datos.totalGastos,
                balance    = datos.balance,
                categorias = datos.categorias.map {
                    CategoriaContexto(
                        nombre      = it.nombre,
                        gastado     = it.gastado,
                        presupuesto = it.presupuesto
                    )
                }
            )
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) listState.animateScrollToItem(mensajes.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        // ── Header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth().height(75.dp)
                .background(FondoNegro)
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.clarai),
                contentDescription = "ClaraAi",
                modifier = Modifier
                    .height(200.dp).fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(bottom = 10.dp)
                    .offset(x = (-158.dp)),
                contentScale = ContentScale.FillHeight
            )
            Text(
                text  = "Asistente financiero",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 15.dp)
            )
            IconButton(
                onClick  = { viewModel.limpiarChat() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.Delete, "Limpiar chat", tint = TextoSecundario)
            }
        }
        HorizontalDivider(color = Divisor)
        // ── Mensajes ──────────────────────────────────────────────
        LazyColumn(
            state    = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (mensajes.isEmpty()) {
                item {
                    BurbujaMensaje(MensajeChatUI(
                        rol      = "assistant",
                        contenido = "¡Hola! Soy Clara, tu asistente financiero. Podés preguntarme sobre el dólar, la inflación, tu contrato de alquiler o cualquier duda sobre la economía argentina. ¿En qué te ayudo?"
                    ))
                }
            }
            items(mensajes) { mensaje -> BurbujaMensaje(mensaje = mensaje) }
        }

        // ── Input ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FondoCard)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = input,
                onValueChange = { input = it },
                placeholder   = { Text("Preguntale a Clara...", color = TextoMuted) },
                modifier      = Modifier.weight(1f),
                enabled       = !esperando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank()) { viewModel.enviarMensaje(input); input = "" }
                }),
                maxLines = 3,
                shape    = RoundedCornerShape(24.dp),
                colors   = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = VerdePrimario,
                    unfocusedBorderColor = Divisor,
                    cursorColor          = VerdePrimario,
                    focusedTextColor     = TextoPrimario,
                    unfocusedTextColor   = TextoPrimario
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick  = { if (input.isNotBlank()) { viewModel.enviarMensaje(input); input = "" } },
                enabled  = !esperando && input.isNotBlank(),
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(if (input.isNotBlank() && !esperando) VerdePrimario else Divisor)
            ) {
                if (esperando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Filled.Send, "Enviar", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// BURBUJA DE MENSAJE
// ═══════════════════════════════════════════════════════════
@Composable
fun BurbujaMensaje(mensaje: MensajeChatUI) {
    val esUsuario = mensaje.rol == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart    = 16.dp, topEnd = 16.dp,
                    bottomStart = if (esUsuario) 16.dp else 4.dp,
                    bottomEnd   = if (esUsuario) 4.dp else 16.dp
                ))
                .background(if (esUsuario) VerdePrimario else FondoCard)
                .then(
                    if (!esUsuario) Modifier.border(0.5.dp, BordeCard, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                    else Modifier
                )
                .padding(12.dp)
        ) {
            if (mensaje.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = VerdePrimario)
            } else {
                Text(
                    text  = mensaje.contenido,
                    color = if (esUsuario) FondoNegro else TextoPrimario,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}