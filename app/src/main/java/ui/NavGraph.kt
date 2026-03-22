package com.candlelabs.gestionpersonal.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.candlelabs.gestionpersonal.network.SupabaseClient
import io.github.jan.supabase.auth.auth

object Rutas {
    const val AUTH        = "auth"
    const val ONBOARDING  = "onboarding"
    const val MAIN        = "main"
    const val DOLAR       = "dolar"
    const val ALQUILER    = "alquiler"
    const val HISTORIAL   = "historial"
    const val INFO        = "info"
    const val HOME        = "home"
    const val ASISTENTE   = "asistente"
    const val PRESUPUESTO = "presupuesto"
    const val DOLAR_DETALLE = "dolar_detalle/{casa}/{nombre}"

    fun dolarDetalleRuta(casa: String, nombre: String) = "dolar_detalle/$casa/$nombre"
}

@Composable
fun NavGraph(navController: NavHostController) {

    // Verificamos si ya tiene sesión activa en Supabase
    // Si sí → va directo al main
    // Si no → va al auth
    val sesionActiva = try {
        SupabaseClient.instance.auth.currentSessionOrNull() != null
    } catch (e: Exception) {
        false
    }

    val destinoInicial = if (sesionActiva) Rutas.MAIN else Rutas.AUTH

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {
        // Pantalla de login/registro
        composable(Rutas.AUTH) {
            AuthScreen(
                onAuthExitoso = {
                    navController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding (nombre del usuario) — se mantiene por si lo necesitás
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
