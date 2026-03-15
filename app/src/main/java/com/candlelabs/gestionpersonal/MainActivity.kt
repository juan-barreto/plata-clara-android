package com.candlelabs.gestionpersonal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.candlelabs.gestionpersonal.ui.DolarScreen
import com.candlelabs.gestionpersonal.ui.theme.GestionPersonalARGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionPersonalARGTheme {
                // Scaffold es el contenedor base de Material Design
                // maneja el padding del sistema (barra de estado, navegación)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Todo el trabajo lo hace DolarScreen
                    // Le pasamos el padding para que respete las barras del sistema
                    DolarScreen(paddingValues = innerPadding)
                }
            }
        }
    }
}