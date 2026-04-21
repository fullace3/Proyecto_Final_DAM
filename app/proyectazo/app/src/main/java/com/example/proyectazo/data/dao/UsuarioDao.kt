package com.example.proyectazo.data.dao

import androidx.room.*
import com.example.proyectazo.data.model.Usuario

@Dao
interface UsuarioDao {

    // CREATE: Registrar un nuevo usuario en la aplicación
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(usuario: Usuario): Long

    // READ: Buscar un usuario por su email (útil para el Inicio de Sesión)
    @Query("SELECT * FROM USUARIO WHERE email = :email LIMIT 1")
    suspend fun login(email: String): Usuario?

    // READ: Obtener todos los usuarios registrados (para control del sistema)
    @Query("SELECT * FROM USUARIO")
    suspend fun obtenerTodosLosUsuarios(): List<Usuario>

    // UPDATE: Modificar los datos del perfil del usuario
    @Update
    suspend fun actualizarPerfil(usuario: Usuario)

    // DELETE: Borrar la cuenta de un usuario
    // Nota: Al borrar el usuario, se borrarán sus rutinas y medidas por el "ON DELETE CASCADE" que pusiste en el SQL
    @Delete
    suspend fun eliminarCuenta(usuario: Usuario)
}