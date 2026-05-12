package com.example.proyectazo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectazo.ui.viewmodel.RutinaConEjercicios
import kotlinx.coroutines.delay

data class SerieEntrenamiento(
    val numero: Int,
    val previa: String = "-",
    val completada: Boolean = false
    // peso y reps son solo estado UI local, no se guardan aquí
)

@Composable
fun EntrenarScreen(
    rutinaConEjercicios: RutinaConEjercicios,
    onTerminar: () -> Unit
) {
    val ejercicios = rutinaConEjercicios.ejercicios
    var ejercicioActualIndex by remember { mutableStateOf(0) }
    val ejercicioActual = ejercicios.getOrNull(ejercicioActualIndex)

    // Series por ejercicio — solo guarda completada, no peso/reps
    val seriesPorEjercicio = remember {
        List(ejercicios.size) {
            mutableStateListOf(
                SerieEntrenamiento(1),
                SerieEntrenamiento(2),
                SerieEntrenamiento(3)
            )
        }
    }
    val seriesActuales = seriesPorEjercicio[ejercicioActualIndex]

    // Temporizador de descanso — solo corre tras completar una serie
    var timerActivo by remember { mutableStateOf(false) }
    var segundos by remember { mutableStateOf(90) }  // empieza en 1:30

    LaunchedEffect(timerActivo) {
        if (timerActivo) {
            while (segundos > 0) {
                delay(1000)
                segundos--
            }
            timerActivo = false  // se oculta al llegar a 0
        }
    }

    var mostrarDialogoTerminar by remember { mutableStateOf(false) }

    if (mostrarDialogoTerminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoTerminar = false },
            title = { Text("¿Terminar entrenamiento?") },
            text  = { Text("Se guardará el progreso de las series completadas.") },
            confirmButton = {
                Button(onClick = { mostrarDialogoTerminar = false; onTerminar() }) {
                    Text("Terminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoTerminar = false }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Header ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = rutinaConEjercicios.rutina.nombre,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { mostrarDialogoTerminar = true },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Terminar entrenamiento", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp)

        // ── Miniaturas ─────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(ejercicios) { index, ejercicio ->
                val isActual = index == ejercicioActualIndex
                AsyncImage(
                    model = ejercicio.imagen,
                    contentDescription = ejercicio.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            if (isActual) 2.dp else 0.dp,
                            if (isActual) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { ejercicioActualIndex = index }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = ejercicioActual?.nombre ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            item {
                AsyncImage(
                    model = ejercicioActual?.imagen,
                    contentDescription = ejercicioActual?.nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth().height(200.dp).padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Cabecera tabla ─────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("Serie", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("Previa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Peso", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    Text("Rep.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ── Filas — peso/reps son estado local puro ────────
            itemsIndexed(seriesActuales) { index, serie ->
                // key incluye ejercicioActualIndex → se resetea al cambiar ejercicio
                var peso by remember(index, ejercicioActualIndex) { mutableStateOf("") }
                var reps by remember(index, ejercicioActualIndex) { mutableStateOf("") }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(
                            if (serie.completada) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${serie.numero}", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(serie.previa, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it },  // solo local, no se guarda
                        modifier = Modifier.weight(1.5f).height(52.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center, fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },  // solo local, no se guarda
                        modifier = Modifier.weight(1.5f).height(52.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center, fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (serie.completada) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                val completada = !serie.completada
                                seriesActuales[index] = serie.copy(completada = completada)
                                if (completada) {
                                    // Iniciar/resetear temporizador de descanso
                                    segundos = 90  // resetear a 1:30
                                    timerActivo = false
                                    timerActivo = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Completar",
                            tint = if (serie.completada) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── Añadir serie ───────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        seriesActuales.add(SerieEntrenamiento(seriesActuales.size + 1))
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    border = null
                ) {
                    Text("Añadir serie", fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── Bottom bar — solo visible si el timer está activo ──
        if (timerActivo) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }

                val min = segundos / 60
                val seg = segundos % 60
                Text(text = "%d:%02d".format(min, seg),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiary)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { segundos = minOf(segundos + 15, 599) },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) { Text("+15", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { segundos = maxOf(0, segundos - 15) },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) { Text("-15", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}