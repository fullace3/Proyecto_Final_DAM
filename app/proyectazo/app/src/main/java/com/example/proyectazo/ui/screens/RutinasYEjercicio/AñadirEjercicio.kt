package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.screens.ListaEjerciciosScreen
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.AñadirEjercicioViewModel

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

    // Ejercicio seleccionado para mostrar su detalle
    var ejercicioDetalle by remember { mutableStateOf<EjercicioResponse?>(null) }

    // Cuando se agrega con éxito, volvemos atrás
    LaunchedEffect(uiState.ejercicioAgregado) {
        if (uiState.ejercicioAgregado) {
            viewModel.onEjercicioAgregadoConsumed()
            onBack()
        }
    }

    if (ejercicioDetalle != null) {
        // ── Pantalla de detalle ──────────────────────────────────
        DetalleEjercicioScreen(
            ejercicio = ejercicioDetalle!!,
            onBack = { ejercicioDetalle = null },
            onAnadir = { ejercicio ->
                viewModel.onEjercicioSeleccionado(ejercicio)
                ejercicioDetalle = null
            }
        )
    } else {
        // ── Lista de ejercicios ──────────────────────────────────
        ListaEjerciciosScreen(
            uiState = uiState,
            onBack = onBack,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onFiltroTipoChange  = viewModel::onFiltroTipoChange,
            onFiltroValorChange = viewModel::onFiltroValorChange,
            onEjercicioClick    = { ejercicioDetalle = it }
        )
    }
}