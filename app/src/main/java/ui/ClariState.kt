package com.candlelabs.gestionpersonal.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// ═══════════════════════════════════════════════════════════
// ESTADOS DE CLARI
// IDLE          → latido suave, ícono C
// TOUCH_DOWN    → compresión leve
// HOLD          → ondas circulares + ícono GastoExpress + vibra
// CONFIRMED     → soltó tras hold → abre GastoExpress
// CANCELLED     → soltó sin hold → rebote
// POST_FEEDBACK → gasto guardado
// ASISTENTE_OPEN → scale+glow fijos en chat
// EXPRESS_OPEN  → dentro de GastoExpress
// ═══════════════════════════════════════════════════════════
enum class ClariState {
    IDLE, TOUCH_DOWN, HOLD, CONFIRMED, CANCELLED,
    POST_FEEDBACK, ASISTENTE_OPEN, EXPRESS_OPEN
}

// ═══════════════════════════════════════════════════════════
// HAPTICS
// Requiere: <uses-permission android:name="android.permission.VIBRATE"/>
// ═══════════════════════════════════════════════════════════
fun vibrar(context: Context, tipo: String) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(when (tipo) {
                "light"  -> VibrationEffect.createOneShot(25, 80)
                "medium" -> VibrationEffect.createOneShot(45, 160)
                "heavy"  -> VibrationEffect.createOneShot(70, 255)
                else     -> VibrationEffect.createOneShot(25, 80)
            })
        }
    } catch (_: Exception) {}
}
