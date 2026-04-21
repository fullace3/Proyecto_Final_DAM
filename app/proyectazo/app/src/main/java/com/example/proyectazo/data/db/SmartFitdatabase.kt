package com.example.proyectazo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.proyectazo.data.model.*
import com.example.proyectazo.data.dao.*

@Database(
    entities = [
        Usuario::class,
        MedidaCorporal::class,
        Ejercicio::class,
        Comida::class,
        Rutina::class,
        Dieta::class,
        RutinaEjercicio::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartFitDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun medidaCorporalDao(): MedidaCorporalDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun comidaDao(): ComidaDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun dietaDao(): DietaDao
    abstract fun rutinaEjercicioDao(): RutinaEjercicioDao
}