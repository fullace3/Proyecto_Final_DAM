package com.example.proyectazo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

// ── Colores (ajústalos a tu Theme.kt) ────────────────────────────────────────
private val BackgroundColor = Color(0xFF1C1C2E)
private val SurfaceColor    = Color(0xFF2A2A3E)
private val TextPrimary     = Color(0xFFFFFFFF)
private val TextSecondary   = Color(0xFF9E9E9E)
private val DividerColor    = Color(0xFF3A3A4E)

// ── Modelo de datos ───────────────────────────────────────────────────────────
data class Ejercicio(
    val id: Int,
    val nombre: String,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val musculo: String,
    val equipamiento: String,
    val imagenUrl: String = ""
)

// ── Datos de ejemplo ──────────────────────────────────────────────────────────
private val ejerciciosEjemplo = listOf(
    Ejercicio(1,  "Press plano con mancuernas",          musculo = "Pecho",     equipamiento = "Mancuernas"),
    Ejercicio(2,  "Press inclinado con barra",            musculo = "Pecho",     equipamiento = "Barra"),
    Ejercicio(3,  "Press militar con mancuernas",         musculo = "Hombro",    equipamiento = "Mancuernas"),
    Ejercicio(4,  "Elevaciones laterales con mancuernas", musculo = "Hombro",    equipamiento = "Mancuernas"),
    Ejercicio(5,  "Face pull en polea",                   musculo = "Hombro",    equipamiento = "Polea"),
    Ejercicio(6,  "Tríceps tras nuca en polea",           musculo = "Tríceps",   equipamiento = "Polea"),
    Ejercicio(7,  "Flexiones en diamante",                musculo = "Tríceps",   equipamiento = "Peso corporal"),
    Ejercicio(8,  "Curl de bíceps con barra",             musculo = "Bíceps",    equipamiento = "Barra"),
    Ejercicio(9,  "Sentadilla con barra",                 musculo = "Cuádriceps",equipamiento = "Barra"),
    Ejercicio(10, "Peso muerto",                          musculo = "Espalda",   equipamiento = "Barra"),
)

// ── Pantalla ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEjercicios(navController: NavController) {

    var searchQuery  by remember { mutableStateOf("") }
    var selectedTab  by remember { mutableStateOf(0) }

    val listaFiltrada = remember(searchQuery, selectedTab) {
        ejerciciosEjemplo
            .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
            .let { lista ->
                if (selectedTab == 0) lista.sortedBy { it.musculo }
                else                  lista.sortedBy { it.equipamiento }
            }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Añadir ejercicio",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // ── Buscador ─────────────────────────────────────────────────────
            Buscador(query = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(Modifier.height(16.dp))

            // ── Pestañas ─────────────────────────────────────────────────────
            SelectorPestanas(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(Modifier.height(8.dp))

            // ── Lista ─────────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(listaFiltrada, key = { it.id }) { ejercicio ->
                    FilaEjercicio(
                        ejercicio = ejercicio,
                        onClick = {
                            // TODO: cuando tengas la pantalla de detalle cambia esto:
                            // navController.navigate("detalle_ejercicio/${ejercicio.id}")
                            navController.navigate("placeholder")
                        }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ── Buscador ──────────────────────────────────────────────────────────────────
@Composable
private fun Buscador(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 15.sp),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) Text("Buscar ejercicio", color = TextSecondary, fontSize = 15.sp)
                inner()
            }
        )
        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

// ── Selector de pestañas ──────────────────────────────────────────────────────
@Composable
private fun SelectorPestanas(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Músculo", "Equipamiento")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) BackgroundColor else TextSecondary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ── Fila de ejercicio ─────────────────────────────────────────────────────────
@Composable
private fun FilaEjercicio(ejercicio: Ejercicio, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen
        AsyncImage(
            model = ejercicio.imagenUrl.ifEmpty { null },
            contentDescription = ejercicio.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceColor)
        )

        Spacer(Modifier.width(14.dp))

        // Texto
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ejercicio.nombre,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${ejercicio.series} series x ${ejercicio.repeticiones} repeticiones",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}