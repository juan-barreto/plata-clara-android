package com.candlelabs.gestionpersonal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Solo usamos LightColorScheme — la app siempre es modo claro
// igual que Mercado Pago, no importa si el celu está en modo oscuro
private val LightColorScheme = lightColorScheme(
    primary = Verde40,
    secondary = Gris40,
    tertiary = Lima40
)

@Composable
fun GestionPersonalARGTheme(
    content: @Composable () -> Unit
) {
    // Status bar transparente para que el #1E1E1E del header se vea bien
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            // íconos de la status bar en claro (para fondo oscuro)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}