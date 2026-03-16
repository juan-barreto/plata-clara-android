package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Las rutas como constantes — así no usamos strings sueltos por todo el código
// Si mañana cambiás el nombre de una ruta, lo cambiás acá y listo
object Rutas {
    const val DOLAR = "dolar"
    const val ALQUILER = "alquiler"
    const val HISTORIAL = "historial"

    const val INFO = "info"
}

// El mapa completo de navegación
// navController: el que maneja el historial de pantallas
// paddingValues: el padding del Scaffold para que nada quede tapado
@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.DOLAR, // pantalla inicial
        modifier = Modifier.padding(paddingValues)
    ) {
        // Cada "composable" registra una pantalla en el mapa
        composable(Rutas.DOLAR) {
            DolarScreen()
        }
        composable(Rutas.ALQUILER) {
            AlquilerScreen(navController = navController)
        }
        composable(Rutas.HISTORIAL) {
            HistorialScreen()
        }
        composable(Rutas.INFO) {
            InfoScreen()
        }
    }
}