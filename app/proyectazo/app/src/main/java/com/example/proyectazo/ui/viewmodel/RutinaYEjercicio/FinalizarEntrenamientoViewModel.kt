package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.HistorialRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.screens.RutinasYEjercicio.ResultadoEntrenamiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GuardarUiState {
    object Idle : GuardarUiState()
    object Guardando : GuardarUiState()
    object Guardado : GuardarUiState()
    data class Error(val msg: String) : GuardarUiState()
}

class FinalizarEntrenamientoViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _state = MutableStateFlow<GuardarUiState>(GuardarUiState.Idle)
    val state: StateFlow<GuardarUiState> = _state

    fun guardarEntrenamiento(resultado: ResultadoEntrenamiento) {
        viewModelScope.launch {
            _state.value = GuardarUiState.Guardando
            try {
                // Guardar un registro por ejercicio con series completadas
                resultado.ejercicios.forEach { ejercicioRes ->
                    val seriesCompletadas = ejercicioRes.series.filter { it.completada }
                    if (seriesCompletadas.isNotEmpty()) {
                        val pesoPromedio = seriesCompletadas
                            .mapNotNull { it.peso.toDoubleOrNull() }.average()
                            .takeIf { !it.isNaN() } ?: 0.0
                        val repsPromedio = seriesCompletadas
                            .mapNotNull { it.reps.toIntOrNull() }
                            .average().toInt().coerceAtLeast(1)

                        api.registrarHistorial(
                            HistorialRequest(
                                id_usuario = userId,
                                id_ejercicio = ejercicioRes.id,
                                id_rutina = resultado.rutinaId,
                                peso_kg = pesoPromedio,
                                repeticiones = repsPromedio,
                                series = seriesCompletadas.size,
                                duracion_minutos = (resultado.tiempoSegundos / 60).coerceAtLeast(1)
                            )
                        )
                    }
                }
                _state.value = GuardarUiState.Guardado
            } catch (e: Exception) {
                _state.value = GuardarUiState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FinalizarEntrenamientoViewModel(context) as T
    }
}