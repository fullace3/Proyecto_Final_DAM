package com.example.proyectazo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "COMIDA",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comida(
    @PrimaryKey(autoGenerate = true) val id_comida: Int = 0,
    val nombre: String,             // Ejemplo: "Pechuga de pollo"
    val calorias_100g: Int,         // Calorías por cada 100g del alimento
    val proteinas_100g: Int,        // Gramos de proteína por 100g
    val carbohidratos_100g: Int,    // Gramos de hidratos por 100g
    val grasas_100g: Int,           // Gramos de grasa por 100g
    val id_usuario: Int,            // El dueño de este registro de comida
    val imagen: String?             // Ruta o URL de la imagen del alimento
)