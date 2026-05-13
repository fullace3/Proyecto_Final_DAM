package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.MedidaRequest
import com.example.proyectazo.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class GuardarEstado {
    object Idle : GuardarEstado()
    object Cargando : GuardarEstado()
    object Exito : GuardarEstado()
    data class Error(val mensaje: String) : GuardarEstado()
}

data class AñadirRegistroUiState(
    val dia: String = "",
    val mes: String = "",
    val anio: String = "",
    val fechaError: String? = null,
    val pesoKg: Float = 70f,
    val alturaCm: Float = 170f,
    val brazoCm: String = "",
    val cinturaCm: String = "",
    val pechoCm: String = "",
    val piernaCm: String = "",
    val cargando: Boolean = false,
    val guardarEstado: GuardarEstado = GuardarEstado.Idle
)

class AñadirRegistroViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(AñadirRegistroUiState())
    val uiState: StateFlow<AñadirRegistroUiState> = _uiState

    // ── Setters de campos ────────────────────────────────────────────────────
    fun onDiaChange(v: String) = _uiState.update { it.copy(dia = v, fechaError = null) }
    fun onMesChange(v: String) = _uiState.update { it.copy(mes = v, fechaError = null) }
    fun onAnioChange(v: String) = _uiState.update { it.copy(anio = v, fechaError = null) }
    fun onPesoChange(v: Float) = _uiState.update { it.copy(pesoKg = v) }
    fun onAlturaChange(v: Float) = _uiState.update { it.copy(alturaCm = v) }
    fun onBrazoChange(v: String) = _uiState.update { it.copy(brazoCm = v) }
    fun onCinturaChange(v: String) = _uiState.update { it.copy(cinturaCm = v) }
    fun onPechoChange(v: String) = _uiState.update { it.copy(pechoCm = v) }
    fun onPiernaChange(v: String) = _uiState.update { it.copy(piernaCm = v) }
    fun resetGuardarEstado() = _uiState.update { it.copy(guardarEstado = GuardarEstado.Idle) }

    // ── Guardar ──────────────────────────────────────────────────────────────
    fun guardar(userId: Int) {
        val state = _uiState.value

        // Validar fecha
        val fechaIso = buildFechaIso(state.dia, state.mes, state.anio)
        if (fechaIso == null) {
            _uiState.update { it.copy(fechaError = "Fecha inválida. Usa un día, mes y año reales.") }
            return
        }

        _uiState.update { it.copy(cargando = true, guardarEstado = GuardarEstado.Cargando) }

        val request = MedidaRequest(
            id_usuario = userId,
            peso_kg = state.pesoKg.toDouble(),
            altura_cm = state.alturaCm.toDouble(),
            pecho_cm = state.pechoCm.toDoubleOrNull(),
            pierna_cm = state.piernaCm.toDoubleOrNull(),
            brazo_cm = state.brazoCm.toDoubleOrNull(),
            // Cintura se guarda en grasa_corporal_pct provisionalmente hasta tener columna propia,
            // o puedes dejarlo en null y añadir el campo al modelo cuando amplíes la BD.
            grasa_corporal_pct = null
        )

        viewModelScope.launch {
            try {
                val resp = api.registrarMedida(request)
                if (resp.isSuccessful) {
                    _uiState.update {
                        it.copy(cargando = false, guardarEstado = GuardarEstado.Exito)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            guardarEstado = GuardarEstado.Error("Error ${resp.code()}: no se pudo guardar")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        guardarEstado = GuardarEstado.Error("Sin conexión con el servidor")
                    )
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun buildFechaIso(dia: String, mes: String, anio: String): String? {
        val d = dia.toIntOrNull() ?: return null
        val m = mes.toIntOrNull() ?: return null
        val y = anio.toIntOrNull() ?: return null
        return try {
            val date = LocalDate.of(y, m, d)
            "${date}T00:00:00"
        } catch (e: Exception) {
            null
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AñadirRegistroViewModel(context) as T
    }
}