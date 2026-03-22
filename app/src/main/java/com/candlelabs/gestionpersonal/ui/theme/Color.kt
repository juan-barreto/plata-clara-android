package com.candlelabs.gestionpersonal.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
// PALETA PLATA CLARA — Fuente única de verdad para colores
// NUNCA usar Color(0xFF...) suelto en una pantalla.
// ═══════════════════════════════════════════════════════════

// — VERDE PRINCIPAL —
val VerdePrimario    = Color(0xFF00B872)
val VerdeSuave       = Color(0xFF86EFAC)
val VerdeOscuro      = Color(0xFF003D26)
val VerdeGlow        = Color(0x3300B872)

// — FONDOS —
val FondoPrincipal   = Color(0xFFF8F2F2)   // crema
val FondoCard        = Color(0xFF000000)   // negro puro — cards
val FondoNegro       = Color(0xFF000000)   // header, loading

// — TEXTOS —
val TextoPrimario    = Color(0xFFFFFFFF)   // blanco sobre cards negras
val TextoSecundario  = Color(0xFF888888)   // labels, hints
val TextoMuted       = Color(0xFFBBBBBB)   // placeholders, textos suaves
val TextoSobreCreme  = Color(0xFF000000)   // negro sobre fondo crema

// — ESTADOS —
val RojoGasto        = Color(0xFFE23E57)   // solo para monto "Gastaste" en hero card
val Naranja          = Color(0xFFFF8800)   // warnings — cerca del límite

// — TERCIARIOS —
val Lima80           = Color(0xFFBEF264)
val Lima40           = Color(0xFF65A30D)

// — BORDES Y DIVISORES —
val Divisor          = Color(0xFF222222)   // sobre cards negras
val DivisorClaro     = Color(0xFFEEEEEE)   // sobre fondo crema
val BordeCard        = Color(0x10FFFFFF)   // borde sutil blanco en cards
val SombraCard       = Color(0x40FFFFFF)   // sombra blanca para cards sobre crema

// — ICONOS DE CATEGORÍA (Material Icons Round) —
// Referencia para usar en Compose con Icons.Rounded.*
// Comida      → Icons.Rounded.ShoppingCart
// Transporte  → Icons.Rounded.DirectionsBus
// Salidas     → Icons.Rounded.Restaurant
// Servicios   → Icons.Rounded.PhoneAndroid
// Salud       → Icons.Rounded.LocalPharmacy
// Varios      → Icons.Rounded.FolderOpen