package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.R

@Composable
fun AsistenteScreen() {

    val context = LocalContext.current
    val viewModel: AsistenteViewModel = viewModel(
        factory = AsistenteViewModel.factory(context)
    )
    val mensajes by viewModel.mensajes.collectAsState()
    val esperando by viewModel.esperando.collectAsState()
    var input by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F2F2))
            .windowInsetsPadding(WindowInsets.ime)
    ) {

        // ─── HEADER NEGRO ───────────────────────────────────────
        // Usamos Box para que el logo, el subtítulo y el botón
        // se posicionen independientemente dentro del mismo espacio.
        // Así el Image de 100dp no empuja al Text fuera del header.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .background(Color(0xFF000000))
                .padding(horizontal = 16.dp)
        ) {
            // Logo — centrado verticalmente, alineado a la izquierda
            Image(
                painter = painterResource(id = R.drawable.clarai),
                contentDescription = "ClaraAi",
                modifier = Modifier
                    .height(180.dp).fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(bottom = 10.dp)
                    .offset(x = (-155.dp)),
                contentScale = ContentScale.FillHeight
            )

            // Subtítulo — pegado al borde inferior izquierdo del header
            Text(
                text = "Asistente financiero",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888888),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 15.dp)
            )

            // Botón limpiar — centrado vertical, alineado a la derecha
            IconButton(
                onClick = { viewModel.limpiarChat() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Limpiar chat",
                    tint = Color(0xFF888888)
                )
            }
        }

        // ─── LISTA DE MENSAJES ──────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

        // ─── INPUT ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = {
                    Text(
                        "Preguntale a Clara...",
                        color = Color(0xFF888888)
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !esperando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (input.isNotBlank()) {
                            viewModel.enviarMensaje(input)
                            input = ""
                        }
                    }
                ),
                maxLines = 3,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00B872),
                    unfocusedBorderColor = Color(0xFFDDDDDD),
                    cursorColor = Color(0xFF00B872)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.enviarMensaje(input)
                        input = ""
                    }
                },
                enabled = !esperando && input.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (input.isNotBlank() && !esperando) Color(0xFF00B872)
                        else Color(0xFFDDDDDD)
                    )
            ) {
                if (esperando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─── BURBUJA DE MENSAJE ─────────────────────────────────────────
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
                    if (esUsuario) Color(0xFF00B872)
                    else Color.White
                )
                .then(
                    if (!esUsuario) Modifier.border(
                        width = 0.5.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    else Modifier
                )
                .padding(12.dp)
        ) {
            if (mensaje.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF00B872)
                )
            } else {
                Text(
                    text = mensaje.contenido,
                    color = if (esUsuario) Color.White
                    else Color(0xFF000000),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}