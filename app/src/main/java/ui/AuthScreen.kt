package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.compose.foundation.Image
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
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth

@Composable
fun AuthScreen(onAuthExitoso: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(context))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.verificarSesion() }
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Exito) onAuthExitoso()
    }

    var pantalla by remember { mutableStateOf("login") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.RegistroExitoso) pantalla = "ingreso"
        if (uiState is AuthUiState.EmailConfirmacionPendiente) pantalla = "confirmacion"
    }

    when (pantalla) {
        "login" -> PantallaLogin(
            viewModel = viewModel, uiState = uiState,
            onIrARegistro = { pantalla = "registro"; viewModel.resetEstado() },
            onIrARecuperar = { pantalla = "recuperar"; viewModel.resetEstado() }
        )
        "registro" -> PantallaRegistro(
            viewModel = viewModel, uiState = uiState, context = context,
            onIrALogin = { pantalla = "login"; viewModel.resetEstado() }
        )
        "recuperar" -> PantallaRecuperar(
            viewModel = viewModel, uiState = uiState,
            onVolver = { pantalla = "login"; viewModel.resetEstado() }
        )
        "confirmacion" -> PantallaConfirmacion(
            onVolver = { pantalla = "login"; viewModel.resetEstado() }
        )
        "ingreso" -> PantallaIngreso(
            onOmitir = { viewModel.completarSetup() },
            onGuardar = { tipo, monto ->
                // Guardamos con userId para aislar datos por usuario
                val userId = SupabaseClient.instance.auth.currentUserOrNull()?.id ?: "anonimo"
                context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE).edit()
                    .putString("tipo_ingreso_$userId", tipo)
                    .putFloat("ingreso_${userId}_principal", monto.toFloat())
                    .apply()
                viewModel.guardarIngresoBase(tipo, monto)
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA LOGIN
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaLogin(
    viewModel: AuthViewModel, uiState: AuthUiState,
    onIrARegistro: () -> Unit, onIrARecuperar: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(FondoNegro)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp).padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.logo_plata_clara),
                contentDescription = "Plata Clara", modifier = Modifier.height(250.dp), contentScale = ContentScale.Fit)
            Text("Tu amiga en la crisis financiera", style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario, modifier = Modifier.offset(y = (-45).dp))

            Spacer(Modifier.height(20.dp))

            CampoTexto(value = email, onValueChange = { email = it }, label = "Email",
                icon = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            CampoPassword(value = password, onValueChange = { password = it },
                mostrar = mostrarPassword, onToggle = { mostrarPassword = !mostrarPassword }, label = "Contraseña")

            Text("¿Olvidaste tu contraseña?", color = VerdePrimario, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp, bottom = 16.dp).clickable { onIrARecuperar() })

            BotonPrincipal(texto = "Iniciar sesión", cargando = uiState is AuthUiState.Cargando,
                habilitado = email.isNotBlank() && password.length >= 6,
                onClick = { viewModel.loginConEmail(email, password) })

            Spacer(Modifier.height(16.dp)); Divisor(); Spacer(Modifier.height(16.dp))

            BotonGoogle(cargando = uiState is AuthUiState.Cargando, onClick = { viewModel.loginConGoogle() })

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("¿No tenés cuenta? ", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
                Text("Registrate", color = VerdePrimario, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onIrARegistro() })
            }
            MensajeError(uiState)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA REGISTRO — con repetir contraseña
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaRegistro(
    viewModel: AuthViewModel, uiState: AuthUiState,
    context: Context, onIrALogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repetirPassword by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarRepetir by remember { mutableStateOf(false) }

    val passwordsCoinciden = password == repetirPassword
    val passwordLarga = password.length >= 6
    val formularioValido = nombre.isNotBlank() && email.isNotBlank() && passwordLarga && passwordsCoinciden

    Box(Modifier.fillMaxSize().background(FondoNegro)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp).padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.logo_plata_clara),
                contentDescription = "Plata Clara", modifier = Modifier.height(200.dp), contentScale = ContentScale.Fit)
            Text("Creá tu cuenta", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = TextoPrimario)
            Spacer(Modifier.height(4.dp))
            Text("Empezá a controlar tus finanzas", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)

            Spacer(Modifier.height(24.dp))

            CampoTexto(value = nombre, onValueChange = { nombre = it }, label = "Nombre",
                icon = Icons.Rounded.Person, keyboardType = KeyboardType.Text)
            Spacer(Modifier.height(12.dp))

            CampoTexto(value = email, onValueChange = { email = it }, label = "Email",
                icon = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))

            CampoPassword(value = password, onValueChange = { password = it },
                mostrar = mostrarPassword, onToggle = { mostrarPassword = !mostrarPassword }, label = "Contraseña")

            if (password.isNotEmpty() && !passwordLarga) {
                Text("Mínimo 6 caracteres", style = MaterialTheme.typography.bodySmall,
                    color = RojoGasto, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
            }

            Spacer(Modifier.height(12.dp))

            CampoPassword(value = repetirPassword, onValueChange = { repetirPassword = it },
                mostrar = mostrarRepetir, onToggle = { mostrarRepetir = !mostrarRepetir }, label = "Repetir contraseña")

            if (repetirPassword.isNotEmpty() && !passwordsCoinciden) {
                Text("Las contraseñas no coinciden", style = MaterialTheme.typography.bodySmall,
                    color = RojoGasto, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
            }
            if (repetirPassword.isNotEmpty() && passwordsCoinciden && passwordLarga) {
                Row(Modifier.align(Alignment.Start).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(14.dp))
                    Text("Las contraseñas coinciden", style = MaterialTheme.typography.bodySmall, color = VerdePrimario)
                }
            }

            Spacer(Modifier.height(16.dp))

            BotonPrincipal(
                texto = "Crear cuenta",
                cargando = uiState is AuthUiState.Cargando,
                habilitado = formularioValido,
                onClick = {
                    context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE).edit()
                        .putString("nombre_usuario", nombre)
                        .apply()
                    viewModel.registrarConEmail(email, password, nombre)
                }
            )

            Spacer(Modifier.height(16.dp)); Divisor(); Spacer(Modifier.height(16.dp))

            BotonGoogle(cargando = uiState is AuthUiState.Cargando, onClick = { viewModel.loginConGoogle() })

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("¿Ya tenés cuenta? ", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
                Text("Iniciá sesión", color = VerdePrimario, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onIrALogin() })
            }
            MensajeError(uiState)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA CONFIRMACIÓN EMAIL
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaConfirmacion(onVolver: () -> Unit) {
    Box(Modifier.fillMaxSize().background(FondoNegro)) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.MarkEmailRead, null, tint = VerdePrimario, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(24.dp))
            Text("Revisá tu email", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = TextoPrimario)
            Spacer(Modifier.height(12.dp))
            Text(
                "Te mandamos un link de confirmación. Una vez que confirmes tu cuenta podés iniciar sesión.",
                style = MaterialTheme.typography.bodyMedium, color = TextoSecundario,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            BotonPrincipal(texto = "Ir al login", cargando = false, habilitado = true, onClick = onVolver)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA RECUPERAR CONTRASEÑA
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaRecuperar(
    viewModel: AuthViewModel, uiState: AuthUiState, onVolver: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(FondoNegro)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.LockReset, null, tint = VerdePrimario, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Recuperar contraseña", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = TextoPrimario)
            Spacer(Modifier.height(8.dp))
            Text("Te vamos a mandar un email con un link para crear una nueva contraseña.",
                style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))

            CampoTexto(value = email, onValueChange = { email = it }, label = "Email de tu cuenta",
                icon = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(20.dp))

            if (uiState is AuthUiState.ResetEnviado) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdePrimario.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(20.dp))
                        Text("Revisá tu bandeja de entrada. Si no lo ves, revisá spam.",
                            color = VerdePrimario, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            BotonPrincipal(
                texto = if (uiState is AuthUiState.ResetEnviado) "Reenviar email" else "Enviar email de recuperación",
                cargando = uiState is AuthUiState.Cargando, habilitado = email.isNotBlank(),
                onClick = { viewModel.recuperarPassword(email) })
            Spacer(Modifier.height(24.dp))
            Text("Volver al login", color = VerdePrimario, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onVolver() })
            MensajeError(uiState)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PANTALLA INGRESO — post-registro
