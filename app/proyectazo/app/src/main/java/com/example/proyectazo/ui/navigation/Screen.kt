package com.example.proyectazo.navigation

/**
 * Define TODAS las pantallas de la app como objetos sellados.
 * Para añadir una pantalla nueva solo hay que añadir un objeto aquí.
 *
 * - Sin argumentos  → object
 * - Con argumentos  → data class con {argumento} en la ruta
 */
sealed class Screen(val route: String) {

    // ── AUTH ──────────────────────────────────────────────────
    object Login    : Screen("login")
    object Register : Screen("register")

    // ── MAIN (futuras pantallas) ───────────────────────────────
    object Home     : Screen("home")
    object Rutinas  : Screen("rutinas")
    object Dieta    : Screen("dieta")
    object Progreso : Screen("progreso")
    object Perfil   : Screen("perfil")

    // ── EJEMPLO CON ARGUMENTO ─────────────────────────────────
    // Para navegar: navController.navigate(Screen.DetalleRutina.createRoute(123))
    object DetalleRutina : Screen("detalle_rutina/{rutinaId}") {
        fun createRoute(rutinaId: Int) = "detalle_rutina/$rutinaId"
        const val ARG = "rutinaId"
    }
}