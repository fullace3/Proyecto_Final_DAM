package com.example.proyectazo.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyectazo.R

object NotificationHelper {

    private const val CHANNEL_ID = "smartfit_entrenamiento"
    private const val CHANNEL_NAME = "Recordatorio de entrenamiento"
    private const val NOTIFICATION_ID = 1001

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de recordatorio de entrenamiento"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    fun mostrarNotificacion(context: Context) {
        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logosinfondo)
            .setContentTitle("¡Hora de entrenar!")
            .setContentText("Tu sesión empieza en 30 minutos. ¡Prepárate!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notificacion)
    }
}