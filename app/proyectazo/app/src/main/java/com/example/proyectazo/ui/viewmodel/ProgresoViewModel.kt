package com.example.proyectazo.ui.viewmodel

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

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                // Medidas
                val medidasResp = api.getHistorial(userId) // historial para volumen
                val medidasCorporalesResp = try {
                    // Intentamos cargar medidas si existe el endpoint
                    emptyList<MedidaResponse>()
                } catch (e: Exception) { emptyList() }

                // Historial para volumen
                val historial = medidasResp.body() ?: emptyList()

                // Volumen por día
                val today = LocalDate.now()
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                fun volumenPorDia(dias: Int, agrupacion: String): List<Pair<String, Double>> {
                    val inicio = today.minusDays(dias.toLong())
                    val porFecha = mutableMapOf<String, Double>()

                    historial.forEach { h ->
                        val fecha = try {
                            LocalDate.parse(h.fecha.take(10), fmt)
                        } catch (e: Exception) { return@forEach }
                        if (!fecha.isBefore(inicio)) {
                            val key = when (agrupacion) {
                                "semana" -> fecha.dayOfWeek.name.take(3).lowercase()
                                    .replaceFirstChar { it.uppercase() }
                                    .let { listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
                                        .let { days -> listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")[days.indexOf(it).coerceAtLeast(0)] } }
                                "mes" -> fecha.dayOfMonth.toString()
                                else -> "${fecha.monthValue}"
                            }
                            val vol = h.peso_kg * h.repeticiones * h.series
                            porFecha[key] = (porFecha[key] ?: 0.0) + vol
                        }
                    }
                    return porFecha.entries.map { it.key to it.value }
                }

                // Semana: últimos 7 días con etiquetas de día
                val diasSemana = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")
                val volSemana = diasSemana.map { dia ->
                    val dayIndex = diasSemana.indexOf(dia)
                    val fecha = today.minusDays((today.dayOfWeek.value - dayIndex - 1).toLong()
                        .let { if (it < 0) it + 7 else it })
                    val vol = historial.filter {
                        try { LocalDate.parse(it.fecha.take(10), fmt) == fecha } catch (e: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    dia to vol
                }

                // Mes: últimas 4 semanas agrupadas
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

                // Año: por mes
                val meses = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
                val volAnio = meses.mapIndexed { i, mes ->
                    val vol = historial.filter {
                        try { LocalDate.parse(it.fecha.take(10), fmt).monthValue == i + 1 } catch (e: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    mes to vol
                }

                _uiState.value = ProgresoUiState(
                    isLoading = false,
                    volumenSemana = volSemana,
                    volumenMes = volMes,
                    volumenAnio = volAnio,
                    medidas = medidasCorporalesResp
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