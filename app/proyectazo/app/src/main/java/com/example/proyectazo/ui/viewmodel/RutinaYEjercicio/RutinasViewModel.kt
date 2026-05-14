package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.RutinaResponse
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────
//  Data model that combines a routine with its full exercise list
// ─────────────────────────────────────────────────────────────────

data class RutinaConEjercicios(
    val rutina: RutinaResponse,
    val ejercicios: List<EjercicioResponse>   // ordered by 'orden' field
)

// ─────────────────────────────────────────────────────────────────
//  UI state
// ─────────────────────────────────────────────────────────────────

sealed class RutinasUiState {
    object Cargando : RutinasUiState()
    data class Exito(val rutinas: List<RutinaConEjercicios>) : RutinasUiState()
    object Vacio : RutinasUiState()
    data class Error(val mensaje: String) : RutinasUiState()
}

// ─────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────

class RutinasViewModel(context: Context) : ViewModel() {

    private val session = SessionManager(context)
    private val userId get() = session.getUserId()

    private val _uiState = MutableStateFlow<RutinasUiState>(RutinasUiState.Cargando)
    val uiState: StateFlow<RutinasUiState> = _uiState

    init {
        cargarRutinas()
    }

    fun cargarRutinas() {
        viewModelScope.launch {
            _uiState.value = RutinasUiState.Cargando
            try {
                // Fetch exercises and routines at the same time (parallel)
                val ejerciciosDeferred = async { RetrofitClient.instance.getEjercicios() }
                val rutinasDeferred   = async { RetrofitClient.instance.getRutinas(userId) }

                val ejerciciosResp = ejerciciosDeferred.await()
                val rutinasResp   = rutinasDeferred.await()

                if (!ejerciciosResp.isSuccessful || !rutinasResp.isSuccessful) {
                    _uiState.value = RutinasUiState.Error("Error al cargar los datos del servidor")
                    return@launch
                }

                // Build a lookup map: id_ejercicio → EjercicioResponse
                val ejerciciosMap = (ejerciciosResp.body() ?: emptyList())
                    .associateBy { it.id_ejercicio }

                val rutinas = rutinasResp.body() ?: emptyList()

                if (rutinas.isEmpty()) {
                    _uiState.value = RutinasUiState.Vacio
                    return@launch
                }

                // For each routine, load its exercise list and resolve full exercise data
                val rutinasConEjercicios = rutinas.map { rutina ->
                    val relResp = RetrofitClient.instance.getEjerciciosDeRutina(rutina.id_rutina)
                    val ejerciciosDeRutina = if (relResp.isSuccessful) {
                        relResp.body()
                            ?.sortedBy { it.orden }
                            ?.mapNotNull { rel -> ejerciciosMap[rel.id_ejercicio] }
                            ?: emptyList()
                    } else {
                        emptyList()
                    }
                    RutinaConEjercicios(rutina = rutina, ejercicios = ejerciciosDeRutina)
                }

                _uiState.value = RutinasUiState.Exito(rutinasConEjercicios)

            } catch (e: Exception) {
                _uiState.value = RutinasUiState.Error("Sin conexión con el servidor")
            }
        }
    }
    fun eliminarRutina(rutinaId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.borrarRutina(rutinaId)
                if (response.isSuccessful) {
                    cargarRutinas()  // recargar la lista
                } else {
                    _uiState.value = RutinasUiState.Error("Error al eliminar la rutina")
                }
            } catch (e: Exception) {
                _uiState.value = RutinasUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    // ── Factory ───────────────────────────────────────────────

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinasViewModel(context) as T
    }
}