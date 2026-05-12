package com.example.proyectazo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.ApiService
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.network.RutinaEjercicioRequest
import com.example.proyectazo.screens.FiltroTipo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AñadirEjercicioUiState(
    val ejercicios: List<EjercicioResponse> = emptyList(),
    val ejerciciosFiltrados: List<EjercicioResponse> = emptyList(),
    val filtrosDisponibles: List<String> = emptyList(),
    val filtroTipo: FiltroTipo = FiltroTipo.MUSCULO,
    val filtroValor: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val ejercicioAgregado: Boolean = false
)

class ListaEjerciciosViewModel(
    private val rutinaId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AñadirEjercicioUiState())
    val uiState: StateFlow<AñadirEjercicioUiState> = _uiState.asStateFlow()

    init {
        cargarEjercicios()
    }

    // ── Carga inicial ───────────────────────────────────────────────
    private fun cargarEjercicios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // FIX: getEjercicios() takes no arguments and returns Response<List<...>>
                val response = apiService.getEjercicios()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            ejercicios = lista,
                            // FIX: mapNotNull + distinct + sorted work directly on List, not Flow
                            filtrosDisponibles = lista
                                .mapNotNull { it.grupo_muscular }
                                .distinct()
                                .sorted()
                        ).aplicarFiltros()
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error ${response.code()}: No se pudieron cargar los ejercicios."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "No se pudieron cargar los ejercicios.")
                }
            }
        }
    }

    // ── Eventos de UI ───────────────────────────────────────────────
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).aplicarFiltros() }
    }

    fun onFiltroTipoChange(tipo: FiltroTipo) {
        _uiState.update { state ->
            val subFiltros = state.ejercicios
                .mapNotNull { if (tipo == FiltroTipo.MUSCULO) it.grupo_muscular else it.equipamiento }
                .distinct()
                .sorted()
            state.copy(filtroTipo = tipo, filtrosDisponibles = subFiltros, filtroValor = null)
                .aplicarFiltros()
        }
    }

    fun onFiltroValorChange(valor: String?) {
        _uiState.update { it.copy(filtroValor = valor).aplicarFiltros() }
    }

    fun onEjercicioSeleccionado(ejercicio: EjercicioResponse) {
        viewModelScope.launch {
            try {
                val orden = (_uiState.value.ejerciciosFiltrados.indexOf(ejercicio) + 1)
                    .coerceAtLeast(1)
                // FIX: correct method name is añadirEjercicioARutina, no token needed
                apiService.añadirEjercicioARutina(
                    datos = RutinaEjercicioRequest(
                        id_rutina = rutinaId,
                        id_ejercicio = ejercicio.id_ejercicio,
                        orden = orden
                    )
                )
                _uiState.update { it.copy(ejercicioAgregado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al agregar el ejercicio.") }
            }
        }
    }

    fun onEjercicioAgregadoConsumed() {
        _uiState.update { it.copy(ejercicioAgregado = false) }
    }

    // ── Filtrado interno ────────────────────────────────────────────
    private fun AñadirEjercicioUiState.aplicarFiltros(): AñadirEjercicioUiState {
        val filtrados = ejercicios.filter { ej ->
            val coincideBusqueda = searchQuery.isBlank() ||
                    ej.nombre.contains(searchQuery, ignoreCase = true)
            val coincideFiltro = filtroValor == null || when (filtroTipo) {
                FiltroTipo.MUSCULO      -> ej.grupo_muscular == filtroValor
                FiltroTipo.EQUIPAMIENTO -> ej.equipamiento == filtroValor
            }
            coincideBusqueda && coincideFiltro
        }
        return copy(ejerciciosFiltrados = filtrados)
    }

    // ── Factory ─────────────────────────────────────────────────────
    class Factory(
        private val rutinaId: Int,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AñadirEjercicioViewModel(rutinaId, apiService) as T
    }
}