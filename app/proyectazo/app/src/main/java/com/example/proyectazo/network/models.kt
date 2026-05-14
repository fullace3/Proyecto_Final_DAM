package com.example.proyectazo.network

// ── USUARIO ───────────────────────────────────
data class LoginRequest(
    val nombre: String,
    val password: String
)

data class UsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class UsuarioResponse(
    val id_usuario: Int,
    val nombre: String,
    val email: String,
    val fecha_registro: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val id_usuario: Int
)

// ── EJERCICIO ─────────────────────────────────
data class EjercicioResponse(
    val id_ejercicio: Int,
    val nombre: String,
    val grupo_muscular: String?,
    val equipamiento: String?,
    val descripcion: String?,
    val imagen: String?
)

// ── RUTINA ────────────────────────────────────
data class RutinaRequest(
    val nombre: String,
    val id_usuario: Int
)

data class RutinaResponse(
    val id_rutina: Int,
    val nombre: String,
    val id_usuario: Int
)

// ── RUTINA-EJERCICIO ──────────────────────────
data class RutinaEjercicioRequest(
    val id_rutina: Int,
    val id_ejercicio: Int,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val orden: Int
)

data class RutinaEjercicioResponse(
    val id_rutina: Int,
    val id_ejercicio: Int,
    val series: Int,
    val repeticiones: Int,
    val orden: Int
)

// ── DIETA ─────────────────────────────────────
data class DietaRequest(
    val nombre: String,
    val objetivo_calorico: Int,
    val proteinas_g: Double,
    val carbohidratos_g: Double,
    val grasas_g: Double,
    val fecha_inicio: String,       // Formato "2025-01-01T00:00:00"
    val fecha_fin: String? = null,
    val id_usuario: Int
)

data class DietaResponse(
    val id_dieta: Int,
    val nombre: String,
    val objetivo_calorico: Int,
    val proteinas_g: Double,
    val carbohidratos_g: Double,
    val grasas_g: Double,
    val fecha_inicio: String,
    val fecha_fin: String?,
    val id_usuario: Int,
    val activo: Boolean = false
)

// ── COMIDA ────────────────────────────────────
data class ComidaRequest(
    val nombre: String,
    val calorias_100g: Int,
    val proteinas_100g: Int,
    val carbohidratos_100g: Int,
    val grasas_100g: Int,
    val id_usuario: Int,
    val imagen: String? = null
)

data class ComidaResponse(
    val id_comida: Int,
    val nombre: String,
    val calorias_100g: Int,
    val proteinas_100g: Int,
    val carbohidratos_100g: Int,
    val grasas_100g: Int,
    val id_usuario: Int?,
    val imagen: String?,
    val dia: String? = null
)

// ── MEDIDAS ───────────────────────────────────
data class MedidaRequest(
    val id_usuario: Int,
    val peso_kg: Double,
    val altura_cm: Double? = null,
    val pecho_cm: Double? = null,
    val pierna_cm: Double? = null,
    val brazo_cm: Double? = null,
    val grasa_corporal_pct: Double? = null,
    val edad: Int? = null,
    val sexo: String? = null
)

data class MedidaResponse(
    val id_medida: Int,
    val id_usuario: Int,
    val fecha: String,
    val peso_kg: Double,
    val altura_cm: Double?,
    val pecho_cm: Double?,
    val pierna_cm: Double?,
    val brazo_cm: Double?,
    val grasa_corporal_pct: Double?,
    val edad: Int?,
    val sexo: String?
)

// ── PROGRESO ──────────────────────────────────
data class ProgresoResponse(
    val fecha: String,
    val volumen: Double
)

// ── Historial ──────────────────────────────────
data class HistorialRequest(
    val id_usuario: Int,
    val id_ejercicio: Int,
    val id_rutina: Int,
    val peso_kg: Double,
    val repeticiones: Int,
    val series: Int,
    val duracion_minutos: Int = 0
)

data class HistorialDetalleResponse(
    val id_registro: Int,
    val id_ejercicio: Int,
    val id_rutina: Int,
    val nombre_ejercicio: String,
    val peso_kg: Double,
    val repeticiones: Int,
    val series: Int,
    val duracion_minutos: Int,
    val fecha: String   // "2025-08-17T..."
)

// ── UI MODELS ─────────────────────────────────
data class EjercicioRutina(
    val id: Int,
    val nombre: String,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val imagenUrl: String = ""
)