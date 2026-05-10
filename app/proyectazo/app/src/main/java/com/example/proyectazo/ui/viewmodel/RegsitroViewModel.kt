package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.UsuarioRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val mensaje: String) : RegisterUiState()
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun registrar(nombre: String, email: String, password: String) {

        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val respuesta = RetrofitClient.instance.registro(
                    UsuarioRequest(nombre, email, password)
                )
                if (respuesta.isSuccessful) {
                    _uiState.value = RegisterUiState.Success
                } else if (respuesta.code() == 400) {
                    _uiState.value = RegisterUiState.Error("El usuario o email ya existe")
                } else {
                    _uiState.value = RegisterUiState.Error("Error del servidor: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

class RegisterViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel() as T
    }
}