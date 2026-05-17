package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Holds the previous performance data for a single exercise
data class SeriePrevia(
    val peso: String,
    val reps: String
)

data class EntrenarUiState(
    // Map of ejercicioId -> previous best serie (most recent from historial)
    val previasPorEjercicio: Map<Int, SeriePrevia> = emptyMap(),
    val isLoading: Boolean = true
)

class EntrenarViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(EntrenarUiState())
    val uiState: StateFlow<EntrenarUiState> = _uiState

    // Load historial and extract the most recent entry per exercise
    fun cargarPrevias(ejercicioIds: List<Int>) {
        viewModelScope.launch {
            try {
                val resp = api.getHistorial(userId)
                if (resp.isSuccessful) {
                    val historial = resp.body() ?: emptyList()

                    // Group by ejercicioId and take the most recent entry
                    val previas = ejercicioIds.associateWith { id ->
                        val entrada = historial
                            .filter { it.id_ejercicio == id }
                            .maxByOrNull { it.fecha }
                        if (entrada != null) {
                            SeriePrevia(
                                peso = "${entrada.peso_kg}",
                                reps = "${entrada.repeticiones}"
                            )
                        } else {
                            SeriePrevia(peso = "-", reps = "-")
                        }
                    }
                    _uiState.update { it.copy(previasPorEjercicio = previas, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EntrenarViewModel(context) as T
    }
}