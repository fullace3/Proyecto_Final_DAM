package com.example.proyectazo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectazo.navigation.Screen
import com.example.proyectazo.ui.screens.PantallaIncioSesion
import com.example.proyectazo.ui.screens.PantallaInicio
import com.example.proyectazo.ui.screens.PantallaRegistro

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier // <-- 1. Añadido el parámetro modifier por defecto
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier // <-- 2. Aplicado el modifier al NavHost
    ) {

        // ── LOGIN ────────────────────────────────────────────
        composable(Screen.Login.route) {
            PantallaIncioSesion(
                onLoginExitoso = {
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
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // ── HOME ──────────────────────────────────────────────
        composable(Screen.Home.route) {
            PantallaInicio(usuario = "usuario")
        }

        // ── RUTINAS ───────────────────────────────────────────
        composable(Screen.Rutinas.route) {
            PlaceholderScreen(nombre = "Rutinas") {}
        }

        // ── DIETA ─────────────────────────────────────────────
        composable(Screen.Dieta.route) {
            PlaceholderScreen(nombre = "Dieta") {}
        }

        // ── PROGRESO ──────────────────────────────────────────
        composable(Screen.Progreso.route) {
            PlaceholderScreen(nombre = "Progreso") {}
        }

        // ── PERFIL ────────────────────────────────────────────
        composable(Screen.Perfil.route) {
            PlaceholderScreen(nombre = "Perfil") {}
        }

        // ── DETALLE RUTINA (con argumento) ────────────────────
        composable(
            route = Screen.DetalleRutina.route,
            arguments = listOf(
                navArgument(Screen.DetalleRutina.ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt(Screen.DetalleRutina.ARG) ?: 0
            PlaceholderScreen(nombre = "Detalle Rutina $rutinaId") {}
        }
    }
}