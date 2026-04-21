package com.example.proyectazo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "EJERCICIO")
data class Ejercicio(
    @PrimaryKey(autoGenerate = true) val id_ejercicio: Int = 0,
    val nombre: String,
    val grupo_muscular: String?,    // Ejemplo: "Pecho", "Espalda", "Pierna"
    val musculo_principal: String?, // Ejemplo: "Pectoral mayor", "Trapecio"
    val equipamiento: String?,      // Ejemplo: "Barra", "Mancuernas", "Sin equipamiento"
    val descripcion: String?,       // Explicación paso a paso
    val imagen: String?             // Ruta o URL de la imagen del ejercicio
)