package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Rutas {
    const val SPLASH     = "splash"
    const val ONBOARDING = "onboarding"
    const val MAIN       = "main"
    const val DOLAR      = "dolar"
    const val ALQUILER   = "alquiler"
    const val HISTORIAL  = "historial"
    const val INFO       = "info"
    const val HOME       = "home"

    const val DOLAR_DETALLE = "dolar_detalle/{casa}/{nombre}"
    fun dolarDetalleRuta(casa: String, nombre: String) = "dolar_detalle/$casa/$nombre"
}

@Composable
fun NavGraph(navController: NavHostController) {

    // Leemos SharedPreferences acá para decidir el destino después del splash
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
    val nombre = prefs.getString("nombre_usuario", null)

    // Si tiene nombre guardado va a MAIN, sino a ONBOARDING
    // Equivalente en Python: destino = "main" if nombre else "onboarding"
    val destinoPosSplash = if (nombre != null) Rutas.MAIN else Rutas.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH
    ) {

        composable(Rutas.SPLASH) {
            SplashScreen(
                onSplashTerminado = {
                    // Ahora usa el destino correcto según si tiene nombre o no
                    navController.navigate(destinoPosSplash) {
                        popUpTo(Rutas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ONBOARDING) {
            OnboardingScreen(
                onNombreGuardado = {
                    navController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.MAIN) {
            MainScreen()
        }
    }
}