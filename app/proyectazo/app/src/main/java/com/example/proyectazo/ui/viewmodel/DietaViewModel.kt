package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ComidaRegistro(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val tipo: String, // "Desayuno", "Almuerzo", "Cena"
    val completada: Boolean = false
)

data class DietaUiState(
    val objetivo: String = "-",
    val calorasRestantes: Int = 0,
    val caloriasTotales: Int = 0,
    val proteina: Pair<Double, Double> = 0.0 to 0.0, // actual a meta
    val carbohidratos: Pair<Double, Double> = 0.0 to 0.0,
    val grasas: Pair<Double, Double> = 0.0 to 0.0,
    val fechaInicio: String = "-",
    val fechaFin: String = "-",
    val comidas: List<ComidaRegistro> = emptyList(),
    val tieneDieta: Boolean? = null, // null = cargando, true = sí, false = no
    val isLoading: Boolean = true
)

class DietaViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()
    private val prefs = context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(DietaUiState())
    val uiState: StateFlow<DietaUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                // Cargar dieta actual
                val dietaResp = api.getDietaActual(userId).body()

                if (dietaResp == null) {
                    // No hay dieta disponible
                    _uiState.value = DietaUiState(tieneDieta = false, isLoading = false)
                    return@launch
                }

                // Cargar objetivo desde SharedPreferences
                val objetivo = prefs.getString("objetivo_usuario", "-") ?: "-"

                val fechaInicio = dietaResp.fecha_inicio.take(10)
                val fechaFin = dietaResp.fecha_fin?.take(10) ?: "-"

                // Calcular calorías restantes (placeholder, debería venir del backend)
                val calorasRestantes = dietaResp.objetivo_calorico - 2200

                _uiState.value = DietaUiState(
                    objetivo = objetivo,
                    caloriasTotales = dietaResp.objetivo_calorico,
                    calorasRestantes = calorasRestantes,
                    proteina = Pair(30.0, dietaResp.proteinas_g),
                    carbohidratos = Pair(12.0, dietaResp.carbohidratos_g),
                    grasas = Pair(12.0, dietaResp.grasas_g),
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    comidas = listOf(
                        // Placeholder: después cargaremos desde la API
                        ComidaRegistro(1, "Desayuno", 450, "Desayuno"),
                        ComidaRegistro(2, "Comida", 660, "Almuerzo"),
                        ComidaRegistro(3, "Pechuga de pollo", 240, "Almuerzo"),
                        ComidaRegistro(4, "Arroz integral", 350, "Almuerzo"),
                        ComidaRegistro(5, "Ensalada mixta", 0, "Cena")
                    ),
                    tieneDieta = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Error o no hay dieta
                _uiState.value = DietaUiState(tieneDieta = false, isLoading = false)
            }
        }
    }

    fun seleccionarDieta(dietaId: Int) {
        // Aquí irían las llamadas para establecer la dieta seleccionada
        // Por ahora simplemente recargamos
        cargar()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DietaViewModel(context) as T
    }
}