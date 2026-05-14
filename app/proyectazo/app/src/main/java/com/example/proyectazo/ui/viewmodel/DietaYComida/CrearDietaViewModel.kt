package com.example.proyectazo.ui.viewmodel.DietaYComida

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.DietaRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AlimentoItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Int = 0,
    val carbohidratos: Int = 0,
    val grasas: Int = 0,
    val dia: String? = null,
    val tipo: String = "Desayuno", // Desayuno, Comida, Cena
    val uid: Long = System.nanoTime()
)

data class CrearDietaUiState(
    val nombreDieta: String = "",
    val objetivo: String = "",
    val alimentos: List<AlimentoItem> = emptyList(),
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
) {
    val proteinas: Int get() = alimentos.sumOf { it.proteinas }
    val carbohidratos: Int get() = alimentos.sumOf { it.carbohidratos }
    val grasas: Int get() = alimentos.sumOf { it.grasas }
    val calorasTotales: Int get() = alimentos.sumOf { it.calorias }
}

class CrearDietaViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(CrearDietaUiState())
    val uiState: StateFlow<CrearDietaUiState> = _uiState

    fun onNombreDietaChange(v: String) = _uiState.update { it.copy(nombreDieta = v) }
    fun onObjetivoChange(v: String) = _uiState.update { it.copy(objetivo = v) }

    fun agregarAlimento(alimento: AlimentoItem) {
        _uiState.update { it.copy(alimentos = it.alimentos + alimento) }
    }

    fun eliminarAlimento(uid: Long) {
        _uiState.update { it.copy(alimentos = it.alimentos.filter { a -> a.uid != uid }) }
    }

    fun guardar(onExitoso: () -> Unit) {
        val state = _uiState.value
        if (state.nombreDieta.isBlank()) {
            _uiState.update { it.copy(error = "Nombre de dieta requerido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val ahora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val request = DietaRequest(
                    nombre = state.nombreDieta.ifBlank { "Nueva dieta" },
                    objetivo_calorico = state.calorasTotales,
                    proteinas_g = state.proteinas.toDouble(),
                    carbohidratos_g = state.carbohidratos.toDouble(),
                    grasas_g = state.grasas.toDouble(),
                    fecha_inicio = ahora,
                    id_usuario = userId
                )
                val resp = api.crearDieta(request)
                if (resp.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                    onExitoso()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al guardar") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrearDietaViewModel(context) as T
    }
}