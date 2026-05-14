package com.example.proyectazo.ui.viewmodel.ProgresoYRegistro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.MedidaResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ProgresoUiState(
    val pesoActual: String = "-",
    val pesoInicial: String = "-",
    val diferenciaPeso: String = "-",
    val imc: String = "-",
    val volumenSemana: List<Pair<String, Double>> = emptyList(),
    val volumenMes: List<Pair<String, Double>> = emptyList(),
    val volumenAnio: List<Pair<String, Double>> = emptyList(),
    val medidas: List<MedidaResponse> = emptyList(),
    val isLoading: Boolean = true
)

class ProgresoViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(ProgresoUiState())
    val uiState: StateFlow<ProgresoUiState> = _uiState

    init { recargar() }

    // Público para poder llamarlo desde la pantalla al volver del formulario
    fun recargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val historial = api.getHistorial(userId).body() ?: emptyList()

                // Medidas corporales reales desde la API
                val medidas = try {
                    api.getMedidas(userId).body() ?: emptyList()
                } catch (e: Exception) { emptyList() }

                val today = LocalDate.now()
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                // ── Peso actual e inicial desde medidas ───────────────
                val medidasOrdenadas = medidas.sortedBy { it.fecha }
                val pesoActual = medidasOrdenadas.lastOrNull()?.peso_kg
                val pesoInicial = medidasOrdenadas.firstOrNull()?.peso_kg
                val alturaCm = medidasOrdenadas.lastOrNull()?.altura_cm

                val diferencia = if (pesoActual != null && pesoInicial != null)
                    String.format("%.1f", pesoActual - pesoInicial) else "-"

                val imc = if (pesoActual != null && alturaCm != null && alturaCm > 0) {
                    val alturaM = alturaCm / 100.0
                    String.format("%.1f", pesoActual / (alturaM * alturaM))
                } else "-"

                // ── Volumen semana: día a día ──────────────────────────
                val diasSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                val volSemana = diasSemana.mapIndexed { dayIndex, dia ->
                    val offset = (today.dayOfWeek.value - dayIndex - 1).let { if (it < 0) it + 7 else it }
                    val fecha = today.minusDays(offset.toLong())
                    val vol = historial.filter {
                        try { LocalDate.parse(it.fecha.take(10), fmt) == fecha } catch (e: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    dia to vol
                }

                // ── Volumen mes: últimas 4 semanas ────────────────────
                val volMes = (1..4).map { semana ->
                    val finSemana = today.minusDays(((semana - 1) * 7).toLong())
                    val inicioSemana = finSemana.minusDays(6)
                    val vol = historial.filter {
                        try {
                            val f = LocalDate.parse(it.fecha.take(10), fmt)
                            !f.isBefore(inicioSemana) && !f.isAfter(finSemana)
                        } catch (e: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    "S${5 - semana}" to vol
                }.reversed()

                // ── Volumen año: por mes ───────────────────────────────
                val meses = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
                val volAnio = meses.mapIndexed { i, mes ->
                    val vol = historial.filter {
                        try {
                            LocalDate.parse(it.fecha.take(10), fmt).monthValue == i + 1
                        } catch (e: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    mes to vol
                }

                _uiState.value = ProgresoUiState(
                    isLoading = false,
                    pesoActual = pesoActual?.let { String.format("%.1f", it) } ?: "-",
                    pesoInicial = pesoInicial?.let { String.format("%.1f", it) } ?: "-",
                    diferenciaPeso = diferencia,
                    imc = imc,
                    volumenSemana = volSemana,
                    volumenMes = volMes,
                    volumenAnio = volAnio,
                    medidas = medidasOrdenadas.reversed() // más reciente primero
                )
            } catch (e: Exception) {
                _uiState.value = ProgresoUiState(isLoading = false)
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProgresoViewModel(context) as T
    }
}