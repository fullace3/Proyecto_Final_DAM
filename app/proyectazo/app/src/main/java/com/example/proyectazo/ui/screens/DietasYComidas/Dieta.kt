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

    TodasLasDietasScreen(
        dietas = uiState.dietas,
        isLoading = uiState.isLoading,
        onCrearDieta = onCrearDieta,
        onSeleccionarDieta = { dietaId -> viewModel.seleccionarDieta(dietaId) }
    )
}