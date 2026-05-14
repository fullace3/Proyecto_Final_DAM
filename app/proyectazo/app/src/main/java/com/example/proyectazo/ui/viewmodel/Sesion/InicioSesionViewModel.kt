package com.example.proyectazo.ui.viewmodel.Sesion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.LoginRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Los estados posibles de la pantalla de login
sealed class LoginUiState {
    object Idle : LoginUiState()        // Estado inicial, no ha pasado nada
    object Loading : LoginUiState()     // Esperando respuesta de la API
    object Success : LoginUiState()     // Login correcto
    data class Error(val mensaje: String) : LoginUiState()  // Algo salió mal
}

class LoginViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val sessionManager = SessionManager(context)

    fun login(nombre: String, password: String) {

        if (nombre.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val respuesta = RetrofitClient.instance.login(LoginRequest(nombre, password))

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    sessionManager.guardarSesion(
                        respuesta.body()!!.access_token,
                        respuesta.body()!!.id_usuario
                    )
                    _uiState.value = LoginUiState.Success
                } else if (respuesta.code() == 401) {
                    _uiState.value = LoginUiState.Error("Usuario o contraseña incorrectos")
                } else {
                    _uiState.value = LoginUiState.Error("Error del servidor: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(context) as T
    }
}