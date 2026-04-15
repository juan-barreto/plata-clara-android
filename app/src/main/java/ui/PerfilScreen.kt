package com.candlelabs.gestionpersonal.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// Opciones de horario disponibles para el recordatorio
enum class HorarioNotificacion(val label: String, val sublabel: String, val hora: Int, val minuto: Int) {
    MANANA("Mañana", "9:00 am", 9, 0),
    TARDE("Tarde", "6:00 pm", 18, 0)
}

@Composable
fun PerfilScreen(onCerrarSesion: () -> Unit) {

    val context = LocalContext.current
    val usuario = try { SupabaseClient.instance.auth.currentUserOrNull() } catch (e: Exception) { null }
    val userId  = usuario?.id ?: "guest"
    val email   = usuario?.email ?: "Sin email"
    val nombre  = try {
        usuario?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: ""
    } catch (e: Exception) { "" }
    val avatarInicial  = if (nombre.isNotBlank()) nombre.first().uppercase() else email.first().uppercase()
    val emailVerificado = try { usuario?.emailConfirmedAt != null } catch (e: Exception) { false }

    // ── SharedPreferences del usuario para notificaciones ──────────────
    val prefs = context.getSharedPreferences("plata_clara_$userId", Context.MODE_PRIVATE)

    // ── Estado de notificaciones leído desde prefs ─────────────────────
    var notificacionesActivas by remember {
        mutableStateOf(prefs.getBoolean("notif_activa", false))
    }
    var horarioSeleccionado by remember {
        mutableStateOf(
            HorarioNotificacion.entries.firstOrNull {
                it.name == prefs.getString("notif_horario", HorarioNotificacion.MANANA.name)
            } ?: HorarioNotificacion.MANANA
        )
    }

    // ── Permiso POST_NOTIFICATIONS (Android 13+) ───────────────────────
    val tienePermiso = remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            } else true // en versiones anteriores no se necesita permiso explícito
        )
    }
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso.value = concedido
        if (concedido) {
            // Si el usuario concedió el permiso, activamos la alarma
            NotificationHelper.programarAlarma(context, horarioSeleccionado.hora, horarioSeleccionado.minuto)
            prefs.edit().putBoolean("notif_activa", true).apply()
            notificacionesActivas = true
        }
    }

    // ── Función para activar/desactivar notificaciones ─────────────────
    fun toggleNotificaciones(activar: Boolean) {
        if (activar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !tienePermiso.value) {
                // Pedir permiso — el resultado lo maneja el launcher de arriba
                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper.programarAlarma(context, horarioSeleccionado.hora, horarioSeleccionado.minuto)
                prefs.edit().putBoolean("notif_activa", true).apply()
                notificacionesActivas = true
            }
        } else {
            NotificationHelper.cancelarAlarma(context)
            prefs.edit().putBoolean("notif_activa", false).apply()
            notificacionesActivas = false
        }
    }

    // ── Función para cambiar horario ───────────────────────────────────
    fun cambiarHorario(nuevo: HorarioNotificacion) {
        horarioSeleccionado = nuevo
        prefs.edit().putString("notif_horario", nuevo.name).apply()
        // Si ya estaba activa, reprogramamos con el nuevo horario
        if (notificacionesActivas) {
            NotificationHelper.programarAlarma(context, nuevo.hora, nuevo.minuto)
        }
    }

    var mostrarConfirmLogout by remember { mutableStateOf(false) }
    var enviandoVerificacion by remember { mutableStateOf(false) }
    var verificacionEnviada  by remember { mutableStateOf(false) }
    var errorVerificacion    by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Diálogo cerrar sesión
    if (mostrarConfirmLogout) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmLogout = false },
            title = { Text("Cerrar sesión", color = TextoPrimario) },
            text  = { Text("¿Seguro que querés cerrar sesión?", color = TextoSecundario) },
            containerColor = FondoCard,
            confirmButton = {
                TextButton(onClick = { mostrarConfirmLogout = false; onCerrarSesion() }) {
                    Text("Cerrar sesión", color = RojoGasto)
                }
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
            .background(FondoNegro)
            .verticalScroll(rememberScrollState()) // scroll por si el contenido es largo
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Perfil",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // ── AVATAR ────────────────────────────────────────────────────
        Surface(shape = CircleShape, color = VerdePrimario, modifier = Modifier.size(90.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(avatarInicial, style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold, color = FondoNegro)
            }
        }
        Spacer(Modifier.height(16.dp))

        if (nombre.isNotBlank()) {
            Text(nombre, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = TextoPrimario)
            Spacer(Modifier.height(4.dp))
        }
        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
        Spacer(Modifier.height(32.dp))

        // ── CARD INFO ─────────────────────────────────────────────────
        Card(
            Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
            shape  = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Email + verificación
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Email, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text("EMAIL", style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                    if (emailVerificado) {
                        Surface(shape = RoundedCornerShape(8.dp), color = VerdePrimario.copy(alpha = 0.15f)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

                // Método de acceso
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Security, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("MÉTODO DE ACCESO", style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text(
                            if (nombre.isNotBlank() && usuario?.appMetadata?.get("provider")?.toString()?.contains("google") == true)
                                "Google" else "Email y contraseña",
                            style = MaterialTheme.typography.bodyMedium, color = TextoPrimario
                        )
                    }
                }

                HorizontalDivider(color = Divisor)

                // Versión
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Info, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                    Column {
                        Text("VERSIÓN", style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario, letterSpacing = 0.8.sp)
                        Text("Plata Clara v2.0", style = MaterialTheme.typography.bodyMedium, color = TextoPrimario)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── CARD CONFIGURACIÓN ────────────────────────────────────────
        Card(
            Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = SombraCard, spotColor = SombraCard),
            shape  = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = FondoCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("CONFIGURACIÓN", style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario, letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 16.dp))

                // ── Toggle recordatorio diario ─────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Notifications, null, tint = VerdePrimario, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Recordatorio diario", style = MaterialTheme.typography.bodyMedium,
                                color = TextoPrimario, fontWeight = FontWeight.Medium)
                            Text("Revisá tu plata una vez al día",
                                style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                        }
                    }
                    Switch(
                        checked = notificacionesActivas,
                        onCheckedChange = { toggleNotificaciones(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor   = FondoNegro,
                            checkedTrackColor   = VerdePrimario,
                            uncheckedThumbColor = TextoSecundario,
                            uncheckedTrackColor = BordeCard
                        )
                    )
                }

                // ── Selector de horario (solo visible si está activo) ──
                if (notificacionesActivas) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Divisor)
                    Spacer(Modifier.height(16.dp))

                    Text("¿Cuándo querés el recordatorio?",
                        style = MaterialTheme.typography.bodySmall, color = TextoSecundario,
                        modifier = Modifier.padding(bottom = 12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorarioNotificacion.entries.forEach { horario ->
                            val seleccionado = horarioSeleccionado == horario
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (seleccionado) VerdePrimario else FondoNegro)
                                    .border(1.dp, if (seleccionado) VerdePrimario else BordeCard, RoundedCornerShape(12.dp))
                                    .clickable { cambiarHorario(horario) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        horario.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (seleccionado) FondoNegro else TextoPrimario
                                    )
                                    Text(
                                        horario.sublabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (seleccionado) FondoNegro.copy(alpha = 0.6f) else TextoSecundario
                                    )
                                }
                            }
                        }
                    }

                    // ── Preview de la notificación ─────────────────────
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Así va a aparecer la notificación",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        color  = FondoNegro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BordeCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = VerdePrimario,
                                modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Notifications, null,
                                        tint = FondoNegro, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text("Plata Clara", style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold, color = TextoPrimario)
                                Text("Te quedan $180.000 para gastar hoy",
                                    style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── BOTÓN VERIFICAR EMAIL (solo si no está verificado) ────────
        if (!emailVerificado) {
            Button(
                onClick = {
                    scope.launch {
                        enviandoVerificacion = true
                        errorVerificacion = null
                        try {
                            SupabaseClient.instance.auth.resendEmail(
                                type  = io.github.jan.supabase.auth.OtpType.Email.SIGNUP,
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
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = VerdePrimario, contentColor = FondoNegro),
                enabled  = !enviandoVerificacion
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

            if (verificacionEnviada) {
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdePrimario.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = VerdePrimario, modifier = Modifier.size(16.dp))
                        Text("Revisá tu bandeja de entrada", color = VerdePrimario,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

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

            Spacer(Modifier.height(16.dp))
        }

        // ── CERRAR SESIÓN ─────────────────────────────────────────────
        Button(
            onClick  = { mostrarConfirmLogout = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = RojoGasto.copy(alpha = 0.1f),
                contentColor   = RojoGasto
            )
        ) {
            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(80.dp))
    }
}