package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.DietaYComida.DietaViewModel

@Composable
fun DietaScreen(
    onCrearDieta: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: DietaViewModel = viewModel(
        factory = DietaViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    var mostrarTodas by remember { mutableStateOf(false) }

    val dietaActiva = uiState.dietas.find { it.activo }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (dietaActiva != null && !mostrarTodas) {
        DietaActivaScreen(
            dieta = dietaActiva,
            onNueva = onCrearDieta,
            onCambiar = { mostrarTodas = true }
        )
    } else {
        TodasLasDietasScreen(
            dietas = uiState.dietas,
            isLoading = false,
            onCrearDieta = onCrearDieta,
            onSeleccionarDieta = { dietaId ->
                viewModel.seleccionarDieta(dietaId)
                mostrarTodas = false
            },
            onBorrarDieta = { dietaId ->
                viewModel.borrarDieta(dietaId)
            },
            mostrarBack = dietaActiva != null,
            onBack = { mostrarTodas = false }
        )
    }
}