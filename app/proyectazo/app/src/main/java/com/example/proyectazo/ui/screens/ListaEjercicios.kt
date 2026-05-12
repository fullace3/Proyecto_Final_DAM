package com.example.proyectazo.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectazo.R
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.ui.viewmodel.AñadirEjercicioUiState

enum class FiltroTipo { MUSCULO, EQUIPAMIENTO }

private val musculoImagenes: Map<String, Int> = mapOf(
    "Antebrazos" to R.drawable.img_003_culturismo,
    "Trapecio" to R.drawable.img_003_atrs_1,
    "Bíceps" to R.drawable.img_002_bceps,
    "Espalda" to R.drawable.img_003_atrs_1,
    "Cuádriceps" to R.drawable.img_002_frente,
    "Isquiosurales" to R.drawable.img_002_atrs,
    "Gemelos" to R.drawable.img_001_msculos,
    "Glúteos" to R.drawable.img_001_gluteo,
    "Hombros" to R.drawable.img_003_hombro,
    "Abdominales" to R.drawable.img_003_ms_bajo,
    "Pecho" to R.drawable.img_001_gimnasia,
    "Dorsal" to R.drawable.img_002_dorsal,
    "Tríceps" to R.drawable.img_001_triceps,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEjerciciosScreen(
    uiState: AñadirEjercicioUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFiltroTipoChange: (FiltroTipo) -> Unit,
    onFiltroValorChange: (String?) -> Unit,
    onEjercicioClick: (EjercicioResponse) -> Unit
) {
    var panelVisible by remember { mutableStateOf(false) }

    // Si se selecciona un valor, cerrar el panel
    LaunchedEffect(uiState.filtroValor) {
        if (uiState.filtroValor != null) panelVisible = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Añadir ejercicio",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Contenido principal (lista) ────────────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar ejercicio") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroTipo.entries.forEach { tipo ->
                        val selected = uiState.filtroValor != null && uiState.filtroTipo == tipo
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onFiltroTipoChange(tipo)
                                onFiltroValorChange(null)
                                panelVisible = true   // abrir panel
                            },
                            label = {
                                Text(
                                    text = if (tipo == FiltroTipo.MUSCULO) "Músculo" else "Equipamiento",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))

                when {
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.error,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                    uiState.ejerciciosFiltrados.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No se encontraron ejercicios",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(
                                items = uiState.ejerciciosFiltrados,
                                key = { it.id_ejercicio }
                            ) { ejercicio ->
                                EjercicioItem(
                                    ejercicio = ejercicio,
                                    onClick = { onEjercicioClick(ejercicio) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 80.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Panel superpuesto ──────────────────────────────────
            if (panelVisible) {
                // Fondo oscuro para cerrar al tocar fuera
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { panelVisible = false }
                )

                // Panel blanco con el grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 8.dp)
                ) {
                    // Línea indicadora
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (uiState.filtroTipo == FiltroTipo.MUSCULO) "Grupo muscular" else "Equipamiento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Botón quitar filtro (solo si hay filtro activo)
                    if (uiState.filtroValor != null) {
                        TextButton(
                            onClick = {
                                onFiltroValorChange(null)
                                panelVisible = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "✕  Quitar filtro",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (uiState.filtroTipo == FiltroTipo.MUSCULO) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filtrosDisponibles) { grupo ->
                                GrupoMusculoItem(
                                    nombre = grupo,
                                    selected = uiState.filtroValor == grupo,
                                    onClick = {
                                        if (uiState.filtroValor == grupo)
                                            onFiltroValorChange(null)  // quitar filtro
                                        else
                                            onFiltroValorChange(grupo)
                                        panelVisible = false
                                    }
                                )
                            }
                        }
                    } else {
                        // Equipamiento: grid con imagen del primer ejercicio de cada tipo
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filtrosDisponibles) { equip ->
                                val imagen = uiState.ejercicios
                                    .firstOrNull { it.equipamiento == equip }?.imagen
                                GrupoEquipamientoItem(
                                    nombre = equip,
                                    imagenUrl = imagen,
                                    selected = uiState.filtroValor == equip,
                                    onClick = {
                                        if (uiState.filtroValor == equip)
                                            onFiltroValorChange(null)  // quitar filtro
                                        else
                                            onFiltroValorChange(equip)
                                        panelVisible = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrupoMusculoItem(nombre: String, selected: Boolean, onClick: () -> Unit) {
    val drawable = musculoImagenes[nombre]
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (drawable != null) {
            Image(
                painter = painterResource(id = drawable),
                contentDescription = nombre,
                modifier = Modifier.size(64.dp).padding(4.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(nombre.take(1), style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun GrupoEquipamientoItem(
    nombre: String,
    imagenUrl: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (imagenUrl != null) {
                AsyncImage(
                    model = imagenUrl,
                    contentDescription = nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(nombre.take(1), style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun EjercicioItem(ejercicio: EjercicioResponse, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (ejercicio.imagen != null) {
                AsyncImage(
                    model = ejercicio.imagen,
                    contentDescription = ejercicio.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(ejercicio.nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ejercicio.nombre,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            val subtitulo = listOfNotNull(ejercicio.grupo_muscular, ejercicio.equipamiento)
                .joinToString(" · ").ifBlank { "3 series x 10 repeticiones" }
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}