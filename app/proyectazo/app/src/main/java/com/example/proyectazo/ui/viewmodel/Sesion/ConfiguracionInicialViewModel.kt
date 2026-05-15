package com.example.proyectazo.ui.viewmodel.Sesion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.MedidaRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfiguracionInicialUiState(
    val peso: String = "",
    val altura: String = "",
    val horaEntrenamiento: Pair<Int, Int> = Pair(20, 0),
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null,
    val errorPeso: String? = null,
    val errorAltura: String? = null
)

class ConfiguracionInicialViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val sessionManager = SessionManager(context)
    private val userId = sessionManager.getUserId()

    private val _uiState = MutableStateFlow(ConfiguracionInicialUiState())
    val uiState: StateFlow<ConfiguracionInicialUiState> = _uiState

    fun onPesoChange(v: String) = _uiState.update { it.copy(peso = v, errorPeso = null) }
    fun onAlturaChange(v: String) = _uiState.update { it.copy(altura = v, errorAltura = null) }
    fun onHoraChange(hora: Int, minuto: Int) = _uiState.update { it.copy(horaEntrenamiento = Pair(hora, minuto)) }

    fun guardar(onExitoso: () -> Unit) {
        val state = _uiState.value

        // Validaciones
        val pesoNum = state.peso.toDoubleOrNull()
        val alturaNum = state.altura.toDoubleOrNull()
        var hayError = false

        if (pesoNum == null || pesoNum <= 0) {
            _uiState.update { it.copy(errorPeso = "Introduce un peso válido") }
            hayError = true
        }
        if (alturaNum == null || alturaNum <= 0) {
            _uiState.update { it.copy(errorAltura = "Introduce una altura válida") }
            hayError = true
        }
        if (hayError) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Guardar peso y altura como medida corporal inicial
                val resp = api.registrarMedida(
                    MedidaRequest(
                        id_usuario = userId,
                        peso_kg = pesoNum!!,
                        altura_cm = alturaNum
                    )
                )

                if (resp.isSuccessful) {
                    // Guardar hora de entrenamiento y flag de configuración en SharedPreferences
                    val horaStr = "%02d:%02d".format(
                        state.horaEntrenamiento.first,
                        state.horaEntrenamiento.second
                    )
                    context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("configuracion_completada", true)
                        .putString("hora_entrenamiento", horaStr)
                        .apply()

                    _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                    onExitoso()
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error al guardar: ${resp.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ConfiguracionInicialViewModel(context) as T
    }
}