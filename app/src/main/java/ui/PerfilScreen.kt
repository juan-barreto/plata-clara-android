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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth

@Composable
fun PerfilScreen(
    onCerrarSesion: () -> Unit
) {
    // Datos del usuario logueado
    val usuario = try {
        SupabaseClient.instance.auth.currentUserOrNull()
    } catch (e: Exception) { null }

    val email = usuario?.email ?: "Sin email"
    // Google provee el nombre en user_metadata
    val nombre = try {
        usuario?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: ""
    } catch (e: Exception) { "" }

    val avatarInicial = if (nombre.isNotBlank()) nombre.first().uppercase() else email.first().uppercase()

    var mostrarConfirmLogout by remember { mutableStateOf(false) }

    // Diálogo confirmar cerrar sesión
    if (mostrarConfirmLogout) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmLogout = false },
            title = { Text("Cerrar sesión", color = TextoPrimario) },
            text = { Text("¿Seguro que querés cerrar sesión?", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmLogout = false
                    onCerrarSesion()
                }) { Text("Cerrar sesión", color = RojoGasto) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmLogout = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPrincipal)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            "Perfil",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextoSobreCreme,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // Avatar
        Surface(
            shape = CircleShape,
            color = VerdePrimario,
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    avatarInicial,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = FondoNegro
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Nombre (si viene de Google)
        if (nombre.isNotBlank()) {
            Text(
                nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoSobreCreme
            )
            Spacer(Modifier.height(4.dp))
        }

        // Email
        Text(
            email,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )

        Spacer(Modifier.height(32.dp))

        // ── CARDS DE INFO ──
        Card(
            Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Email row
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Email, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("EMAIL", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                }

                HorizontalDivider(color = Divisor)

                // Proveedor
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Security, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("MÉTODO DE ACCESO", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text(
                            if (nombre.isNotBlank()) "Google" else "Email y contraseña",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoPrimario
                        )
                    }
                }

                HorizontalDivider(color = Divisor)

                // Versión app
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Info, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("VERSIÓN", style = MaterialTheme.typography.labelSmall, color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text("Plata Clara v1.0", style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── BOTÓN CERRAR SESIÓN ──
        Button(
            onClick = { mostrarConfirmLogout = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RojoGasto.copy(alpha = 0.1f),
                contentColor = RojoGasto
            )
        ) {
            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(80.dp)) // espacio para el navbar
    }
}
