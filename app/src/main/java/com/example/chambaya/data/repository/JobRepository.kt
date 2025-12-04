package com.example.chambaya.data.repository

import com.example.chambaya.data.local.JobDao
import com.example.chambaya.data.remote.JobApiService
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JobRepository(
    private val jobDao: JobDao,
    private val apiService: JobApiService
) {

    // Obtener trabajos locales como Flow
    fun getAllJobsFlow(): Flow<List<Job>> = jobDao.getAllJobs()

    fun getNearbyJobsFlow(limit: Int = 10): Flow<List<Job>> = jobDao.getNearbyJobs(limit)

    fun getNewJobsFlow(limit: Int = 5): Flow<List<Job>> = jobDao.getNewJobs(limit)

    fun getJobsByTypeFlow(type: JobType): Flow<List<Job>> = jobDao.getJobsByType(type.name)

    // Obtener trabajo por ID
    suspend fun getJobById(jobId: Int): Job? = withContext(Dispatchers.IO) {
        jobDao.getJobById(jobId)
    }

    // Sincronizar con servidor PostgreSQL
    suspend fun syncJobsFromServer(): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllJobs()
            if (response.isSuccessful && response.body() != null) {
                val jobs = response.body()!!
                // Guardar en base de datos local
                jobDao.insertJobs(jobs)
                Result.success(jobs)
            } else {
                Result.failure(Exception("Error al obtener trabajos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sincronizar trabajos cercanos desde servidor
    suspend fun syncNearbyJobsFromServer(
        latitude: Double,
        longitude: Double,
        radius: Double = 10.0
    ): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNearbyJobs(latitude, longitude, radius)
            if (response.isSuccessful && response.body() != null) {
                val jobs = response.body()!!
                // Guardar en base de datos local
                jobDao.insertJobs(jobs)
                Result.success(jobs)
            } else {
                Result.failure(Exception("Error al obtener trabajos cercanos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear nuevo trabajo (guardar localmente)
    suspend fun insertJob(job: Job): Result<Job> = withContext(Dispatchers.IO) {
        try {
            jobDao.insertJob(job)
            Result.success(job)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar trabajo (solo local por ahora)
    suspend fun updateJob(job: Job): Result<Job> = withContext(Dispatchers.IO) {
        try {
            jobDao.updateJob(job)
            Result.success(job)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar trabajo (solo local por ahora)
    suspend fun deleteJob(job: Job): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            jobDao.deleteJob(job)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // COMENTADO: Ya no se usan trabajos de ejemplo
    // Los trabajos ahora solo se crean desde PublishFragment por usuarios
    /*
    suspend fun insertSampleJobs() = withContext(Dispatchers.IO) {
        // Código comentado
    }
    */

    // Limpiar caché local
    suspend fun clearLocalCache() = withContext(Dispatchers.IO) {
        jobDao.deleteAllJobs()
    }
}
