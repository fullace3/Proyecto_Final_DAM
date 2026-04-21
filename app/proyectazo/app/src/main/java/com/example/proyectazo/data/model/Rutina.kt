package com.example.proyectazo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "RUTINA",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE // Si el usuario borra su cuenta, adiós a sus rutinas
        )
    ]
)
data class Rutina(
    @PrimaryKey(autoGenerate = true) val id_rutina: Int = 0,
    val nombre: String,           // Ejemplo: "Rutina de Verano"
    val descripcion: String?,     // Notas sobre la rutina
    val id_usuario: Int           // El ID del usuario que la creó
)