package com.example.proyectazo.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker que se ejecuta en un hilo secundario (hilo de background).
 * WorkManager gestiona automáticamente el ciclo de vida del hilo,
 * garantizando que la tarea se ejecuta fuera del hilo principal (Main Thread).
 *
 * CoroutineWorker permite usar coroutines de Kotlin para operaciones asíncronas.
 */
class EntrenamientoWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // doWork() se ejecuta siempre en un hilo secundario (Dispatchers.Default)
    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.mostrarNotificacion(context)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}