package com.example.chambaya.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chambaya.MainActivity
import com.example.chambaya.R
import com.example.chambaya.data.local.ChambaYaDatabase

class WalletBalanceReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "wallet_balance_notifications"
        const val NOTIFICATION_ID = 2001

        // Umbrales de saldo (en la misma moneda que tu app)
        const val LOW_BALANCE_THRESHOLD = 10000 // Saldo bajo
        const val CRITICAL_BALANCE_THRESHOLD = 4000 // Saldo crítico

        // Keys para configuración
        const val KEY_CUSTOM_THRESHOLD = "custom_threshold"

        // Tag para el trabajo periódico
        const val WORK_TAG = "wallet_balance_reminder"
    }

    override suspend fun doWork(): Result {
        return try {
            // Crear canal de notificación
            createNotificationChannel()

            // Obtener base de datos
            val database = ChambaYaDatabase.getDatabase(context)
            val userDao = database.userDao()

            // Obtener usuario logueado
            val loggedInUser = userDao.getLoggedInUser()

            if (loggedInUser == null) {
                // No hay usuario logueado, no hacer nada
                return Result.success()
            }

            // Verificar saldo
            val balance = loggedInUser.billetera
            val customThreshold = inputData.getInt(KEY_CUSTOM_THRESHOLD, LOW_BALANCE_THRESHOLD)

            // Decidir si enviar notificación
            when {
                balance <= CRITICAL_BALANCE_THRESHOLD -> {
                    sendCriticalBalanceNotification(balance, loggedInUser.name)
                }
                balance <= customThreshold -> {
                    sendLowBalanceNotification(balance, loggedInUser.name)
                }
                else -> {
                    // Saldo suficiente, no notificar
                }
            }

            Result.success()
        } catch (e: Exception) {
            // En caso de error, reintentar
            Result.retry()
        }
    }

    /**
     * Notificación para saldo bajo (advertencia)
     */
    private fun sendLowBalanceNotification(balance: Int, userName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la app cuando se toque la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "profile") // Para navegar al perfil/billetera
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle("Saldo Bajo en Billetera")
            .setContentText("Tu saldo actual es $$balance")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hola $userName, tu billetera tiene un saldo de $$balance. " +
                        "Considera recargar para poder contratar mas servicios en ChambaYa."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_wallet,
                "Recargar",
                pendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Notificación para saldo crítico (urgente)
     */
    private fun sendCriticalBalanceNotification(balance: Int, userName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "profile")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle("Saldo Critico en Billetera")
            .setContentText("Solo te quedan $$balance")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Atencion $userName! Tu billetera tiene solo $$balance. " +
                        "No podras contratar servicios hasta que recargues tu saldo. " +
                        "Recarga ahora para no perder oportunidades!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(
                R.drawable.ic_wallet,
                "Recargar Ahora",
                pendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Billetera"
            val descriptionText = "Notificaciones sobre el saldo de tu billetera"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}


