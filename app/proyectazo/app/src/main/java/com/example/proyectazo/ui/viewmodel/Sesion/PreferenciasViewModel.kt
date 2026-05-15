package com.example.proyectazo.ui.viewmodel.PerfilYAjustes

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

data class PreferenciasUiState(
    val isLoading: Boolean = false,
    val cuentaEliminada: Boolean = false,
    val error: String? = null
)

class PreferenciasViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val sessionManager = SessionManager(context)

    private val _uiState = MutableStateFlow(PreferenciasUiState())
    val uiState: StateFlow<PreferenciasUiState> = _uiState

    fun eliminarCuenta(onEliminado: () -> Unit) {
        val userId = sessionManager.getUserId()
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val resp = api.eliminarUsuario(userId)
                if (resp.isSuccessful) {
                    // Limpiar sesión local completamente
                    context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                    _uiState.update { it.copy(isLoading = false, cuentaEliminada = true) }
                    onEliminado()
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error al eliminar la cuenta: ${resp.code()}")
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
            PreferenciasViewModel(context) as T
    }
}