package com.candlelabs.gestionpersonal.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onNombreGuardado: () -> Unit) {

    // Estado local del campo de texto
    var nombre by remember { mutableStateOf("") }

    // Contexto para acceder a SharedPreferences
    val context = LocalContext.current

    // Maneja el foco del teclado — para cerrarlo cuando el usuario confirma
    val focusManager = LocalFocusManager.current

    // Función que guarda el nombre y avisa al NavGraph
    // Equivalente en Python:
    // def guardar_nombre(nombre):
    //     json.dump({"nombre": nombre}, open("prefs.json", "w"))
    //     redirect("/main")
    fun guardarYContinuar() {
        if (nombre.isNotBlank()) {
            // Abre SharedPreferences en modo privado — solo esta app puede leerlo
            val prefs = context.getSharedPreferences("gestion_prefs", Context.MODE_PRIVATE)
            // edit() abre el editor, putString guarda, apply() confirma de forma asíncrona
            prefs.edit().putString("nombre_usuario", nombre.trim()).apply()
            focusManager.clearFocus() // cierra el teclado
            onNombreGuardado()        // avisa al NavGraph que navegue a MAIN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Emoji + título
        Text(
            text = "🇦🇷",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bienvenido a\nPlata Clara",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu asistente financiero argentino",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "¿Cómo te llamás?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto — ImeAction.Done cierra el teclado al tocar "listo"
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Tu nombre") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                // Cuando toca "listo" en el teclado, mismo efecto que el botón
                onDone = { guardarYContinuar() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { guardarYContinuar() },
            enabled = nombre.isNotBlank(), // deshabilitado si el campo está vacío
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Empezar")
        }
    }
}