package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComidaListItem(
    val id: Int,
    val nombre: String,
    val calorias100g: Int,
    val proteinas100g: Int,
    val carbohidratos100g: Int,
    val grasas100g: Int,
    val imagen: String? = null
)

data class ListaComidasUiState(
    val comidas: List<ComidaListItem> = emptyList(),
    val busqueda: String = "",
    val filtroActivo: String? = null, // "Proteinas", "Carbohidratos", "Grasas saludables"
    val isLoading: Boolean = true
) {
    val comidasFiltradas: List<ComidaListItem>
        get() {
            var lista = if (busqueda.isBlank()) comidas
            else comidas.filter { it.nombre.contains(busqueda, ignoreCase = true) }

            lista = when (filtroActivo) {
                "Proteinas" -> lista.sortedByDescending { it.proteinas100g }
                "Carbohidratos" -> lista.sortedByDescending { it.carbohidratos100g }
                "Grasas saludables" -> lista.sortedByDescending { it.grasas100g }
                else -> lista
            }
            return lista
        }
}

class ListaComidasViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(ListaComidasUiState())
    val uiState: StateFlow<ListaComidasUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val resp = api.getComidas()
                if (resp.isSuccessful) {
                    val lista = resp.body()?.map {
                        ComidaListItem(
                            id = it.id_comida,
                            nombre = it.nombre,
                            calorias100g = it.calorias_100g,
                            proteinas100g = it.proteinas_100g,
                            carbohidratos100g = it.carbohidratos_100g,
                            grasas100g = it.grasas_100g,
                            imagen = it.imagen
                        )
                    } ?: emptyList()
                    _uiState.update { it.copy(comidas = lista, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBusquedaChange(v: String) = _uiState.update { it.copy(busqueda = v) }

    fun onFiltroChange(filtro: String) {
        _uiState.update {
            it.copy(filtroActivo = if (it.filtroActivo == filtro) null else filtro)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ListaComidasViewModel(context) as T
    }
}