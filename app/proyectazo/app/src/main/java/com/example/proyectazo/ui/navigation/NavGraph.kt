package com.example.proyectazo.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectazo.ui.screens.AñadirEjercicioScreen
import com.example.proyectazo.ui.screens.AñadirRegistroScreen
import com.example.proyectazo.ui.screens.CrearRutinaScreen
import com.example.proyectazo.ui.screens.DetalleEjercicioScreen
import com.example.proyectazo.ui.screens.DetallesRutinaScreen
import com.example.proyectazo.ui.screens.EditarRutinaScreen
import com.example.proyectazo.ui.screens.EntrenarScreen
import com.example.proyectazo.ui.screens.FinalizarEntrenamientoScreen
import com.example.proyectazo.ui.screens.EditarPerfilScreen
import com.example.proyectazo.ui.screens.PantallaPerfil
import com.example.proyectazo.ui.screens.PantallaProgreso
import com.example.proyectazo.ui.screens.ResultadoEntrenamiento
import com.example.proyectazo.ui.screens.PantallaIncioSesion
import com.example.proyectazo.ui.screens.PantallaInicio
import com.example.proyectazo.ui.screens.PantallaRegistro
import com.example.proyectazo.ui.screens.PantallaRutinas
import com.example.proyectazo.ui.viewmodel.RutinaConEjercicios

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    userId: Int = 0,
    modifier: Modifier = Modifier
) {
    var rutinaSeleccionada by remember { mutableStateOf<RutinaConEjercicios?>(null) }
    var resultadoEntrenamiento by remember { mutableStateOf<ResultadoEntrenamiento?>(null) }

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
                onRegisterClick = { navController.navigate(Screen.Register.route) }
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
                onCrearRutina = { navController.navigate(Screen.CrearRutina.route) },
                onVerDetalles = { rutina ->
                    rutinaSeleccionada = rutina
                    navController.navigate("detalles_rutina")
                }
            )
        }

        // ── DETALLES RUTINA ───────────────────────────────────────
        composable("detalles_rutina") {
            rutinaSeleccionada?.let { rutina ->
                DetallesRutinaScreen(
                    rutinaConEjercicios = rutina,
                    onBack = { navController.popBackStack() },
                    onEditar = { navController.navigate("editar_rutina/${rutina.rutina.id_rutina}") },
                    onEmpezar = { navController.navigate("entrenar") }
                )
            }
        }

        // ── ENTRENAR ──────────────────────────────────────────────
        composable("entrenar") {
            rutinaSeleccionada?.let { rutina ->
                EntrenarScreen(
                    rutinaConEjercicios = rutina,
                    onTerminar = { resultado ->
                        resultadoEntrenamiento = resultado
                        navController.navigate("finalizar_entrenamiento") {
                            popUpTo("entrenar") { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── FINALIZAR ENTRENAMIENTO ───────────────────────────────
        composable("finalizar_entrenamiento") {
            resultadoEntrenamiento?.let { resultado ->
                FinalizarEntrenamientoScreen(
                    resultado = resultado,
                    onGuardado = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onEliminar = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
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

        // ── EDITAR RUTINA ─────────────────────────────────────────
        composable(
            route = "editar_rutina/{rutinaId}",
            arguments = listOf(navArgument("rutinaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt("rutinaId") ?: 0
            EditarRutinaScreen(
                rutinaId = rutinaId,
                onNavigateBack = { navController.popBackStack() },
                onAnadirEjercicio = { id -> navController.navigate("seleccionar_ejercicio/$id") }
            )
        }

        // ── SELECCIONAR EJERCICIO ─────────────────────────────────
        composable(
            route = "seleccionar_ejercicio/{rutinaId}",
            arguments = listOf(navArgument("rutinaId") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt("rutinaId") ?: 0
            AñadirEjercicioScreen(
                rutinaId = rutinaId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── DIETA ─────────────────────────────────────────────────
        composable(Screen.Dieta.route) { PlaceholderScreen(nombre = "Dieta") }

        // ── PROGRESO ──────────────────────────────────────────────
        composable(Screen.Progreso.route) {
            PantallaProgreso(
                onAñadirRegistro = { navController.navigate("añadir_registro") }
            )
        }

        // ── AÑADIR REGISTRO ───────────────────────────────────────
        composable("añadir_registro") {
            AñadirRegistroScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── PERFIL ────────────────────────────────────────────────
        composable(Screen.Perfil.route) {
            PantallaPerfil(
                onEditarPerfil = { navController.navigate("editar_perfil") },
                onCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── EDITAR PERFIL ──────────────────────────────────────────
        composable("editar_perfil") { backStackEntry -> // <-- Importante: añadimos el parámetro backStackEntry
            val context = androidx.compose.ui.platform.LocalContext.current

            // Usamos el backStackEntry de la pantalla ACTUAL como clave para el remember
            val perfilEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Perfil.route)
            }

            val perfilViewModel: com.example.proyectazo.ui.viewmodel.PerfilViewModel =
                viewModel(perfilEntry, factory = com.example.proyectazo.ui.viewmodel.PerfilViewModel.Factory(context))

            EditarPerfilScreen(
                onBack = { navController.popBackStack() },
                onGuardadoExitoso = {
                    perfilViewModel.recargar()
                    navController.popBackStack()
                }
            )
        }

        // ── DETALLE RUTINA (ruta antigua) ─────────────────────────
        composable(
            route = Screen.DetalleRutina.route,
            arguments = listOf(navArgument(Screen.DetalleRutina.ARG) { type = NavType.IntType })
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