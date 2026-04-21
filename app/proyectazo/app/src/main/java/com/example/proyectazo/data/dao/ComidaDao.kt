package com.example.proyectazo.data.dao

import androidx.room.*
import com.example.proyectazo.data.model.Comida

@Dao
interface ComidaDao {

    // CREATE: Registrar un alimento en el catálogo del usuario
    @Insert
    suspend fun insertarComida(comida: Comida)

    // READ: Obtener todos los alimentos registrados por un usuario
    @Query("SELECT * FROM COMIDA WHERE id_usuario = :userId ORDER BY nombre ASC")
    suspend fun obtenerComidasUsuario(userId: Int): List<Comida>

    // READ: Buscar alimentos por nombre (útil para el buscador al crear una dieta)
    @Query("SELECT * FROM COMIDA WHERE id_usuario = :userId AND nombre LIKE '%' || :nombre || '%'")
    suspend fun buscarPorNombre(userId: Int, nombre: String): List<Comida>

    // UPDATE: Corregir los valores nutricionales de un alimento
    @Update
    suspend fun actualizarComida(comida: Comida)

    // DELETE: Borrar un alimento del catálogo
    @Delete
    suspend fun eliminarComida(comida: Comida)
}