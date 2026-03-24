package com.candlelabs.gestionpersonal.ui

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(onCerrarSesion: () -> Unit) {

    val usuario = try { SupabaseClient.instance.auth.currentUserOrNull() } catch (e: Exception) { null }
    val email = usuario?.email ?: "Sin email"
    val nombre = try {
        usuario?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: ""
    } catch (e: Exception) { "" }
    val avatarInicial = if (nombre.isNotBlank()) nombre.first().uppercase() else email.first().uppercase()

    // Verificar si el email está confirmado
    // Supabase guarda la fecha de confirmación en email_confirmed_at
    val emailVerificado = try {
        usuario?.emailConfirmedAt != null
    } catch (e: Exception) { false }

    var mostrarConfirmLogout by remember { mutableStateOf(false) }
    var enviandoVerificacion by remember { mutableStateOf(false) }
    var verificacionEnviada by remember { mutableStateOf(false) }
    var errorVerificacion by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Diálogo cerrar sesión
    if (mostrarConfirmLogout) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmLogout = false },
            title = { Text("Cerrar sesión", color = TextoPrimario) },
            text = { Text("¿Seguro que querés cerrar sesión?", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = { mostrarConfirmLogout = false; onCerrarSesion() }) {
                    Text("Cerrar sesión", color = RojoGasto)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmLogout = false }) { Text("Cancelar", color = TextoSecundario) }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(FondoPrincipal).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = TextoSobreCreme, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))

        // Avatar
        Surface(shape = CircleShape, color = VerdePrimario, modifier = Modifier.size(90.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(avatarInicial, style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold, color = FondoNegro)
            }
        }
        Spacer(Modifier.height(16.dp))

        if (nombre.isNotBlank()) {
            Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoSobreCreme)
            Spacer(Modifier.height(4.dp))
        }
        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)

        Spacer(Modifier.height(32.dp))

        // ── CARD INFO ──
        Card(
            Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Email + estado de verificación
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Email, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text("EMAIL", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                    // Badge de verificación
                    if (emailVerificado) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VerdePrimario.copy(alpha = 0.15f)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Rounded.Verified, null, tint = VerdePrimario, modifier = Modifier.size(14.dp))
                                Text("Verificado", style = MaterialTheme.typography.labelSmall, color = VerdePrimario)
                            }
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(8.dp), color = Naranja.copy(alpha = 0.15f)) {
                            Text("Sin verificar", style = MaterialTheme.typography.labelSmall,
                                color = Naranja, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                HorizontalDivider(color = Divisor)

                // Proveedor
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Security, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("MÉTODO DE ACCESO", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text(if (nombre.isNotBlank() && usuario?.appMetadata?.get("provider")?.toString()?.contains("google") == true) "Google" else "Email y contraseña",
                            style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                }

                HorizontalDivider(color = Divisor)

                // Versión
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Info, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("VERSIÓN", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text("Plata Clara v1.0", style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── BOTÓN VERIFICAR EMAIL (solo si no está verificado) ──
        if (!emailVerificado) {
            Button(
                onClick = {
                    scope.launch {
                        enviandoVerificacion = true
                        errorVerificacion = null
                        try {
                            SupabaseClient.instance.auth.resendEmail(
                                type = io.github.jan.supabase.auth.OtpType.Email.SIGNUP,
                                email = email
                            )
                            verificacionEnviada = true
                        } catch (e: Exception) {
                            errorVerificacion = e.message ?: "Error al enviar"
                        }
                        enviandoVerificacion = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
                enabled = !enviandoVerificacion
            ) {
                if (enviandoVerificacion) {
                    CircularProgressIndicator(color = FondoNegro, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.MarkEmailRead, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (verificacionEnviada) "Reenviar verificación" else "Verificar email",
                        fontWeight = FontWeight.Bold)
                }
            }

            // Mensaje de éxito
            if (verificacionEnviada) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdePrimario.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(16.dp))
                        Text("Revisá tu bandeja de entrada", color = VerdePrimario, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Mensaje de error
            errorVerificacion?.let { err ->
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RojoGasto.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = RojoGasto, modifier = Modifier.size(16.dp))
                        Text(err, color = RojoGasto, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── CERRAR SESIÓN ──
        Button(
            onClick = { mostrarConfirmLogout = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RojoGasto.copy(alpha = 0.1f), contentColor = RojoGasto)
        ) {
            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(80.dp))
    }
}