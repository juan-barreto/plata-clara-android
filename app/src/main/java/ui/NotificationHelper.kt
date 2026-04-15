package com.candlelabs.gestionpersonal.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object NotificationHelper {

    private const val REQUEST_CODE = 2001

    // ── Programar alarma diaria a la hora elegida ──────────────────────
    // hora: 0-23, minuto: 0-59
    fun programarAlarma(context: Context, hora: Int, minuto: Int) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // En Android 12+ necesitamos verificar que tenemos permiso para alarmas exactas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculamos la próxima ocurrencia de esa hora
        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Si la hora ya pasó hoy, la programamos para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // ALARM_RTC_WAKEUP: despierta el dispositivo aunque esté en reposo
        // setRepeating: se repite cada 24 horas automáticamente
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendario.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    // ── Cancelar la alarma ─────────────────────────────────────────────
    fun cancelarAlarma(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
