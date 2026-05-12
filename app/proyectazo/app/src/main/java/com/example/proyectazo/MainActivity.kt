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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectazo.navigation.NavGraph
import com.example.proyectazo.navigation.Screen
import com.example.proyectazo.navigation.SmartFitNavigationBar
import com.example.proyectazo.navigation.screensWithoutNav
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.theme.ProyectazoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sessionManager = SessionManager(this)
        setContent {
            ProyectazoTheme {
                SmartFitApp(
                    startDestination = if (sessionManager.isLoggedIn())
                        Screen.Home.route
                    else
                        Screen.Login.route,
                    userId = sessionManager.getUserId()
                )
            }
        }
    }
}

@Composable
fun SmartFitApp(
    startDestination: String,
    userId: Int = -1
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in screensWithoutNav

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)),
                exit  = slideOutVertically(targetOffsetY  = { it }, animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                SmartFitNavigationBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            userId = userId,
            modifier = Modifier.padding(innerPadding)
        )
    }
}