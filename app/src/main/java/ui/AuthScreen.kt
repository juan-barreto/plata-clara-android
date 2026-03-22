package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.ui.theme.*

@Composable
fun AuthScreen(
    onAuthExitoso: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(context))
    val uiState by viewModel.uiState.collectAsState()

    // Si ya tiene sesión, va directo al home
    LaunchedEffect(Unit) {
        viewModel.verificarSesion()
    }

    // Si el login fue exitoso, navegamos
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Exito) {
            onAuthExitoso()
        }
    }

    // Toggle entre login y registro
    var esRegistro by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_plata_clara),
                contentDescription = "Plata Clara",
                modifier = Modifier.height(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(12.dp))

            // Título

            Text(
                "Tu amiga en la crisis financiera",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                modifier = Modifier.offset(y = (-20).dp)
                )


            Spacer(Modifier.height(40.dp))

            // ── EMAIL ──
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TextoSecundario) },
                leadingIcon = { Icon(Icons.Rounded.Email, null, tint = TextoSecundario) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoPrimario,
                    unfocusedTextColor = TextoPrimario,
                    focusedBorderColor = VerdePrimario,
                    unfocusedBorderColor = Divisor,
                    cursorColor = VerdePrimario
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── CONTRASEÑA ──
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = TextoSecundario) },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoPrimario,
                    unfocusedTextColor = TextoPrimario,
                    focusedBorderColor = VerdePrimario,
                    unfocusedBorderColor = Divisor,
                    cursorColor = VerdePrimario
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── BOTÓN PRINCIPAL ──
            Button(
                onClick = {
                    if (esRegistro) viewModel.registrarConEmail(email, password)
                    else viewModel.loginConEmail(email, password)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
                enabled = email.isNotBlank() && password.length >= 6 && uiState !is AuthUiState.Cargando
            ) {
                if (uiState is AuthUiState.Cargando) {
                    CircularProgressIndicator(color = FondoNegro, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (esRegistro) "Crear cuenta" else "Iniciar sesión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── DIVISOR ──
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(Modifier.weight(1f), color = Divisor)
                Text("  o  ", color = TextoMuted, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(Modifier.weight(1f), color = Divisor)
            }

            Spacer(Modifier.height(16.dp))

            // ── GOOGLE SIGN-IN ──
            OutlinedButton(
                onClick = { viewModel.loginConGoogle() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Divisor),
                enabled = uiState !is AuthUiState.Cargando
            ) {
                Icon(Icons.Rounded.Person, null, tint = TextoPrimario, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Continuar con Google", color = TextoPrimario, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))

            // ── TOGGLE LOGIN / REGISTRO ──
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (esRegistro) "¿Ya tenés cuenta? " else "¿No tenés cuenta? ",
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    if (esRegistro) "Iniciá sesión" else "Registrate",
                    color = VerdePrimario,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        esRegistro = !esRegistro
                        viewModel.resetEstado()
                    }
                )
            }

            // ── ERROR ──
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
        }
    }
}
