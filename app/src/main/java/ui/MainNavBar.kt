package com.candlelabs.gestionpersonal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.candlelabs.gestionpersonal.ui.theme.*

// ═══════════════════════════════════════════════════════════
// MAIN NAV BAR
// Barra de navegación inferior con ítems izquierda/derecha
// y espacio central para Clara.
// ═══════════════════════════════════════════════════════════

data class ItemNavegacion(val ruta: String, val iconoRes: Int, val etiqueta: String)
data class ItemMas(val ruta: String, val icono: ImageVector, val titulo: String, val subtitulo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavBar(
    navController: NavController,
    itemsIzquierda: List<ItemNavegacion>,
    itemsDerecha: List<ItemNavegacion>,
    itemsMas: List<ItemMas>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    var mostrarMenuMas by remember { mutableStateOf(false) }

    // ── Menú Más ──────────────────────────────────────────────
    if (mostrarMenuMas) {
        ModalBottomSheet(
            onDismissRequest = { mostrarMenuMas = false },
            containerColor = Color(0xFF111111)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Más opciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                itemsMas.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(item.ruta) { launchSingleTop = true }
                                mostrarMenuMas = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = VerdeOscuro,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icono, null, tint = VerdePrimario, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text(item.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = TextoPrimario)
                            Text(item.subtitulo, style = MaterialTheme.typography.bodySmall, color = TextoSecundario)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
                    }
                    if (item != itemsMas.last()) HorizontalDivider(color = Divisor, modifier = Modifier.padding(horizontal = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Barra inferior ────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(FondoNegro)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.07f), Color.Transparent),
                        startY = 0f, endY = 32f
                    )
                )
            }
            .height(45.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ítems izquierda
            itemsIzquierda.forEach { item ->
                val sel = rutaActual == item.ruta
                Column(
                    modifier = Modifier.weight(1f).clickable {
                        navController.navigate(item.ruta) { launchSingleTop = true }
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painterResource(item.iconoRes), item.etiqueta,
                        Modifier.size(24.dp).offset(y = 4.dp),
                        tint = if (sel) VerdePrimario else Color(0xFF444444)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sel) VerdePrimario else Color(0xFF444444),
                        modifier = Modifier.offset(y = 4.dp)
                    )
                }
            }

            // Espacio central — Clara vive acá
            Spacer(Modifier.weight(1f))

            // Ítems derecha
            itemsDerecha.forEach { item ->
                val sel = rutaActual == item.ruta
                Column(
                    modifier = Modifier.weight(1f).clickable {
                        navController.navigate(item.ruta) { launchSingleTop = true }
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painterResource(item.iconoRes), item.etiqueta,
                        Modifier.size(24.dp).offset(y = 4.dp),
                        tint = if (sel) VerdePrimario else Color(0xFF444444)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sel) VerdePrimario else Color(0xFF444444),
                        modifier = Modifier.offset(y = 4.dp)
                    )
                }
            }

            // Botón Más
            val masSel = rutaActual in listOf(Rutas.PRESUPUESTO, Rutas.HISTORIAL, Rutas.INFO, Rutas.PERFIL)
            Column(
                modifier = Modifier.weight(1f).clickable { mostrarMenuMas = true },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painterResource(com.candlelabs.gestionpersonal.R.drawable.icon_mas), "Más",
                    Modifier.size(24.dp).offset(y = 4.dp),
                    tint = if (masSel) VerdePrimario else Color(0xFF444444)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Más",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (masSel) VerdePrimario else Color(0xFF444444),
                    modifier = Modifier.offset(y = 4.dp)
                )
            }
        }
    }
}
