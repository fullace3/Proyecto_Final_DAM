package com.example.proyectazo.data.dao

import androidx.room.*
import com.example.proyectazo.data.model.RutinaEjercicio

@Dao
interface RutinaEjercicioDao {

    // CREATE: Añadir un ejercicio a una rutina específica
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun añadirEjercicioARutina(relacion: RutinaEjercicio)

    // READ: Obtener todos los ejercicios de una rutina concreta ordenados
    @Query("SELECT * FROM RUTINA_EJERCICIO WHERE id_rutina = :rutinaId ORDER BY orden ASC")
    suspend fun obtenerEjerciciosDeRutina(rutinaId: Int): List<RutinaEjercicio>

    // UPDATE: Cambiar series, reps o el peso
    @Update
    suspend fun actualizarDatosEjercicio(relacion: RutinaEjercicio)

    // DELETE: Quitar un ejercicio de una rutina
    @Delete
    suspend fun quitarEjercicioDeRutina(relacion: RutinaEjercicio)
}