package com.example.proyectazo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.proyectazo.ui.viewmodel.RutinaConEjercicios
import com.example.proyectazo.ui.viewmodel.RutinasUiState
import com.example.proyectazo.ui.viewmodel.RutinasViewModel

// ─────────────────────────────────────────────────────────────────
//  Constants
//  NOTE: make sure your FastAPI serves static files at this path:
//  app.mount("/static", StaticFiles(directory="static"), name="static")
// ─────────────────────────────────────────────────────────────────

private const val IMAGE_BASE_URL =
    "https://smartfit-imagenes-dam.s3.us-east-1.amazonaws.com/imagenes_ejercicios/"
private const val MAX_PREVIEW_IMAGES = 4

// ─────────────────────────────────────────────────────────────────
//  Main screen
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRutinas(
    onCrearRutina: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: RutinasViewModel = viewModel(
        factory = RutinasViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Todas las rutinas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearRutina,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nueva rutina"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {

                // ── Loading ────────────────────────────────────
                is RutinasUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // ── Error ──────────────────────────────────────
                is RutinasUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 15.sp
                        )
                        OutlinedButton(onClick = { viewModel.cargarRutinas() }) {
                            Text("Reintentar")
                        }
                    }
                }

                // ── Empty ──────────────────────────────────────
                is RutinasUiState.Vacio -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No tienes rutinas todavía",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Pulsa + para crear tu primera rutina",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // ── Routines list ──────────────────────────────
                is RutinasUiState.Exito -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.rutinas,
                            key = { it.rutina.id_rutina }   // stable key for animations
                        ) { rutinaConEj ->
                            TarjetaRutina(rutinaConEj = rutinaConEj)
                        }

                        // Extra bottom space so FAB doesn't cover last card
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Routine card
// ─────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaRutina(rutinaConEj: RutinaConEjercicios) {

    // rememberSaveable → expansion state survives screen rotation ✅
    var expandida by rememberSaveable { mutableStateOf(false) }

    val ejercicios       = rutinaConEj.ejercicios
    val preview          = ejercicios.take(MAX_PREVIEW_IMAGES)
    val extrasCount      = ejercicios.size - preview.size

    Card(
        onClick = { expandida = !expandida },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {

            // ── Routine title ──────────────────────────────────
            Text(
                text = rutinaConEj.rutina.nombre,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Exercise image row ─────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                preview.forEach { ejercicio ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("$IMAGE_BASE_URL${ejercicio.imagen}")
                            .crossfade(true)
                            .build(),
                        contentDescription = ejercicio.nombre,
                        contentScale = ContentScale.Crop,     // ContentScale.Crop ✅
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                // "+N more" badge
                if (extrasCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$extrasCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // ── Expandable exercise list (Spring animation) ────
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(                     // Spring animation ✅
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = shrinkVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (ejercicios.isEmpty()) {
                        Text(
                            text = "Esta rutina no tiene ejercicios asignados",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ejercicios.forEachIndexed { index, ejercicio ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Index badge
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ejercicio.nombre,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}