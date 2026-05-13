package com.example.proyectazo.ui.viewmodel

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

sealed class EditarGuardarEstado {
    object Idle : EditarGuardarEstado()
    object Cargando : EditarGuardarEstado()
    object Exito : EditarGuardarEstado()
    data class Error(val mensaje: String) : EditarGuardarEstado()
}

data class EditarPerfilUiState(
    // Campos de usuario
    val nombre: String = "",
    val email: String = "",
    // Campos de medida
    val edad: String = "",
    val alturaCm: String = "",
    val sexo: String = "",
    val pesoKg: String = "",
    val objetivo: String = "",
    // Estado
    val isLoading: Boolean = true,
    val guardarEstado: EditarGuardarEstado = EditarGuardarEstado.Idle
)

class EditarPerfilViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val session = SessionManager(context)
    private val userId = session.getUserId()

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val usuario = api.getUsuario(userId).body()
                val medidas = try {
                    api.getMedidas(userId).body()
                        ?.sortedBy { it.fecha }
                        ?: emptyList()
                } catch (e: Exception) { emptyList() }

                val ultima = medidas.lastOrNull()

                _uiState.value = EditarPerfilUiState(
                    nombre = usuario?.nombre ?: "",
                    email = usuario?.email ?: "",
                    alturaCm = ultima?.altura_cm?.let { "${it.toInt()}" } ?: "",
                    pesoKg = ultima?.peso_kg?.let { String.format("%.1f", it) } ?: "",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Setters ──────────────────────────────────────────────────────────────
    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onEdadChange(v: String) = _uiState.update { it.copy(edad = v) }
    fun onAlturaChange(v: String) = _uiState.update { it.copy(alturaCm = v) }
    fun onSexoChange(v: String) = _uiState.update { it.copy(sexo = v) }
    fun onPesoChange(v: String) = _uiState.update { it.copy(pesoKg = v) }
    fun onObjetivoChange(v: String) = _uiState.update { it.copy(objetivo = v) }
    fun resetEstado() = _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Idle) }

    // ── Guardar ──────────────────────────────────────────────────────────────
    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Cargando) }

        viewModelScope.launch {
            try {
                // Guardar medida con los nuevos valores de peso y altura
                val peso = state.pesoKg.toDoubleOrNull()
                val altura = state.alturaCm.toDoubleOrNull()

                if (peso != null) {
                    val request = MedidaRequest(
                        id_usuario = userId,
                        peso_kg = peso,
                        altura_cm = altura
                    )
                    val resp = api.registrarMedida(request)
                    if (!resp.isSuccessful) {
                        _uiState.update {
                            it.copy(guardarEstado = EditarGuardarEstado.Error("Error al guardar: ${resp.code()}"))
                        }
                        return@launch
                    }
                }

                // Nota: actualizar nombre/email requiere password en UsuarioRequest.
                // Pendiente cuando el backend exponga un endpoint PATCH o se añada campo password.

                _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Exito) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardarEstado = EditarGuardarEstado.Error("Sin conexión con el servidor"))
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditarPerfilViewModel(context) as T
    }
}