package com.example.proyectazo.ui.screens.PerfilYAjustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectazo.notificaciones.NotificationScheduler
import com.example.proyectazo.ui.components.SmartFitTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraEntrenamientoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("smartfit_config", android.content.Context.MODE_PRIVATE)
    }

    val horaGuardada = prefs.getString("hora_entrenamiento", "20:00") ?: "20:00"
    val partes = horaGuardada.split(":")
    val horaInicial = partes.getOrNull(0)?.toIntOrNull() ?: 20
    val minutoInicial = partes.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minutoInicial,
        is24Hour = true
    )

    var guardado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Hora del entrenamiento", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "Hora de entrenamiento",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            TimeInput(state = timePickerState)

            Spacer(Modifier.height(8.dp))

            Text(
                "Recibirás una notificación 30 minutos antes",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (guardado) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "✓ Guardado — notificación programada",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val horaStr = "%02d:%02d".format(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    // Guardar en SharedPreferences
                    prefs.edit()
                        .putString("hora_entrenamiento", horaStr)
                        .apply()

                    // Programar notificación en hilo secundario via WorkManager
                    NotificationScheduler.programar(context, horaStr)

                    guardado = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Guardar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}