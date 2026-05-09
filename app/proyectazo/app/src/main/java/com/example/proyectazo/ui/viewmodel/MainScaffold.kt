package com.example.proyectazo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectazo.navigation.PlaceholderScreen
import com.example.proyectazo.ui.screens.PantallaIncioSesion
import com.example.proyectazo.ui.theme.ProyectazoTheme
import com.example.proyectazo.ui.screens.PantallaRegistro

// ── Rutas de navegación ───────────────────────────────────────
sealed class Screen(val route: String) {
    object Login    : Screen("login")
    object Register : Screen("register")
    object Home     : Screen("home")
    object Ejercicios : Screen("ejercicios")
    object Dieta    : Screen("dieta")
    object Perfil   : Screen("perfil")
}

// ── Modelo de cada ítem del NavigationBar ─────────────────────
data class NavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    NavItem("Inicio",      Screen.Home.route,       Icons.Filled.Home,        Icons.Outlined.Home),
    NavItem("Ejercicios",  Screen.Ejercicios.route, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    NavItem("Dieta",       Screen.Dieta.route,      Icons.Filled.Restaurant,  Icons.Outlined.Restaurant),
    NavItem("Perfil",      Screen.Perfil.route,     Icons.Filled.Person,      Icons.Outlined.Person),
)

// ── Pantallas que NO muestran el NavigationBar ────────────────
private val screensWithoutNav = listOf(Screen.Login.route, Screen.Register.route)

// ── MainActivity ──────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectazoTheme {
                SmartFitApp()
            }
        }
    }
}

// ── Composable raíz ───────────────────────────────────────────
@Composable
fun SmartFitApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // El NavigationBar solo aparece en pantallas principales
    val showBottomBar = currentRoute !in screensWithoutNav

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // AnimatedVisibility hace que el nav bar entre/salga con animación
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                SmartFitNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Autenticación ──────────────────────────────
            composable(Screen.Login.route) {
                PantallaIncioSesion(
                    onLoginClick = { _, _ ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                PantallaRegistro(
                    onRegisterClick = { _, _, _ ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

            // ── Pantallas principales ──────────────────────
            composable(Screen.Home.route) {
                PlaceholderScreen("hola")
            }

            composable(Screen.Ejercicios.route) {
                PlaceholderScreen("hola")
            }

            composable(Screen.Dieta.route) {
                PlaceholderScreen("hola")
            }

            composable(Screen.Perfil.route) {
                PlaceholderScreen("hola",
//                    onLogout = {
//                        navController.navigate(Screen.Login.route) {
//                            popUpTo(0) { inclusive = true }
//                        }
//                    }
                )
            }
        }
    }
}

// ── NavigationBar ─────────────────────────────────────────────
@Composable
fun SmartFitNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Evita acumular pantallas en el back stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                }
            )
        }
    }
}