package com.example.proyectazo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.ApiService
import com.example.proyectazo.network.RutinaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CrearRutinaUiState {
    object Idle    : CrearRutinaUiState()
    object Loading : CrearRutinaUiState()
    // Lleva el id_rutina devuelto por la API para poder navegar
    data class RutinaCreada(val rutinaId: Int) : CrearRutinaUiState()
    data class Error(val mensaje: String) : CrearRutinaUiState()
}

class RutinaViewModel(
    private val apiService: ApiService,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<CrearRutinaUiState>(CrearRutinaUiState.Idle)
    val uiState: StateFlow<CrearRutinaUiState> = _uiState.asStateFlow()

    /**
     * Crea la rutina en la API y emite [CrearRutinaUiState.RutinaCreada] con el id real.
     * Se llama al pulsar "Añadir ejercicio" para tener el id antes de navegar.
     */
    fun crearRutina(nombre: String) {
        if (nombre.isBlank()) {
            _uiState.value = CrearRutinaUiState.Error("Escribe un nombre antes de añadir ejercicios")
            return
        }
        viewModelScope.launch {
            _uiState.value = CrearRutinaUiState.Loading
            try {
                val response = apiService.crearRutina(
                    RutinaRequest(nombre = nombre, id_usuario = userId)
                )
                if (response.isSuccessful) {
                    val rutinaId = response.body()?.id_rutina
                        ?: throw Exception("La API no devolvió id_rutina")
                    _uiState.value = CrearRutinaUiState.RutinaCreada(rutinaId)
                } else {
                    _uiState.value = CrearRutinaUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = CrearRutinaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() { _uiState.value = CrearRutinaUiState.Idle }

    // ── Factory ─────────────────────────────────────────────────────
    class Factory(
        private val apiService: ApiService,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinaViewModel(apiService, userId) as T
    }
}