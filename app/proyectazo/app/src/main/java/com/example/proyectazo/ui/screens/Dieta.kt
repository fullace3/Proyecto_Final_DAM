package com.example.proyectazo.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.DietaViewModel

@Composable
fun DietaScreen(onCrearDieta: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: DietaViewModel = viewModel(
        factory = DietaViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Por ahora, mostrar lista vacía (sin dietas creadas)
    TodasLasDietasScreen(
        dietas = emptyList(),
        onCrearDieta = onCrearDieta,
        onSeleccionarDieta = { dietaId ->
            viewModel.seleccionarDieta(dietaId)
        }
    )
}