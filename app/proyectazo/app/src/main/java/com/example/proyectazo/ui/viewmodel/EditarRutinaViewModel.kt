package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.EjercicioRutina
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.RutinaRequest
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarRutinaUiState(
    val nombre: String = "",
    val ejercicios: List<EjercicioRutina> = emptyList(),
    val isLoading: Boolean = true,
    val guardado: Boolean = false,
    val error: String? = null
)

class EditarRutinaViewModel(
    private val rutinaId: Int,
    private val context: Context
) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(EditarRutinaUiState())
    val uiState: StateFlow<EditarRutinaUiState> = _uiState.asStateFlow()

    init { cargarRutina() }

    private fun cargarRutina() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Cargar nombre de la rutina
                val rutinasResp = api.getRutinas(userId)
                val rutina = rutinasResp.body()?.find { it.id_rutina == rutinaId }

                // Cargar ejercicios de la rutina
                val relResp = api.getEjerciciosDeRutina(rutinaId)
                val ejerciciosResp = api.getEjercicios()
                val ejerciciosMap = ejerciciosResp.body()?.associateBy { it.id_ejercicio } ?: emptyMap()

                val ejercicios = relResp.body()
                    ?.sortedBy { it.orden }
                    ?.mapNotNull { rel ->
                        ejerciciosMap[rel.id_ejercicio]?.let { ej ->
                            EjercicioRutina(
                                id = ej.id_ejercicio,
                                nombre = ej.nombre,
                                series = rel.series,
                                repeticiones = rel.repeticiones,
                                imagenUrl = ej.imagen ?: ""
                            )
                        }
                    } ?: emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nombre = rutina?.nombre ?: "",
                        ejercicios = ejercicios
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar la rutina") }
            }
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun guardarCambios() {
        val nombre = _uiState.value.nombre
        if (nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            try {
                api.editarRutina(rutinaId, RutinaRequest(nombre = nombre, id_usuario = userId))
                _uiState.update { it.copy(guardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar los cambios") }
            }
        }
    }

    fun onGuardadoConsumed() { _uiState.update { it.copy(guardado = false) } }

    class Factory(private val rutinaId: Int, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditarRutinaViewModel(rutinaId, context) as T
    }
}