package com.candlelabs.gestionpersonal.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

data class ItemNavegacion(
    val ruta: String,
    val icono: ImageVector,
    val etiqueta: String
)

@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val items = listOf(
        ItemNavegacion(Rutas.HOME, Icons.Filled.Home, "Inicio"),
        ItemNavegacion(Rutas.DOLAR, Icons.Filled.AttachMoney, "Dólar"),
        ItemNavegacion(Rutas.ALQUILER, Icons.Filled.HomeWork, "Alquiler"),
        ItemNavegacion(Rutas.HISTORIAL, Icons.Filled.History, "Historial")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = rutaActual == item.ruta,
                        onClick = {
                            navController.navigate(item.ruta) {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                        label = { Text(item.etiqueta) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rutas.HOME) {
                HomeScreen(navController = navController)
            }
            composable(Rutas.DOLAR) {
                DolarScreen(navController = navController)
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
            composable(Rutas.DOLAR_DETALLE) { backStackEntry ->
                val casa = backStackEntry.arguments?.getString("casa") ?: ""
                val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                DolarDetalleScreen(
                    casa = casa,
                    nombre = nombre,
                    navController = navController
                )
            }
        }
    }
}