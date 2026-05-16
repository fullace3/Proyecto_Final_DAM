package com.example.proyectazo.notificaciones

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_TAG = "smartfit_recordatorio"

    /**
     * Programa una notificación para 30 minutos antes de la hora de entrenamiento.
     * Si esa hora ya pasó hoy, la programa para mañana.
     * WorkManager se encarga de ejecutar el Worker en un hilo secundario.
     */
    fun programar(context: Context, horaStr: String) {
        val partes = horaStr.split(":")
        val hora = partes.getOrNull(0)?.toIntOrNull() ?: 20
        val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0

        // Calcular el momento objetivo: hora de entrenamiento - 30 minutos
        val ahora = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -30) // 30 minutos antes
        }

        // Si ya pasó hoy, programar para mañana
        if (objetivo.before(ahora)) {
            objetivo.add(Calendar.DAY_OF_YEAR, 1)
        }

        val demora = objetivo.timeInMillis - ahora.timeInMillis

        // Configurar constraints: solo necesita estar en ejecución el dispositivo
        val constraints = Constraints.Builder()
            .build()

        // Crear la petición de trabajo con el delay calculado
        val workRequest = OneTimeWorkRequestBuilder<EntrenamientoWorker>()
            .setConstraints(constraints)
            .setInitialDelay(demora, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        // Cancelar cualquier notificación anterior y programar la nueva
        WorkManager.getInstance(context).apply {
            cancelAllWorkByTag(WORK_TAG)
            enqueue(workRequest)
        }
    }

    fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}