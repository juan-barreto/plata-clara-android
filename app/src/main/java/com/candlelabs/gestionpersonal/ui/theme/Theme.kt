package com.candlelabs.gestionpersonal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Verde40,              // #00B872 — verde Plata Clara
    onPrimary = Negro,              // texto sobre verde → negro
    primaryContainer = VerdeOscuro, // fondo de íconos activos → verde oscuro
    onPrimaryContainer = Verde40,   // texto sobre container verde

    secondary = Gris80,             // gris medio — textos secundarios
    onSecondary = Negro,

    background = Gris40,            // #F8F2F2 — fondo crema de toda la app
    onBackground = Negro,           // texto sobre crema → negro

    surface = Gris40,               // mismo que background
    onSurface = Negro,
    onSurfaceVariant = Gris80,      // textos secundarios sobre superficie

    tertiary = Lima40,
    error = RojoError               // #FF0034 — rojo YouTube
)

@Composable
fun GestionPersonalARGTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            // íconos de la status bar en claro — para fondo oscuro del header
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}