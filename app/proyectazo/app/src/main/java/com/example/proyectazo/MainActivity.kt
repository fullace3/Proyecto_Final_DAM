package com.example.proyectazo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.proyectazo.navigation.NavGraph
import com.example.proyectazo.navigation.Screen
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.theme.ProyectazoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectazoTheme {
                SmartFitApp(
                    startDestination = if (SessionManager(this).isLoggedIn())
                        Screen.Home.route
                    else
                        Screen.Login.route
                )
            }
        }
    }
}

@Composable
fun SmartFitApp(startDestination: String) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}