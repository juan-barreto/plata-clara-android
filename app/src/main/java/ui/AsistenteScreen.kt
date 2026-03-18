package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun AsistenteScreen() {

    val context = LocalContext.current
    val viewModel: AsistenteViewModel = viewModel(
        factory = AsistenteViewModel.factory(context)
    )
    val mensajes by viewModel.mensajes.collectAsState()
    val esperando by viewModel.esperando.collectAsState()
    var input by remember { mutableStateOf("") }

    // Scroll automático al último mensaje
    val listState = rememberLazyListState()
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // — HEADER —
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar de Clara
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        color = Color(0xFF14532D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Clara",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Asistente financiero",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            // Botón limpiar chat
            IconButton(onClick = { viewModel.limpiarChat() }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Limpiar chat",
                    tint = Color.White
                )
            }
        }

        // — LISTA DE MENSAJES —
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mensaje de bienvenida si el chat está vacío
            if (mensajes.isEmpty()) {
                item {
                    BurbujaMensaje(
                        mensaje = MensajeChatUI(
                            rol = "assistant",
                            contenido = "¡Hola! Soy Clara, tu asistente financiero. " +
                                    "Podés preguntarme sobre el dólar, la inflación, " +
                                    "tu contrato de alquiler o cualquier duda sobre " +
                                    "la economía argentina. ¿En qué te ayudo?"
                        )
                    )
                }
            }

            items(mensajes) { mensaje ->
                BurbujaMensaje(mensaje = mensaje)
            }
        }

        // — INPUT —
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Preguntale a Clara...") },
                modifier = Modifier.weight(1f),
                enabled = !esperando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.enviarMensaje(input)
                        input = ""
                    }
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Botón enviar — muestra spinner si está esperando
            IconButton(
                onClick = {
                    viewModel.enviarMensaje(input)
                    input = ""
                },
                enabled = !esperando && input.isNotBlank()
            ) {
                if (esperando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Enviar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Burbuja de mensaje — estilo diferente para usuario y asistente
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
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (esUsuario) 16.dp else 4.dp,
                        bottomEnd = if (esUsuario) 4.dp else 16.dp
                    )
                )
                .background(
                    if (esUsuario) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            if (mensaje.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = mensaje.contenido,
                    color = if (esUsuario) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}