// ═══════════════════════════════════════════════════════════
@Composable
private fun PantallaIngreso(onOmitir: () -> Unit, onGuardar: (String, Double) -> Unit) {
    var tipoIngreso by remember { mutableStateOf("Sueldo") }
    var monto by remember { mutableStateOf("") }

    val tipos = listOf("Sueldo", "Freelance", "Negocio propio", "Changas", "Jubilación", "Otro")

    Box(Modifier.fillMaxSize().background(FondoNegro)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.AccountBalanceWallet, null, tint = VerdePrimario, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("¿Cuánto ganás por mes?", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = TextoPrimario)
            Spacer(Modifier.height(8.dp))
            Text("Esto nos ayuda a distribuir tu presupuesto. Podés cambiarlo después.",
                style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))

            Text("TIPO DE INGRESO", style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                tipos.take(3).forEach { tipo ->
                    FilterChip(selected = tipoIngreso == tipo, onClick = { tipoIngreso = tipo },
                        label = { Text(tipo, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VerdePrimario, selectedLabelColor = FondoNegro))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                tipos.drop(3).forEach { tipo ->
                    FilterChip(selected = tipoIngreso == tipo, onClick = { tipoIngreso = tipo },
                        label = { Text(tipo, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VerdePrimario, selectedLabelColor = FondoNegro))
                }
            }
            Spacer(Modifier.height(20.dp))

            Text("MONTO MENSUAL", style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))
            OutlinedTextField(
                value = monto, onValueChange = { monto = it.filter { c -> c.isDigit() } },
                label = { Text("Ingreso mensual", color = TextoSecundario) },
                prefix = { Text("$", color = VerdePrimario, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                leadingIcon = { Icon(Icons.Rounded.AttachMoney, null, tint = TextoSecundario) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
                    focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor, cursorColor = VerdePrimario),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text("Podés agregar más fuentes de ingreso después desde el presupuesto.",
                style = MaterialTheme.typography.bodySmall, color = TextoMuted)
            Spacer(Modifier.height(28.dp))

            Button(onClick = {
                val m = monto.toDoubleOrNull()
                if (m != null && m > 0) onGuardar(tipoIngreso, m)
            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
                enabled = monto.isNotBlank() && (monto.toDoubleOrNull() ?: 0.0) > 0) {
                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar y continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onOmitir, modifier = Modifier.fillMaxWidth()) {
                Text("Omitir por ahora", color = TextoSecundario)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// COMPONENTES REUTILIZABLES
// ═══════════════════════════════════════════════════════════
@Composable
private fun CampoTexto(value: String, onValueChange: (String) -> Unit, label: String,
                       icon: androidx.compose.ui.graphics.vector.ImageVector, keyboardType: KeyboardType) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextoSecundario) },
        leadingIcon = { Icon(icon, null, tint = TextoSecundario) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
            focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor, cursorColor = VerdePrimario),
        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun CampoPassword(value: String, onValueChange: (String) -> Unit,
                          mostrar: Boolean, onToggle: () -> Unit, label: String) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextoSecundario) },
        leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = TextoSecundario) },
        trailingIcon = { IconButton(onClick = onToggle) {
            Icon(if (mostrar) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextoSecundario)
        }},
        visualTransformation = if (mostrar) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextoPrimario, unfocusedTextColor = TextoPrimario,
            focusedBorderColor = VerdePrimario, unfocusedBorderColor = Divisor, cursorColor = VerdePrimario),
        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun BotonPrincipal(texto: String, cargando: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
        enabled = habilitado && !cargando) {
        if (cargando) CircularProgressIndicator(color = FondoNegro, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        else Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun BotonGoogle(cargando: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Divisor), enabled = !cargando) {
        Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google", modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Continuar con Google", color = TextoPrimario, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Divisor() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = com.candlelabs.gestionpersonal.ui.theme.Divisor)
        Text("  o  ", color = TextoMuted, style = MaterialTheme.typography.bodySmall)
        HorizontalDivider(Modifier.weight(1f), color = com.candlelabs.gestionpersonal.ui.theme.Divisor)
    }
}

@Composable
private fun MensajeError(uiState: AuthUiState) {
    if (uiState is AuthUiState.Error) {
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RojoGasto.copy(alpha = 0.1f))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = RojoGasto, modifier = Modifier.size(18.dp))
                Text(uiState.mensaje, color = RojoGasto, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}