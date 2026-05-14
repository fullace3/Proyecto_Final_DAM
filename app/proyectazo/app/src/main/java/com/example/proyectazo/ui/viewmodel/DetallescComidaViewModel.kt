package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleComidaUiState(
    val nombre: String = "",
    val calorias100g: Int = 0,
    val proteinas100g: Int = 0,
    val carbohidratos100g: Int = 0,
    val grasas100g: Int = 0,
    val imagen: String? = null,
    val isLoading: Boolean = true
)

class DetalleComidaViewModel(context: Context, private val comidaId: Int) : ViewModel() {

    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(DetalleComidaUiState())
    val uiState: StateFlow<DetalleComidaUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val resp = api.getComida(comidaId)
                if (resp.isSuccessful) {
                    val comida = resp.body()!!
                    _uiState.update {
                        it.copy(
                            nombre = comida.nombre,
                            calorias100g = comida.calorias_100g,
                            proteinas100g = comida.proteinas_100g,
                            carbohidratos100g = comida.carbohidratos_100g,
                            grasas100g = comida.grasas_100g,
                            imagen = comida.imagen,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    class Factory(private val context: Context, private val comidaId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetalleComidaViewModel(context, comidaId) as T
    }
}