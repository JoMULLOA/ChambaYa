package com.example.chambaya.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chambaya.R

/**
 * Worker que envía notificaciones cuando ocurren transacciones de pago
 * Se ejecuta en segundo plano usando WorkManager
 */
class TransactionNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "transaction_notifications"
        const val NOTIFICATION_ID_DEBIT = 1001
        const val NOTIFICATION_ID_CREDIT = 1002
        
        // Keys para los datos del Worker
        const val KEY_TRANSACTION_TYPE = "transaction_type"
        const val KEY_AMOUNT = "amount"
        const val KEY_USER_NAME = "user_name"
        const val KEY_SERVICE_NAME = "service_name"
        
        // Tipos de transacción
        const val TYPE_DEBIT = "debit"  // Se descontó dinero
        const val TYPE_CREDIT = "credit" // Se agregó dinero
    }

    override suspend fun doWork(): Result {
        return try {
            // Crear canal de notificación
            createNotificationChannel()
            
            // Obtener datos de la transacción
            val transactionType = inputData.getString(KEY_TRANSACTION_TYPE) ?: return Result.failure()
            val amount = inputData.getInt(KEY_AMOUNT, 0)
            val userName = inputData.getString(KEY_USER_NAME) ?: "Usuario"
            val serviceName = inputData.getString(KEY_SERVICE_NAME) ?: "el servicio"
            
            // Enviar notificación según el tipo
            when (transactionType) {
                TYPE_DEBIT -> sendDebitNotification(amount, serviceName)
                TYPE_CREDIT -> sendCreditNotification(amount, userName, serviceName)
                else -> return Result.failure()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    /**
     * Notificación para quien paga (descuento de wallet)
     */
    private fun sendDebitNotification(amount: Int, serviceName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet) // Asegúrate de tener este icono
            .setContentTitle("💸 Pago Realizado")
            .setContentText("Se descontaron $$amount por $serviceName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Se han descontado $$amount de tu billetera por la contratación de $serviceName"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_DEBIT, notification)
    }

    /**
     * Notificación para quien recibe el pago (aumento de saldo)
     */
    private fun sendCreditNotification(amount: Int, userName: String, serviceName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet) // Asegúrate de tener este icono
            .setContentTitle("💰 Pago Recibido")
            .setContentText("Recibiste $$amount por $serviceName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("¡Felicidades! $userName te ha pagado $$amount por $serviceName. El dinero ha sido agregado a tu billetera."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_CREDIT, notification)
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Transacciones"
            val descriptionText = "Notificaciones de pagos y transacciones en ChambaYa"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
