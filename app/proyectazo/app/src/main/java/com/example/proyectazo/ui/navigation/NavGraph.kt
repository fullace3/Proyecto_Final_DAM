package com.example.proyectazo.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectazo.ui.screens.AñadirEjercicioScreen
import com.example.proyectazo.ui.screens.CrearRutinaScreen
import com.example.proyectazo.ui.screens.PantallaIncioSesion
import com.example.proyectazo.ui.screens.PantallaInicio
import com.example.proyectazo.ui.screens.PantallaRegistro
import com.example.proyectazo.ui.screens.PantallaRutinas

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    userId: Int = 0,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ── LOGIN ────────────────────────────────────────────────
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

        // ── REGISTER ─────────────────────────────────────────────
        composable(Screen.Register.route) {
            PantallaRegistro(
                onRegistroExitoso = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        // ── HOME ──────────────────────────────────────────────────
        composable(Screen.Home.route) {
            PantallaInicio(usuario = "usuario")
        }

        // ── RUTINAS ───────────────────────────────────────────────
        composable(Screen.Rutinas.route) {
            PantallaRutinas(
                onCrearRutina = { navController.navigate(Screen.CrearRutina.route) }
            )
        }

        // ── CREAR RUTINA ──────────────────────────────────────────
        composable("crear_rutina") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = remember { com.example.proyectazo.network.SessionManager(context) }
            CrearRutinaScreen(
                userId = sessionManager.getUserId(),
                onNavigateBack = { navController.popBackStack() },
                onAnadirEjercicio = { rutinaId ->
                    navController.navigate("seleccionar_ejercicio/$rutinaId")
                }
            )
        }

        // ── SELECCIONAR EJERCICIO ─────────────────────────────────
        composable(
            route = "seleccionar_ejercicio/{rutinaId}",
            arguments = listOf(
                navArgument("rutinaId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt("rutinaId") ?: 0
            AñadirEjercicioScreen(
                rutinaId = rutinaId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── DIETA ─────────────────────────────────────────────────
        composable(Screen.Dieta.route) {
            PlaceholderScreen(nombre = "Dieta")
        }

        // ── PROGRESO ──────────────────────────────────────────────
        composable(Screen.Progreso.route) {
            PlaceholderScreen(nombre = "Progreso")
        }

        // ── PERFIL ────────────────────────────────────────────────
        composable(Screen.Perfil.route) {
            PlaceholderScreen(nombre = "Perfil")
        }

        // ── DETALLE RUTINA ────────────────────────────────────────
        composable(
            route = Screen.DetalleRutina.route,
            arguments = listOf(
                navArgument(Screen.DetalleRutina.ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt(Screen.DetalleRutina.ARG) ?: 0
            PlaceholderScreen(nombre = "Detalle Rutina $rutinaId")
        }
    }
}

@Composable
fun PlaceholderScreen(nombre: String) {
    Text(text = nombre)
}