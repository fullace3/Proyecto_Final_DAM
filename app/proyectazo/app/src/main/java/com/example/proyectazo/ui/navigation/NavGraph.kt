package com.example.proyectazo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectazo.ui.screens.PantallaIncioSesion
import com.example.proyectazo.ui.screens.PantallaRegistro

/**
 * Grafo de navegación central de la app.
 *
 * Cómo añadir una pantalla nueva:
 *  1. Añade su ruta en Screen.kt
 *  2. Crea su @Composable en ui/screens/
 *  3. Añade un bloque composable { } aquí
 *  4. Enlaza la navegación con navController.navigate(Screen.NuevaPantalla.route)
 *
 * @param navController  controlador de navegación inyectado desde MainActivity
 * @param startDestination  pantalla inicial (Login si no hay sesión, Home si ya está logueado)
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── LOGIN ────────────────────────────────────────────
        composable(Screen.Login.route) {
            PantallaIncioSesion(
                onLoginClick = { email, password ->
                    // TODO: llamar al ViewModel de login
                    // Cuando el login sea correcto, navega a Home
                    // y borra Login del backstack para que no se pueda volver atrás
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // ── REGISTER ─────────────────────────────────────────
        composable(Screen.Register.route) {
            PantallaRegistro(
                onRegisterClick = { email, username, password ->
                    // TODO: llamar al ViewModel de registro
                    // Cuando se registre, vuelve al Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // ── HOME (placeholder hasta implementar la pantalla) ──
        composable(Screen.Home.route) {
            // TODO: reemplazar por HomeScreen() cuando esté hecha
            PlaceholderScreen(nombre = "Home") {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }

        // ── RUTINAS ───────────────────────────────────────────
        composable(Screen.Rutinas.route) {
            // TODO: reemplazar por RutinasScreen()
            PlaceholderScreen(nombre = "Rutinas") {}
        }

        // ── DIETA ─────────────────────────────────────────────
        composable(Screen.Dieta.route) {
            // TODO: reemplazar por DietaScreen()
            PlaceholderScreen(nombre = "Dieta") {}
        }

        // ── PROGRESO ──────────────────────────────────────────
        composable(Screen.Progreso.route) {
            // TODO: reemplazar por ProgresoScreen()
            PlaceholderScreen(nombre = "Progreso") {}
        }

        // ── PERFIL ────────────────────────────────────────────
        composable(Screen.Perfil.route) {
            // TODO: reemplazar por PerfilScreen()
            PlaceholderScreen(nombre = "Perfil") {}
        }

        // ── DETALLE RUTINA (ejemplo con argumento) ────────────
        composable(
            route = Screen.DetalleRutina.route,
            arguments = listOf(
                navArgument(Screen.DetalleRutina.ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt(Screen.DetalleRutina.ARG) ?: 0
            // TODO: reemplazar por DetalleRutinaScreen(rutinaId = rutinaId)
            PlaceholderScreen(nombre = "Detalle Rutina $rutinaId") {}
        }
    }
}