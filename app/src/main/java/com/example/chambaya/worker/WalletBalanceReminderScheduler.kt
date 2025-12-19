package com.example.chambaya.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Clase helper para programar y gestionar el WalletBalanceReminderWorker
 */
object WalletBalanceReminderScheduler {

    /**
     * Programa el worker para que se ejecute periódicamente
     *
     * @param context Contexto de la aplicación
     * @param intervalHours Intervalo en horas (mínimo 15 minutos para testing, 1 hora en producción)
     * @param customThreshold Umbral personalizado de saldo bajo (opcional)
     */
    fun scheduleWalletBalanceCheck(
        context: Context,
        intervalHours: Long = 24, // Por defecto revisa cada 24 horas
        customThreshold: Int? = null
    ) {
        // Constraints: Solo ejecutar si hay batería suficiente (opcional)
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // No ejecutar si batería está baja
            .build()

        // Preparar datos de entrada (si hay umbral personalizado)
        val inputData = if (customThreshold != null) {
            Data.Builder()
                .putInt(WalletBalanceReminderWorker.KEY_CUSTOM_THRESHOLD, customThreshold)
                .build()
        } else {
            Data.EMPTY
        }

        // Crear el trabajo periódico
        val workRequest = PeriodicWorkRequestBuilder<WalletBalanceReminderWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(WalletBalanceReminderWorker.WORK_TAG)
            .build()

        // Programar el trabajo (actualiza el anterior si existe)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WalletBalanceReminderWorker.WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Programa el worker con intervalo mínimo (para testing: 15 minutos)
     * Útil durante desarrollo para probar rápidamente
     */
    fun scheduleWalletBalanceCheckForTesting(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false) // Permitir en testing
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WalletBalanceReminderWorker>(
            15, TimeUnit.MINUTES // Intervalo mínimo permitido
        )
            .setConstraints(constraints)
            .addTag(WalletBalanceReminderWorker.WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WalletBalanceReminderWorker.WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancela el worker periódico
     */
    fun cancelWalletBalanceCheck(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WalletBalanceReminderWorker.WORK_TAG)
    }

    /**
     * Verifica si el worker está programado
     */
    fun isWalletBalanceCheckScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosByTag(WalletBalanceReminderWorker.WORK_TAG)
            .get()

        return workInfos.any { !it.state.isFinished }
    }
}

