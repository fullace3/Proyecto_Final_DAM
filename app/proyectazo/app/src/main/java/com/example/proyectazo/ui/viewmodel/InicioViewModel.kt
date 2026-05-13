package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.DietaResponse
import com.example.proyectazo.network.HistorialDetalleResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.RutinaResponse
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────
//  Estado del entreno para el día seleccionado
// ─────────────────────────────────────────────────────────────────

sealed class EntrenoDelDia {
    object Cargando : EntrenoDelDia()
    object SinEntreno : EntrenoDelDia()
    data class ConEntreno(
        val ejercicios: List<HistorialDetalleResponse>,
        val series: Int,
        val duracionEstimadaMin: Int,
        val rutinaId: Int,
        val rutinaNombre: String?
    ) : EntrenoDelDia()
    data class Error(val mensaje: String) : EntrenoDelDia()
}

// ─────────────────────────────────────────────────────────────────
//  Estado de la dieta
// ─────────────────────────────────────────────────────────────────

sealed class DietaDelDia {
    object Cargando : DietaDelDia()
    object SinDieta : DietaDelDia()
    data class ConDieta(val dieta: DietaResponse) : DietaDelDia()
    data class Error(val mensaje: String) : DietaDelDia()
}

// ─────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────

class InicioViewModel(context: Context) : ViewModel() {

    private val session = SessionManager(context)
    private val userId get() = session.getUserId()

    private var historialCompleto: List<HistorialDetalleResponse> = emptyList()
    private var rutinas: List<RutinaResponse> = emptyList()

    // Día actualmente seleccionado en el DatePicker
    private val _diaSeleccionado = MutableStateFlow(LocalDate.now())
    val diaSeleccionado: StateFlow<LocalDate> = _diaSeleccionado

    private val _entrenoDelDia = MutableStateFlow<EntrenoDelDia>(EntrenoDelDia.Cargando)
    val entrenoDelDia: StateFlow<EntrenoDelDia> = _entrenoDelDia

    private val _dietaDelDia = MutableStateFlow<DietaDelDia>(DietaDelDia.Cargando)
    val dietaDelDia: StateFlow<DietaDelDia> = _dietaDelDia

    init {
        cargarDatos()
    }

    // ── Carga inicial ──────────────────────────────────────────

    private fun cargarDatos() {
        cargarHistorial()
        cargarDieta()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            try {
                val histResp = RetrofitClient.instance.getHistorial(userId)
                val rutResp  = RetrofitClient.instance.getRutinas(userId)
                if (histResp.isSuccessful && histResp.body() != null) {
                    historialCompleto = histResp.body()!!
                    rutinas = if (rutResp.isSuccessful) rutResp.body() ?: emptyList() else emptyList()
                    filtrarEntrenoPorDia(_diaSeleccionado.value)
                } else {
                    _entrenoDelDia.value = EntrenoDelDia.Error("No se pudo cargar el historial")
                }
            } catch (e: Exception) {
                _entrenoDelDia.value = EntrenoDelDia.Error("Sin conexión")
            }
        }
    }

    private fun cargarDieta() {
        viewModelScope.launch {
            try {
                val respuesta = RetrofitClient.instance.getDietaActual(userId)
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    _dietaDelDia.value = DietaDelDia.ConDieta(respuesta.body()!!)
                } else if (respuesta.code() == 404) {
                    _dietaDelDia.value = DietaDelDia.SinDieta
                } else {
                    _dietaDelDia.value = DietaDelDia.Error("Error al cargar la dieta")
                }
            } catch (e: Exception) {
                _dietaDelDia.value = DietaDelDia.Error("Sin conexión")
            }
        }
    }

    // ── Cambio de día desde el DatePicker ─────────────────────

    fun seleccionarDia(nuevoDia: LocalDate) {
        _diaSeleccionado.value = nuevoDia
        filtrarEntrenoPorDia(nuevoDia)
    }

    // ── Filtrado del historial por fecha ──────────────────────

    private fun filtrarEntrenoPorDia(dia: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Las fechas del historial vienen como "2025-08-17T10:30:00"
        // Comparamos solo la parte de la fecha (primeros 10 caracteres)
        val ejerciciosDelDia = historialCompleto.filter { registro ->
            registro.fecha.take(10) == dia.format(formatter)
        }
        val rutinaId = ejerciciosDelDia.firstOrNull()?.id_rutina
        android.util.Log.d("DEBUG", "rutinaId=$rutinaId, rutinas=${rutinas.map { it.id_rutina to it.nombre }}")

        _entrenoDelDia.value = if (ejerciciosDelDia.isEmpty()) {
            EntrenoDelDia.SinEntreno
        } else {
            val totalSeries = ejerciciosDelDia.sumOf { it.series }
            val rutinaId = ejerciciosDelDia.first().id_rutina
            val rutinaNombre = rutinas.find { it.id_rutina == rutinaId }?.nombre
            EntrenoDelDia.ConEntreno(
                ejercicios = ejerciciosDelDia,
                series = totalSeries,
                duracionEstimadaMin = ejerciciosDelDia.maxOf { it.duracion_minutos },
                rutinaId = rutinaId,
                rutinaNombre = rutinaNombre
            )
        }
    }

    // ── Factory ───────────────────────────────────────────────

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InicioViewModel(context) as T
    }
}