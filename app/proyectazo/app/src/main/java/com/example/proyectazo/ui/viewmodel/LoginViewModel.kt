package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
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

    fun login(email: String, password: String) {

        // Validación básica antes de llamar a la API
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val respuesta = RetrofitClient.instance.login(LoginRequest(email, password))

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val token = respuesta.body()!!.access_token

                    // Con el token, pedimos los datos del usuario para obtener su ID
                    // Tu API de login solo devuelve el token, así que decodificamos el ID
                    // desde el propio token o hacemos una segunda llamada
                    // Por simplicidad guardamos lo que tenemos y navegamos
                    sessionManager.guardarSesion(token, respuesta.body()!!.id_usuario)

                    // Llamamos a /usuarios con el email para obtener el ID real
                    // Como no tienes ese endpoint directo, usaremos el token JWT
                    // El ID está en el payload del token que genera tu FastAPI
                    _uiState.value = LoginUiState.Success

                } else if (respuesta.code() == 401) {
                    _uiState.value = LoginUiState.Error("Email o contraseña incorrectos")
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

class LoginViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(context) as T
    }
}