package com.example.proyectazo.data.dao

import androidx.room.*
import com.example.proyectazo.data.model.Rutina

@Dao
interface RutinaDao {

    // CREATE: Crear una nueva rutina de entrenamiento
    @Insert
    suspend fun crearRutina(rutina: Rutina): Long

    // READ: Listar todas las rutinas de un usuario
    @Query("SELECT * FROM RUTINA WHERE id_usuario = :userId")
    suspend fun obtenerRutinasUsuario(userId: Int): List<Rutina>

    // UPDATE: Cambiar el nombre o la descripción de la rutina
    @Update
    suspend fun editarRutina(rutina: Rutina)

    // DELETE: Eliminar una rutina completa
    @Delete
    suspend fun borrarRutina(rutina: Rutina)
}