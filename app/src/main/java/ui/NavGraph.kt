package com.candlelabs.gestionpersonal.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Rutas actualizadas — agregamos SPLASH y MAIN
object Rutas {
    const val SPLASH    = "splash"   // nueva — pantalla de bienvenida
    const val MAIN      = "main"     // nueva — contenedor con bottom bar
    const val DOLAR     = "dolar"
    const val ALQUILER  = "alquiler"
    const val HISTORIAL = "historial"
    const val INFO      = "info"
}

// El mapa EXTERNO — solo conoce Splash y Main
// Ya no recibe paddingValues porque el Scaffold se fue a MainScreen
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH  // arranca en splash
    ) {

        // Pantalla splash — sin bottom bar, sin padding
        composable(Rutas.SPLASH) {
            SplashScreen(
                // cuando el splash termina, navega a MAIN
                // y saca el SPLASH del historial para que el botón atrás no vuelva ahí
                onSplashTerminado = {
                    navController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla principal — tiene su propio Scaffold adentro
        composable(Rutas.MAIN) {
            MainScreen()
        }
    }
}