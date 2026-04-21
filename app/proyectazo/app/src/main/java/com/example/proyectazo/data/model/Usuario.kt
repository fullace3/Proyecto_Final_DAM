package com.example.proyectazo.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "USUARIO")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val nombre: String,
    @ColumnInfo(name = "email") val email: String,
    val password_hash: String,
    val fecha_registro: Long = System.currentTimeMillis()
)