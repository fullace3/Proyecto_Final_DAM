package com.example.proyectazo.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── USUARIOS ──────────────────────────────
    @POST("usuarios/registro")
    suspend fun registro(@Body datos: UsuarioRequest): Response<UsuarioResponse>

    @POST("usuarios/login")
    suspend fun login(@Body datos: LoginRequest): Response<TokenResponse>

    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): Response<UsuarioResponse>

    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Int,
        @Body datos: UsuarioRequest
    ): Response<UsuarioResponse>

    // ── EJERCICIOS ────────────────────────────
    @GET("ejercicios")
    suspend fun getEjercicios(): Response<List<EjercicioResponse>>

    @GET("ejercicios/grupo/{grupo}")
    suspend fun getEjerciciosPorGrupo(
        @Path("grupo") grupo: String
    ): Response<List<EjercicioResponse>>

    // ── RUTINAS ───────────────────────────────
    @GET("rutinas/usuario/{id}")
    suspend fun getRutinas(@Path("id") userId: Int): Response<List<RutinaResponse>>

    @POST("rutinas")
    suspend fun crearRutina(@Body datos: RutinaRequest): Response<RutinaResponse>

    @PUT("rutinas/{id}")
    suspend fun editarRutina(
        @Path("id") id: Int,
        @Body datos: RutinaRequest
    ): Response<RutinaResponse>

    @DELETE("rutinas/{id}")
    suspend fun borrarRutina(@Path("id") id: Int): Response<Unit>

    // ── RUTINA-EJERCICIO ──────────────────────
    @GET("rutinas/{id}/ejercicios")
    suspend fun getEjerciciosDeRutina(
        @Path("id") rutinaId: Int
    ): Response<List<RutinaEjercicioResponse>>

    @POST("rutinas/ejercicios")
    suspend fun añadirEjercicioARutina(
        @Body datos: RutinaEjercicioRequest
    ): Response<RutinaEjercicioResponse>

    @POST("historial")
    suspend fun registrarHistorial(@Body datos: HistorialRequest): Response<HistorialDetalleResponse>

    @GET("historial/usuario/{id}")
    suspend fun getHistorial(@Path("id") userId: Int): Response<List<HistorialDetalleResponse>>

    @GET("dietas/usuario/{id}/actual")
    suspend fun getDietaActual(@Path("id") userId: Int): Response<DietaResponse>

}