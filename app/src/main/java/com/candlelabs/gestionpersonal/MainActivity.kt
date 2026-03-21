package com.candlelabs.gestionpersonal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.candlelabs.gestionpersonal.ui.NavGraph
import com.candlelabs.gestionpersonal.ui.theme.GestionPersonalARGTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // ← sacamos la barra de título
        setContent {
            GestionPersonalARGTheme {
                // Solo crea el navController y llama al mapa
                // Todo lo demás (Scaffold, bottom bar) se fue a MainScreen
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}