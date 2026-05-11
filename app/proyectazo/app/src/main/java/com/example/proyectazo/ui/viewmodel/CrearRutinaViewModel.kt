package com.tuapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.ui.screens.entrenos.EjercicioRutina
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Rutina(
    val id: Int = 0,
    val nombre: String,
    val ejercicios: List<EjercicioRutina>
)

sealed class RutinaUiState {
    object Idle : RutinaUiState()
    object Loading : RutinaUiState()
    object Success : RutinaUiState()
    data class Error(val mensaje: String) : RutinaUiState()
}

class RutinaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RutinaUiState>(RutinaUiState.Idle)
    val uiState: StateFlow<RutinaUiState> = _uiState.asStateFlow()

    fun guardarRutina(nombre: String, ejercicios: List<EjercicioRutina>) {
        if (nombre.isBlank()) {
            _uiState.value = RutinaUiState.Error("El nombre no puede estar vacío")
            return
        }

        viewModelScope.launch {
            _uiState.value = RutinaUiState.Loading
            try {
                val rutina = Rutina(nombre = nombre, ejercicios = ejercicios)
                // TODO: reemplaza esto por tu llamada al repositorio/API
                // repositorio.guardarRutina(rutina)
                println("Rutina guardada: $rutina") // temporal
                _uiState.value = RutinaUiState.Success
            } catch (e: Exception) {
                _uiState.value = RutinaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _uiState.value = RutinaUiState.Idle
    }
}