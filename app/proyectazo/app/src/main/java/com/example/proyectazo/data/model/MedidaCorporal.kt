package com.example.proyectazo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.proyectazo.data.model.Usuario

@Entity(
    tableName = "MEDIDA_CORPORAL",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedidaCorporal(
    @PrimaryKey(autoGenerate = true) val id_medida: Int = 0,
    val id_usuario: Int,
    val peso_kg: Double?,
    val brazo_cm: Double?,
    val cintura_cm: Double?,
    val fecha: Long = System.currentTimeMillis()
)