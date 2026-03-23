package com.candlelabs.gestionpersonal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = FondoNegro,
    primaryContainer = VerdeOscuro,
    onPrimaryContainer = VerdePrimario,

    secondary = TextoSecundario,
    onSecondary = FondoNegro,

    background = FondoPrincipal,
    onBackground = TextoSobreCreme,

    surface = FondoPrincipal,
    onSurface = TextoSobreCreme,
    onSurfaceVariant = TextoSecundario,

    tertiary = Lima40,
    error = RojoGasto
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
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}