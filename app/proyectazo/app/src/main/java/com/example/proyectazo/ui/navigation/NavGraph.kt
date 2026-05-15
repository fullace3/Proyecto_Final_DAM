package com.example.proyectazo.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.proyectazo.ui.screens.RutinasYEjercicio.AñadirEjercicioScreen
import com.example.proyectazo.ui.screens.ProgresoYRegistros.AñadirRegistroScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.CrearRutinaScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.DetalleEjercicioScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.DetallesRutinaScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.EditarRutinaScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.EntrenarScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.FinalizarEntrenamientoScreen
import com.example.proyectazo.ui.screens.DietasYComidas.CrearDietaScreen
import com.example.proyectazo.ui.screens.DietasYComidas.DietaScreen
import com.example.proyectazo.ui.screens.DietasYComidas.ListaComidasScreen
import com.example.proyectazo.ui.screens.DietasYComidas.DetalleComidaScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.EditarPerfilScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.PantallaPerfil
import com.example.proyectazo.ui.screens.PerfilYAjustes.PreferenciasScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.TerminosCondicionesScreen
import com.example.proyectazo.ui.screens.ProgresoYRegistros.PantallaProgreso
import com.example.proyectazo.ui.screens.RutinasYEjercicio.ResultadoEntrenamiento
import com.example.proyectazo.ui.screens.Sesion.PantallaIncioSesion
import com.example.proyectazo.ui.screens.PantallaInicio
import com.example.proyectazo.ui.screens.Sesion.PantallaRegistro
import com.example.proyectazo.ui.screens.RutinasYEjercicio.PantallaRutinas
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinaConEjercicios
import androidx.compose.runtime.collectAsState
import com.example.proyectazo.ui.screens.Sesion.ConfiguracionInicialScreen

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
                    navController.navigate("configuracion_inicial") {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable("configuracion_inicial") {
            ConfiguracionInicialScreen(
                onBack = { navController.popBackStack() },
                onConfigurado = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
        composable(Screen.Dieta.route) { backStackEntry ->
            val recargar = backStackEntry.savedStateHandle.get<Boolean>("recargar") ?: false
            val context = androidx.compose.ui.platform.LocalContext.current
            val dietaViewModel: com.example.proyectazo.ui.viewmodel.DietaYComida.DietaViewModel = viewModel(
                factory = com.example.proyectazo.ui.viewmodel.DietaYComida.DietaViewModel.Factory(context)
            )

            LaunchedEffect(recargar) {
                if (recargar) {
                    dietaViewModel.cargar()
                    backStackEntry.savedStateHandle.remove<Boolean>("recargar")
                }
            }

            DietaScreen(
                onCrearDieta = { navController.navigate("crear_dieta") }
            )
        }

        // ── CREAR DIETA ───────────────────────────────────────────
        composable("crear_dieta") { backStackEntry ->
            val savedState = backStackEntry.savedStateHandle

            val comidaId by savedState.getStateFlow<Int?>("comida_id", null).collectAsState()
            val comidaNombre by savedState.getStateFlow<String?>("comida_nombre", null).collectAsState()
            val comidaCalorias by savedState.getStateFlow<Int?>("comida_calorias", null).collectAsState()
            val comidaProteinas by savedState.getStateFlow<Int?>("comida_proteinas", null).collectAsState()
            val comidaCarbos by savedState.getStateFlow<Int?>("comida_carbos", null).collectAsState()
            val comidaGrasas by savedState.getStateFlow<Int?>("comida_grasas", null).collectAsState()
            val comidaTipo by savedState.getStateFlow<String?>("comida_tipo", null).collectAsState()
            val comidaDia by savedState.getStateFlow<String?>("comida_dia", null).collectAsState()

            val currentComidaId = comidaId
            val currentComidaNombre = comidaNombre

            CrearDietaScreen(
                onBack = { navController.popBackStack() },
                onGuardadoExitoso = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("recargar", true)
                    navController.popBackStack()
                },
                onAñadirAlimento = { tipo, dia ->
                    savedState["comida_tipo"] = tipo
                    savedState["comida_dia"] = dia
                    navController.navigate("lista_comidas")
                },
                comidaAñadida = if (currentComidaId != null && currentComidaNombre != null) {
                    Triple(currentComidaId, currentComidaNombre, comidaCalorias ?: 0)
                } else null,
                comidaAñadidaMacros = if (currentComidaId != null) {
                    Triple(comidaProteinas ?: 0, comidaCarbos ?: 0, comidaGrasas ?: 0)
                } else null,
                comidaTipo = comidaTipo,
                comidaDia = comidaDia,
                onComidaConsumida = {
                    savedState.remove<Int>("comida_id")
                    savedState.remove<String>("comida_nombre")
                    savedState.remove<Int>("comida_calorias")
                    savedState.remove<Int>("comida_proteinas")
                    savedState.remove<Int>("comida_carbos")
                    savedState.remove<Int>("comida_grasas")
                    savedState.remove<String>("comida_tipo")
                    savedState.remove<String>("comida_dia")
                }
            )
        }

        // ── LISTA COMIDAS ────────────────────────────────────────
        composable("lista_comidas") { backStackEntry ->
            val comidaId = backStackEntry.savedStateHandle.get<Int>("comida_id")

            LaunchedEffect(comidaId) {
                if (comidaId != null) {
                    val prev = navController.previousBackStackEntry
                    prev?.savedStateHandle?.set("comida_id", comidaId)
                    prev?.savedStateHandle?.set("comida_nombre", backStackEntry.savedStateHandle.get<String>("comida_nombre"))
                    prev?.savedStateHandle?.set("comida_calorias", backStackEntry.savedStateHandle.get<Int>("comida_calorias"))
                    prev?.savedStateHandle?.set("comida_proteinas", backStackEntry.savedStateHandle.get<Int>("comida_proteinas"))
                    prev?.savedStateHandle?.set("comida_carbos", backStackEntry.savedStateHandle.get<Int>("comida_carbos"))
                    prev?.savedStateHandle?.set("comida_grasas", backStackEntry.savedStateHandle.get<Int>("comida_grasas"))
                    backStackEntry.savedStateHandle.remove<Int>("comida_id")
                    navController.popBackStack()
                }
            }

            if (comidaId == null) {
                ListaComidasScreen(
                    onBack = { navController.popBackStack() },
                    onComidaSeleccionada = { id -> navController.navigate("detalle_comida/$id") }
                )
            }
        }

        // ── DETALLE COMIDA ───────────────────────────────────────
        composable(
            "detalle_comida/{comidaId}",
            arguments = listOf(navArgument("comidaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val comidaId = backStackEntry.arguments?.getInt("comidaId") ?: 0
            DetalleComidaScreen(
                comidaId = comidaId,
                onBack = { navController.popBackStack() },
                onAñadir = { id, nombre, calorias, proteinas, carbos, grasas ->
                    navController.previousBackStackEntry?.savedStateHandle?.let { handle ->
                        handle["comida_id"] = id
                        handle["comida_nombre"] = nombre
                        handle["comida_calorias"] = calorias
                        handle["comida_proteinas"] = proteinas
                        handle["comida_carbos"] = carbos
                        handle["comida_grasas"] = grasas
                    }
                    navController.popBackStack()
                }
            )
        }

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
        composable(Screen.Perfil.route) { navBackStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val perfilEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(Screen.Perfil.route)
            }
            val perfilViewModel: com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel =
                viewModel(perfilEntry, factory = com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel.Factory(context))

            PantallaPerfil(
                viewModel = perfilViewModel,
                onEditarPerfil = { navController.navigate("editar_perfil") },
                onPreferencias = { navController.navigate("preferencias") },
                onCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── EDITAR PERFIL ──────────────────────────────────────────
        composable("editar_perfil") { navBackStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val perfilEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(Screen.Perfil.route)
            }
            val perfilViewModel: com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel =
                viewModel(perfilEntry, factory = com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel.Factory(context))

            EditarPerfilScreen(
                onBack = { navController.popBackStack() },
                onGuardadoExitoso = {
                    perfilViewModel.recargar()
                    navController.popBackStack()
                }
            )
        }

        // ── PREFERENCIAS ───────────────────────────────────────────
        composable("preferencias") {
            PreferenciasScreen(
                onBack = { navController.popBackStack() },
                onTerminos = { navController.navigate("terminos_condiciones") },
                onEliminarCuenta = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── TÉRMINOS Y CONDICIONES ──────────────────────────────────
        composable("terminos_condiciones") {
            TerminosCondicionesScreen(onBack = { navController.popBackStack() })
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