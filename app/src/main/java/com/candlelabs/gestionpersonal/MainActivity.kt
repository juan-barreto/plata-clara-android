package com.candlelabs.gestionpersonal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.candlelabs.gestionpersonal.ui.NavGraph
import com.candlelabs.gestionpersonal.ui.theme.GestionPersonalARGTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        setContent {
            GestionPersonalARGTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}