package com.example.proyectazo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

val screensWithoutNav = listOf(
    Screen.Login.route,
    Screen.Register.route,
    "crear_rutina",
    "seleccionar_ejercicio/{rutinaId}",
    "editar_rutina/{rutinaId}",
    "detalles_rutina",
    "entrenar"
)

data class NavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    NavItem("Inicio",     Screen.Home.route,    Icons.Filled.Home,         Icons.Outlined.Home),
    NavItem("Ejercicios", Screen.Rutinas.route, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    NavItem("Dieta",      Screen.Dieta.route,   Icons.Filled.Restaurant,   Icons.Outlined.Restaurant),
    NavItem("Progreso",      Screen.Dieta.route,   Icons.Filled.BarChart,   Icons.Outlined.BarChart),
    NavItem("Perfil",     Screen.Perfil.route,  Icons.Filled.Person,       Icons.Outlined.Person),
)

@Composable
fun SmartFitNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}