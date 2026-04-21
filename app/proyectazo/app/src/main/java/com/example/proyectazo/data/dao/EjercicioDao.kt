package com.example.proyectazo.data.dao

import androidx.room.*
import com.example.proyectazo.data.model.Ejercicio

@Dao
interface EjercicioDao {

    // CREATE: Añadir un nuevo ejercicio al catálogo
    @Insert
    suspend fun insertarEjercicio(ejercicio: Ejercicio)

    // READ: Obtener todos los ejercicios ordenados alfabéticamente
    @Query("SELECT * FROM EJERCICIO ORDER BY nombre ASC")
    suspend fun obtenerTodos(): List<Ejercicio>

    // READ: Buscar ejercicios por grupo muscular (útil para filtros)
    @Query("SELECT * FROM EJERCICIO WHERE grupo_muscular = :grupo")
    suspend fun obtenerPorGrupo(grupo: String): List<Ejercicio>

    // UPDATE: Corregir el nombre o la descripción
    @Update
    suspend fun actualizarEjercicio(ejercicio: Ejercicio)

    // DELETE: Quitar un ejercicio del catálogo
    @Delete
    suspend fun eliminarEjercicio(ejercicio: Ejercicio)
}