package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.DietaComidaRequest
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
    val tipo: String = "Desayuno",
    val uid: Long = System.nanoTime(),
    val dietaComidaId: Int? = null // ID en tabla DIETA_COMIDA (para edición)
)

data class CrearDietaUiState(
    val nombreDieta: String = "",
    val objetivo: String = "",
    val alimentos: List<AlimentoItem> = emptyList(),
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null,
    val editando: Boolean = false,
    val dietaId: Int? = null
) {
    val proteinas: Int get() = alimentos.sumOf { it.proteinas }
    val carbohidratos: Int get() = alimentos.sumOf { it.carbohidratos }
    val grasas: Int get() = alimentos.sumOf { it.grasas }
    val calorasTotales: Int get() = alimentos.sumOf { it.calorias }
}

class CrearDietaViewModel(
    private val context: Context,
    private val dietaIdEditar: Int? = null
) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(CrearDietaUiState())
    val uiState: StateFlow<CrearDietaUiState> = _uiState

    init {
        if (dietaIdEditar != null) cargarDietaExistente(dietaIdEditar)
    }

    private fun cargarDietaExistente(dietaId: Int) {
        _uiState.update { it.copy(isLoading = true, editando = true, dietaId = dietaId) }
        viewModelScope.launch {
            try {
                // Cargar datos de la dieta
                val dietasResp = api.getDietasUsuario(userId)
                val dieta = dietasResp.body()?.find { it.id_dieta == dietaId }

                if (dieta != null) {
                    _uiState.update {
                        it.copy(nombreDieta = dieta.nombre)
                    }
                }

                // Cargar comidas de la dieta
                val comidasResp = api.getComidasDeDieta(dietaId)
                if (comidasResp.isSuccessful) {
                    val dietaComidas = comidasResp.body() ?: emptyList()
                    val alimentos = dietaComidas.mapNotNull { dc ->
                        val comida = dc.comida
                        if (comida != null) {
                            AlimentoItem(
                                id = comida.id_comida,
                                nombre = comida.nombre,
                                calorias = comida.calorias_100g,
                                proteinas = comida.proteinas_100g,
                                carbohidratos = comida.carbohidratos_100g,
                                grasas = comida.grasas_100g,
                                dia = dc.dia,
                                tipo = dc.tipo,
                                dietaComidaId = dc.id
                            )
                        } else null
                    }
                    _uiState.update { it.copy(alimentos = alimentos, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

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
        val nombre = state.nombreDieta.ifBlank { "Nueva dieta" }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val ahora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val request = DietaRequest(
                    nombre = nombre,
                    objetivo_calorico = state.calorasTotales,
                    proteinas_g = state.proteinas.toDouble(),
                    carbohidratos_g = state.carbohidratos.toDouble(),
                    grasas_g = state.grasas.toDouble(),
                    fecha_inicio = ahora,
                    id_usuario = userId
                )
                val resp = api.crearDieta(request)
                if (resp.isSuccessful) {
                    val dietaCreada = resp.body()
                    // Guardar alimentos en DIETA_COMIDA
                    if (dietaCreada != null) {
                        state.alimentos.forEach { alimento ->
                            api.añadirComidaADieta(
                                DietaComidaRequest(
                                    id_dieta = dietaCreada.id_dieta,
                                    id_comida = alimento.id,
                                    tipo = alimento.tipo,
                                    dia = alimento.dia ?: "Lun"
                                )
                            )
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                    onExitoso()
                } else {
                    val errorBody = resp.errorBody()?.string() ?: "Error ${resp.code()}"
                    _uiState.update { it.copy(isLoading = false, error = errorBody) }
                    android.widget.Toast.makeText(context, "Error: $errorBody", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    class Factory(private val context: Context, private val dietaId: Int? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrearDietaViewModel(context, dietaId) as T
    }
}