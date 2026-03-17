package com.candlelabs.gestionpersonal.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.MonetizationOn
@Composable
fun SplashScreen(onSplashTerminado: () -> Unit) {

    // Controla si el contenido es visible o no — arranca invisible
    // "by" delega la lectura del estado, igual que en tus ViewModels
    var visible by remember { mutableStateOf(false) }

    // animateFloatAsState anima un número entre 0f y 1f suavemente
    // tween(800) significa que tarda 800ms en completarse
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splash_alpha"
    )

    // LaunchedEffect corre UNA SOLA VEZ cuando el composable aparece
    // La key "Unit" significa "no lo repitas nunca" — Unit es como None en Python
    LaunchedEffect(Unit) {
        visible = true          // dispara la animación de entrada
        delay(2000)  // espera 2 segundos sin bloquear la UI
        onSplashTerminado()     // avisa al NavGraph que navegue a Main
    }

    // Fondo verde que ocupa toda la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF16A34A))  // Verde40 de tu paleta
            .alpha(alpha),                  // aplica la animación de fade
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Ícono central — Lima80 como pediste
        Icon(
            imageVector = Icons.Filled.MonetizationOn,
            contentDescription = "Logo",
            tint = Color(0xFFFFD700),  // dorado
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre de la app
        Text(
            text = "Gestión Personal",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo
        Text(
            text = "Argentina",
            fontSize = 16.sp,
            color = Color(0xFFBEF264)       // Lima80
        )
    }
}