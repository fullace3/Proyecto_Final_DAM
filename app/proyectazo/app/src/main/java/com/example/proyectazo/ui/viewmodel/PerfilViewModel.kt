package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val alturaCm: String = "-",
    val pesoInicial: String = "-",
    val isLoading: Boolean = true
)

class PerfilViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val session = SessionManager(context)
    private val userId = session.getUserId()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val usuarioDeferred = async { api.getUsuario(userId) }
                val medidasDeferred = async {
                    try { api.getMedidas(userId).body() ?: emptyList() }
                    catch (e: Exception) { emptyList() }
                }

                val usuario = usuarioDeferred.await().body()
                val medidas = medidasDeferred.await().sortedBy { it.fecha }

                val pesoInicial = medidas.firstOrNull()?.peso_kg
                val altura = medidas.lastOrNull()?.altura_cm

                _uiState.value = PerfilUiState(
                    nombre = usuario?.nombre ?: "",
                    email = usuario?.email ?: "",
                    alturaCm = altura?.let { String.format("%.2f", it / 100.0) + " m" } ?: "-",
                    pesoInicial = pesoInicial?.let { String.format("%.1f", it) + " Kg" } ?: "-",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = PerfilUiState(isLoading = false)
            }
        }
    }

    fun cerrarSesion() {
        session.cerrarSesion()   // ← era clearSession()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PerfilViewModel(context) as T
    }
}