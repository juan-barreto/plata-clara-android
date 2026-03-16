package com.candlelabs.gestionpersonal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.candlelabs.gestionpersonal.ui.NavGraph
import com.candlelabs.gestionpersonal.ui.Rutas
import com.candlelabs.gestionpersonal.ui.theme.GestionPersonalARGTheme

// Modelo de cada ítem del bottom bar
data class ItemNavegacion(
    val ruta: String,           // a qué pantalla navega
    val icono: ImageVector,     // qué ícono muestra
    val etiqueta: String        // qué texto muestra abajo
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionPersonalARGTheme {

                // Crea el NavController — el que maneja el historial de pantallas
                // "remember" hace que sobreviva recomposiciones de Compose
                val navController = rememberNavController()

                // La lista de ítems del bottom bar
                val items = listOf(
                    ItemNavegacion(Rutas.DOLAR, Icons.Filled.Home, "Dólar"),
                    ItemNavegacion(Rutas.ALQUILER, Icons.Filled.Search, "Alquiler"),
                    ItemNavegacion(Rutas.HISTORIAL, Icons.Filled.List, "Historial")
                )

                // Observa la pantalla actual para saber qué ícono resaltar
                // "by" delega la lectura del estado — igual que en DolarScreen
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        // La barra de navegación de abajo
                        NavigationBar {
                            items.forEach { item ->
                                NavigationBarItem(
                                    // está seleccionado si su ruta coincide con la pantalla actual
                                    selected = rutaActual == item.ruta,
                                    onClick = {
                                        navController.navigate(item.ruta) {
                                            // evita apilar la misma pantalla múltiples veces
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
                    // El mapa de navegación recibe el navController y el padding
                    NavGraph(
                        navController = navController,
                        paddingValues = innerPadding
                    )
                }
            }
        }
    }
}