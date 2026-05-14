package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.DietaResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DietaListItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Double,
    val carbohidratos: Double,
    val grasas: Double,
    val activo: Boolean
)

data class DietaUiState(
    val dietas: List<DietaListItem> = emptyList(),
    val isLoading: Boolean = true
)

class DietaViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(DietaUiState())
    val uiState: StateFlow<DietaUiState> = _uiState

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            try {
                val resp = api.getDietasUsuario(userId)
                if (resp.isSuccessful) {
                    val lista = resp.body()?.map {
                        DietaListItem(
                            id = it.id_dieta,
                            nombre = it.nombre,
                            calorias = it.objetivo_calorico,
                            proteinas = it.proteinas_g,
                            carbohidratos = it.carbohidratos_g,
                            grasas = it.grasas_g,
                            activo = it.activo
                        )
                    } ?: emptyList()
                    _uiState.update { it.copy(dietas = lista, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun seleccionarDieta(dietaId: Int) {
        viewModelScope.launch {
            try {
                api.activarDieta(dietaId)
                cargar()
            } catch (_: Exception) {}
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DietaViewModel(context) as T
    }
}