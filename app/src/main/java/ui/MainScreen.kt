package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.shadow
import com.candlelabs.gestionpersonal.R

data class ItemNavegacion(
    val ruta: String,
    val iconoRes: Int,       // ← ahora usamos recursos PNG en vez de ImageVector
    val etiqueta: String
)

// Ítems del menú "Más" — van en el BottomSheet
data class ItemMas(
    val ruta: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val titulo: String,
    val subtitulo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    var mostrarMenuMas by remember { mutableStateOf(false) }

    // — ÍTEMS DEL NAVBAR — ahora con PNGs propios
    val items = listOf(
        ItemNavegacion(Rutas.HOME, R.drawable.icon_home, "Inicio"),
        ItemNavegacion(Rutas.DOLAR, R.drawable.icon_dolar, "Dólar"),
        ItemNavegacion(Rutas.ALQUILER, R.drawable.icon_alquiler, "Alquiler"),
        ItemNavegacion(Rutas.ASISTENTE, R.drawable.icon_clarai, "ClarAI"),
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
            onDismissRequest = { mostrarMenuMas = false },
            containerColor = Color(0xFF111111) // ← fondo oscuro del bottom sheet
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
                    color = Color.White,
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
                        // Ícono con fondo verde oscuro
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF003D26), // verde oscuro
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icono,
                                    contentDescription = item.titulo,
                                    tint = Color(0xFF00B872), // verde acento
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = item.titulo,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = item.subtitulo,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF888888)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (item != itemsMas.last()) {
                        HorizontalDivider(
                            color = Color(0xFF222222),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            // — NAVBAR CUSTOM — altura y tamaño de íconos independientes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp) // ← alto fijo que querés
                    .shadow(elevation = 16.dp)
                    .background(Color(0xFFF8F2F2))
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ítems normales
                items.forEach { item ->
                    val seleccionado = rutaActual == item.ruta
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                navController.navigate(item.ruta) {
                                    launchSingleTop = true
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconoRes),
                            contentDescription = item.etiqueta,
                            modifier = Modifier.size(55.dp),// ← tamaño exacto
                            tint = if (seleccionado) Color(0xFF00B872) else Color(0xFF000000)
                        )
                        Text(
                            text = item.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (seleccionado) Color(0xFF00B872) else Color(0xFF000000)
                        )
                    }
                }
                // botón Más
                val masSeleccionado = rutaActual in listOf(Rutas.PRESUPUESTO, Rutas.HISTORIAL, Rutas.INFO)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mostrarMenuMas = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_mas),
                        contentDescription = "Más",
                        modifier = Modifier.size(55.dp), // ← tamaño exacto
                        tint = if (masSeleccionado) Color(0xFF00B872) else Color(0xFF000000)
                    )
                    Text(
                        text = "Más",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (masSeleccionado) Color(0xFF00B872) else Color(0xFF000000)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
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