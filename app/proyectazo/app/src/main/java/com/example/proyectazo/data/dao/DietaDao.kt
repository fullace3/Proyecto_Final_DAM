package com.example.proyectazo.data.dao

import androidx.room.*
import com.example.proyectazo.data.model.Dieta

@Dao
interface DietaDao {

    // CREATE: Crear un nuevo plan nutricional
    @Insert
    suspend fun crearDieta(dieta: Dieta)

    // READ: Ver todos los planes de un usuario
    @Query("SELECT * FROM DIETA WHERE id_usuario = :userId ORDER BY fecha_inicio DESC")
    suspend fun obtenerDietasUsuario(userId: Int): List<Dieta>

    // READ: Obtener la dieta activa (la última creada)
    @Query("SELECT * FROM DIETA WHERE id_usuario = :userId ORDER BY fecha_inicio DESC LIMIT 1")
    suspend fun obtenerDietaActual(userId: Int): Dieta?

    // UPDATE: Ajustar los macronutrientes o calorías
    @Update
    suspend fun actualizarDieta(dieta: Dieta)

    // DELETE: Borrar un plan de dieta
    @Delete
    suspend fun borrarDieta(dieta: Dieta)
}