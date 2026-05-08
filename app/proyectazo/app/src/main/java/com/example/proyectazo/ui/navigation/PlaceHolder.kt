package com.example.proyectazo.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla temporal para rutas que todavía no tienen su @Composable real.
 * Sustituir por la pantalla real cuando esté implementada.
 */
@Composable
fun PlaceholderScreen(
    nombre: String,
    onCerrarSesion: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pantalla: $nombre",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Próximamente",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (nombre == "Home") {
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onCerrarSesion) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}