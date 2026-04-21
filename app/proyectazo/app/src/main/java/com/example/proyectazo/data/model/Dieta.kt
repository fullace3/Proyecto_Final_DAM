package com.example.proyectazo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "DIETA",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE // Si el usuario se va, sus planes de dieta también
        )
    ]
)
data class Dieta(
    @PrimaryKey(autoGenerate = true) val id_dieta: Int = 0,
    val nombre: String,           // Ejemplo: "Volumen limpio"
    val descripcion: String?,
    val objetivo_calorico: Int,    // Calorías totales diarias
    val proteinas_g: Double,       // Usamos Double para los decimales de los macros
    val carbohidratos_g: Double,
    val grasas_g: Double,
    val fecha_inicio: Long,        // Fecha en la que empieza el plan
    val fecha_fin: Long?,          // Opcional, por si es una dieta indefinida
    val id_usuario: Int            // El usuario al que pertenece este plan
)