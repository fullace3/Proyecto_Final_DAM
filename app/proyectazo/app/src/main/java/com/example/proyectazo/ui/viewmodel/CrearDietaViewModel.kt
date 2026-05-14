package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlimentoItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Int = 0,
    val carbohidratos: Int = 0,
    val grasas: Int = 0,
    val dia: String? = null // Lun, Mar, Mie, Jue, Vie, Sab, Dom (nullable)
)

data class CrearDietaUiState(
    val nombreDieta: String = "",
    val objetivo: String = "",
    val alimentos: List<AlimentoItem> = emptyList(),
    val isLoading: Boolean = false
) {
    // Calcular automáticamente
    val proteinas: Int get() = alimentos.sumOf { it.proteinas }
    val carbohidratos: Int get() = alimentos.sumOf { it.carbohidratos }
    val grasas: Int get() = alimentos.sumOf { it.grasas }
    val calorasTotales: Int get() = alimentos.sumOf { it.calorias }
}

class CrearDietaViewModel(context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearDietaUiState())
    val uiState: StateFlow<CrearDietaUiState> = _uiState

    fun onNombreDietaChange(v: String) = _uiState.update { it.copy(nombreDieta = v) }
    fun onObjetivoChange(v: String) = _uiState.update { it.copy(objetivo = v) }

    fun agregarAlimento(alimento: AlimentoItem) {
        _uiState.update { it.copy(alimentos = it.alimentos + alimento) }
    }

    fun eliminarAlimento(alimentoId: Int) {
        _uiState.update { it.copy(alimentos = it.alimentos.filter { a -> a.id != alimentoId }) }
    }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // TODO: Llamar API para guardar dieta
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrearDietaViewModel(context) as T
    }
}