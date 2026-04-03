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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.candlelabs.gestionpersonal.R
import com.candlelabs.gestionpersonal.network.SupabaseClient
import com.candlelabs.gestionpersonal.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay

object Rutas {
    const val SPLASH        = "splash"
    const val AUTH          = "auth"
    const val ONBOARDING    = "onboarding"
    const val MAIN          = "main"
    const val DOLAR         = "dolar"
    const val ALQUILER      = "alquiler"
    const val HISTORIAL     = "historial"
    const val INFO          = "info"
    const val HOME          = "home"
    const val ASISTENTE     = "asistente"
    const val PRESUPUESTO   = "presupuesto"
    const val PERFIL        = "perfil"
    const val DOLAR_DETALLE = "dolar_detalle/{casa}/{nombre}"
    const val RESET_PASSWORD = "reset_password?token_hash={token_hash}&type={type}"

    fun dolarDetalleRuta(casa: String, nombre: String) = "dolar_detalle/$casa/$nombre"
}

@Composable
fun NavGraph(navController: NavHostController, abrirGastoExpress: Boolean = false) {

    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH
    ) {
        composable(Rutas.SPLASH) {
            SplashScreen()

            LaunchedEffect(Unit) {
                val sesionActiva = try {
                    SupabaseClient.instance.auth.awaitInitialization()
                    SupabaseClient.instance.auth.currentSessionOrNull() != null
                } catch (e: Exception) {
                    false
                }

                delay(500)

                val destino = if (sesionActiva) Rutas.MAIN else Rutas.AUTH

                navController.navigate(destino) {
                    popUpTo(Rutas.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Rutas.AUTH) {
            AuthScreen(
                onAuthExitoso = {
                    navController.navigate(Rutas.MAIN) {
                        popUpTo(Rutas.AUTH) { inclusive = true }
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
            MainScreen(
                onCerrarSesion = {
                    navController.navigate(Rutas.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                abrirGastoExpress = abrirGastoExpress
            )
        }

        // Ruta que captura el deep link de reset password
        // Cuando Flask redirige a com.candlelabs.gestionpersonal://reset-password?token_hash=...
        // Android abre esta pantalla con el token listo para usar
        composable(
            route = Rutas.RESET_PASSWORD,
            arguments = listOf(
                navArgument("token_hash") { type = NavType.StringType; defaultValue = "" },
                navArgument("type") { type = NavType.StringType; defaultValue = "recovery" }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "com.candlelabs.gestionpersonal://reset-password?token_hash={token_hash}&type={type}"
                },
                navDeepLink {
                    uriPattern = "https://web-production-f82cf.up.railway.app/auth/reset-password?token_hash={token_hash}&type={type}"
                }
            )
        ) { backStackEntry ->
            val tokenHash = backStackEntry.arguments?.getString("token_hash") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "recovery"
            ResetPasswordScreen(
                tokenHash = tokenHash,
                type = type,
                onExito = {
                    navController.navigate(Rutas.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

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