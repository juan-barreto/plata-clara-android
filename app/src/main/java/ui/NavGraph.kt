package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay

object Rutas {
    const val SPLASH      = "splash"
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
    const val PERFIL      = "perfil"
    const val DOLAR_DETALLE = "dolar_detalle/{casa}/{nombre}"

    fun dolarDetalleRuta(casa: String, nombre: String) = "dolar_detalle/$casa/$nombre"
}

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH
    ) {
        // ── SPLASH — espera verificación de sesión ──
        composable(Rutas.SPLASH) {
            SplashScreen()

            LaunchedEffect(Unit) {
                // Le damos un momento a Supabase para restaurar la sesión
                delay(800)

                val sesionActiva = try {
                    SupabaseClient.instance.auth.currentSessionOrNull() != null
                } catch (e: Exception) {
                    false
                }

                val destino = if (sesionActiva) Rutas.MAIN else Rutas.AUTH

                navController.navigate(destino) {
                    popUpTo(Rutas.SPLASH) { inclusive = true }
                }
            }
        }

        // ── AUTH — login/registro ──
        composable(Rutas.AUTH) {
            AuthScreen(
                onAuthExitoso = {
                    navController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // ── ONBOARDING — se mantiene por si lo necesitás ──
        composable(Rutas.ONBOARDING) {
            OnboardingScreen(
                onNombreGuardado = {
                    navController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── MAIN — home con bottom nav ──
        composable(Rutas.MAIN) {
            MainScreen(
                onCerrarSesion = {
                    navController.navigate(Rutas.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SPLASH SCREEN — logo + loading mientras verifica sesión
// ═══════════════════════════════════════════════════════════
@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoNegro),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_plata_clara),
                contentDescription = "Plata Clara",
                modifier = Modifier.height(250.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(
                color = VerdePrimario,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}