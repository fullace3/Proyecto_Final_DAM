package com.example.proyectazo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "RUTINA_EJERCICIO",
    primaryKeys = ["id_rutina", "id_ejercicio"], // Clave primaria compuesta
    foreignKeys = [
        ForeignKey(
            entity = Rutina::class,
            parentColumns = ["id_rutina"],
            childColumns = ["id_rutina"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ejercicio::class,
            parentColumns = ["id_ejercicio"],
            childColumns = ["id_ejercicio"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RutinaEjercicio(
    val id_rutina: Int,
    val id_ejercicio: Int,
    val series: Int = 3,           // Valor por defecto
    val repeticiones: Int = 10,    // Valor por defecto
    val peso_kg: Double?,          // El récord del usuario
    val orden: Int                 // Para saber qué ejercicio va primero
)