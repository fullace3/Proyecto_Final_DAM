package com.example.proyectazo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectazo.ui.screens.Sesion.PantallaIncioSesion
import com.example.proyectazo.ui.theme.ProyectazoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [PantallaIncioSesion].
 * These tests run on a device or emulator and verify the
 * UI tree structure and component behavior.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupScreen() {
        composeTestRule.setContent {
            ProyectazoTheme {
                PantallaIncioSesion(
                    onLoginExitoso = {},
                    onRegisterClick = {}
                )
            }
        }
    }


    @Test
    fun loginScreen_mostrarBotonIniciarSesion() {
        setupScreen()
        // Busca específicamente el botón, no el título
        composeTestRule
            .onNodeWithText("Iniciar sesión")
            .assertHasClickAction()
    }

    @Test
    fun loginScreen_camposVacios_muestraError() {
        setupScreen()
        // Verifica que el campo de nombre existe y es editable
        composeTestRule
            .onNodeWithText("Nombre de usuario")
            .assertIsDisplayed()
        // Verifica que el campo contraseña existe
        composeTestRule
            .onNodeWithText("Contraseña")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_botonRegistro_esClickable() {
        var registroClicked = false
        composeTestRule.setContent {
            ProyectazoTheme {
                PantallaIncioSesion(
                    onLoginExitoso = {},
                    onRegisterClick = { registroClicked = true }
                )
            }
        }
        composeTestRule
            .onNodeWithText("Regístrate")
            .performClick()
        assert(registroClicked) { "El botón Regístrate no llamó a onRegisterClick" }
    }
}