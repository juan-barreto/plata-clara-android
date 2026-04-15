package com.candlelabs.gestionpersonal.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.candlelabs.gestionpersonal.MainActivity
import com.candlelabs.gestionpersonal.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // ── Leer el userId guardado en prefs globales ──────────────────
        // Lo guardamos cuando el usuario inicia sesión, igual que en el resto de la app
        val prefsGlobal = context.getSharedPreferences("plata_clara_prefs", Context.MODE_PRIVATE)
        val userId = prefsGlobal.getString("user_id", null) ?: return // si no hay sesión, no mostramos nada

        // ── Leer el balance del usuario desde sus prefs propias ────────
        val prefsUsuario = context.getSharedPreferences("plata_clara_$userId", Context.MODE_PRIVATE)
        val balance = prefsUsuario.getLong("balance_disponible", -1L)

        // ── Armar el mensaje según el balance ──────────────────────────
        val mensaje = when {
            balance < 0 -> "Revisá cómo va tu plata este mes"
            balance == 0L -> "Ya gastaste todo tu presupuesto este mes"
            else -> "Te quedan $${"%,d".format(balance).replace(",", ".")} para gastar este mes"
        }

        // ── Crear el canal de notificaciones (requerido en Android 8+) ─
        val canalId = "plata_clara_recordatorio"
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Recordatorio diario",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorio diario de tu saldo disponible"
            }
            notifManager.createNotificationChannel(canal)
        }

        // ── Intent para abrir la app al tocar la notificación ─────────
        val intentAbrirApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentAbrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Construir y mostrar la notificación ───────────────────────
        val notificacion = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.mipmap.ic_launcher_round) // icono de la app
            .setContentTitle("Plata Clara")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // desaparece al tocarla
            .build()

        notifManager.notify(1001, notificacion)
    }
}
