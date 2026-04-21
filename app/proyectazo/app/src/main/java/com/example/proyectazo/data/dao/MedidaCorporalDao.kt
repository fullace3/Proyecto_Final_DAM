package com.example.proyectazo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectazo.data.model.MedidaCorporal

@Dao
interface MedidaCorporalDao {

    // Crear: Registrar una nueva medición
    @Insert
    suspend fun insertarMedida(medida: MedidaCorporal)

    // Leer: Obtener todas las medidas de un usuario para el gráfico de evolución
    @Query("SELECT * FROM MEDIDA_CORPORAL WHERE id_usuario = :userId ORDER BY fecha ASC")
    suspend fun obtenerHistorialUsuario(userId: Int): List<MedidaCorporal>

    // Actualizar: Por si el usuario se equivoca al anotar su peso
    @Update
    suspend fun editarMedida(medida: MedidaCorporal)

    // Borrar: Eliminar un registro específico
    @Delete
    suspend fun eliminarMedida(medida: MedidaCorporal)
}