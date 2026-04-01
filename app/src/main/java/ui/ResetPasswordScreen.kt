package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.ui.theme.*

// Pantalla que aparece cuando el usuario toca el link de recuperación del email
// Recibe el tokenHash del deep link y permite escribir la nueva contraseña
@Composable
fun ResetPasswordScreen(
    tokenHash: String,
    type: String,
    onExito: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(context))
    val uiState by viewModel.uiState.collectAsState()

    var nuevaPassword by remember { mutableStateOf("") }
    var repetirPassword by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarRepetir by remember { mutableStateOf(false) }

    val passwordLarga = nuevaPassword.length >= 6
    val passwordsCoinciden = nuevaPassword == repetirPassword

    // Cuando Supabase confirma que la contraseña se actualizó, navegamos al login
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.PasswordActualizado) onExito()
    }

    Box(Modifier.fillMaxSize().background(FondoNegro)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.LockReset,
                null,
                tint = VerdePrimario,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(24.dp))

            Text(
                "Nueva contraseña",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Escribí tu nueva contraseña. Tiene que tener al menos 6 caracteres.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            // Campo nueva contraseña
            OutlinedTextField(
                value = nuevaPassword,
                onValueChange = { nuevaPassword = it },
                label = { Text("Nueva contraseña", color = TextoSecundario) },
                leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = TextoSecundario) },
                trailingIcon = {
                    IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Icon(
                            if (mostrarPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            null, tint = TextoSecundario
                        )
                    }
                },
                visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                    focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor,
                    cursorColor = VerdePrimario
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (nuevaPassword.isNotEmpty() && !passwordLarga) {
                Text(
                    "Mínimo 6 caracteres",
                    style = MaterialTheme.typography.bodySmall,
                    color = RojoGasto,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Campo repetir contraseña
            OutlinedTextField(
                value = repetirPassword,
                onValueChange = { repetirPassword = it },
                label = { Text("Repetir contraseña", color = TextoSecundario) },
                leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = TextoSecundario) },
                trailingIcon = {
                    IconButton(onClick = { mostrarRepetir = !mostrarRepetir }) {
                        Icon(
                            if (mostrarRepetir) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            null, tint = TextoSecundario
                        )
                    }
                },
                visualTransformation = if (mostrarRepetir) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                    focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor,
                    cursorColor = VerdePrimario
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (repetirPassword.isNotEmpty() && !passwordsCoinciden) {
                Text(
                    "Las contraseñas no coinciden",
                    style = MaterialTheme.typography.bodySmall,
                    color = RojoGasto,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                )
            }
            if (repetirPassword.isNotEmpty() && passwordsCoinciden && passwordLarga) {
                Row(
                    Modifier.align(Alignment.Start).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(14.dp))
                    Text("Las contraseñas coinciden", style = MaterialTheme.typography.bodySmall, color = VerdePrimario)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Botón guardar
            Button(
                onClick = {
                    viewModel.actualizarPassword(tokenHash, type, nuevaPassword)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
                enabled = passwordLarga && passwordsCoinciden && uiState !is AuthUiState.Cargando
            ) {
                if (uiState is AuthUiState.Cargando) {
                    CircularProgressIndicator(color = FondoNegro, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar contraseña", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Mensaje de error
            if (uiState is AuthUiState.Error) {
                Spacer(Modifier.height(16.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RojoGasto.copy(alpha = 0.1f))
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = RojoGasto, modifier = Modifier.size(18.dp))
                        Text(
                            (uiState as AuthUiState.Error).mensaje,
                            color = RojoGasto,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
