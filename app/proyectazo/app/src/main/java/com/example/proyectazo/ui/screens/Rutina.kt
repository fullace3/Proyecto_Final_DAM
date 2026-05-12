package com.example.proyectazo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.proyectazo.ui.viewmodel.RutinaConEjercicios
import com.example.proyectazo.ui.viewmodel.RutinasUiState
import com.example.proyectazo.ui.viewmodel.RutinasViewModel

private const val MAX_PREVIEW_IMAGES = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRutinas(
    onCrearRutina: () -> Unit,
    onVerDetalles: (RutinaConEjercicios) -> Unit = {},
    onEliminarRutina: ((rutinaId: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: RutinasViewModel = viewModel(
        factory = RutinasViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarRutinas()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Todas las rutinas", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
                Icon(Icons.Default.Add, contentDescription = "Crear nueva rutina")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is RutinasUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RutinasUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(state.mensaje, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                        OutlinedButton(onClick = { viewModel.cargarRutinas() }) { Text("Reintentar") }
                    }
                }
                is RutinasUiState.Vacio -> {
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text("No tienes rutinas todavía", fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Pulsa + para crear tu primera rutina", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
                is RutinasUiState.Exito -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.rutinas, key = { it.rutina.id_rutina }) { rutinaConEj ->
                            TarjetaRutina(
                                rutinaConEj = rutinaConEj,
                                onClick = { onVerDetalles(rutinaConEj) },
                                onEliminar = { viewModel.eliminarRutina(rutinaConEj.rutina.id_rutina) }
                            )
                        }
                        item { Spacer(Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRutina(
    rutinaConEj: RutinaConEjercicios,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    var menuVisible by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val ejercicios  = rutinaConEj.ejercicios
    val preview     = ejercicios.take(MAX_PREVIEW_IMAGES)
    val extrasCount = ejercicios.size - preview.size

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar rutina") },
            text  = { Text("¿Seguro que quieres eliminar \"${rutinaConEj.rutina.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false; onEliminar() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── Título + menú (solo Eliminar) ──────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = rutinaConEj.rutina.nombre,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { menuVisible = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = menuVisible, onDismissRequest = { menuVisible = false }) {
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null,
                                tint = MaterialTheme.colorScheme.error) },
                            onClick = { menuVisible = false; mostrarDialogoEliminar = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Imágenes preview ───────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                preview.forEach { ejercicio ->
                    AsyncImage(
                        model = ejercicio.imagen,
                        contentDescription = ejercicio.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                if (extrasCount > 0) {
                    Box(modifier = Modifier.size(68.dp).clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Text("+$extrasCount", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}