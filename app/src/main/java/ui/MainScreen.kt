package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.statusBarsPadding

data class ItemNavegacion(
    val ruta: String,
    val icono: ImageVector,
    val etiqueta: String
)

// Ítems del menú "Más" — van en el BottomSheet
data class ItemMas(
    val ruta: String,
    val icono: ImageVector,
    val titulo: String,
    val subtitulo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    var mostrarMenuMas by remember { mutableStateOf(false) }

    val items = listOf(
        ItemNavegacion(Rutas.HOME, Icons.Filled.Home, "Inicio"),
        ItemNavegacion(Rutas.DOLAR, Icons.Filled.AttachMoney, "Dólar"),
        ItemNavegacion(Rutas.ALQUILER, Icons.Filled.HomeWork, "Alquiler"),
        ItemNavegacion(Rutas.ASISTENTE, Icons.Filled.SmartToy, "Clara"),
    )

    val itemsMas = listOf(
        ItemMas(Rutas.PRESUPUESTO, Icons.Filled.AccountBalanceWallet, "Presupuesto", "Ingresos y gastos"),
        ItemMas(Rutas.HISTORIAL, Icons.Filled.History, "Historial", "Cálculos de alquiler"),
        ItemMas(Rutas.INFO, Icons.Filled.Info, "¿Cómo funciona?", "Guía de contratos"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    if (mostrarMenuMas) {
        ModalBottomSheet(
            onDismissRequest = { mostrarMenuMas = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Más opciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                itemsMas.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(item.ruta) {
                                    launchSingleTop = true
                                }
                                mostrarMenuMas = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icono,
                                    contentDescription = item.titulo,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = item.titulo,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = item.subtitulo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (item != itemsMas.last()) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        // contentWindowInsets en cero — le decimos al Scaffold que no maneje insets
        // nosotros los manejamos manualmente en cada pantalla
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier.statusBarsPadding(),
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
                NavigationBarItem(
                    selected = rutaActual in listOf(
                        Rutas.PRESUPUESTO, Rutas.HISTORIAL, Rutas.INFO
                    ),
                    onClick = { mostrarMenuMas = true },
                    icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "Más") },
                    label = { Text("Más") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
            // padding del nav bar + consumimos los insets para que no se dupliquen
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
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
            composable(Rutas.ASISTENTE) {
                AsistenteScreen()
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
            composable(Rutas.PRESUPUESTO) {
                PresupuestoScreen()
            }
        }
    }
}