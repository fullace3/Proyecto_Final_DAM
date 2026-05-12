package com.example.proyectazo.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.screens.ListaEjerciciosScreen
import com.example.proyectazo.ui.viewmodel.AñadirEjercicioViewModel

/**
 * Composable de entrada para la pantalla "Añadir ejercicio".
 * Se coloca en el NavGraph con la ruta "seleccionar_ejercicio/{rutinaId}".
 *
 * @param rutinaId  ID de la rutina a la que se añadirá el ejercicio
 * @param onBack    Navega hacia atrás (popBackStack)
 */
@Composable
fun AñadirEjercicioScreen(
    rutinaId: Int,
    onBack: () -> Unit
) {
    val viewModel: AñadirEjercicioViewModel = viewModel(
        factory = AñadirEjercicioViewModel.Factory(
            rutinaId = rutinaId,
            apiService = RetrofitClient.instance
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    // Cuando el ejercicio se agrega con éxito, volvemos atrás automáticamente
    LaunchedEffect(uiState.ejercicioAgregado) {
        if (uiState.ejercicioAgregado) {
            viewModel.onEjercicioAgregadoConsumed()
            onBack()
        }
    }

    ListaEjerciciosScreen(
        uiState = uiState,
        onBack = onBack,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFiltroTipoChange  = viewModel::onFiltroTipoChange,
        onFiltroValorChange = viewModel::onFiltroValorChange,
        onEjercicioClick    = viewModel::onEjercicioSeleccionado
    )
}