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
    suspend fun getJobById(jobId: String): Job? = withContext(Dispatchers.IO) {
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

    // Crear nuevo trabajo (guardar en servidor y local)
    suspend fun createJob(job: Job): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createJob(job)
            if (response.isSuccessful && response.body() != null) {
                val createdJob = response.body()!!
                // Guardar en base de datos local
                jobDao.insertJob(createdJob)
                Result.success(createdJob)
            } else {
                Result.failure(Exception("Error al crear trabajo: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si falla el servidor, guardar solo localmente
            jobDao.insertJob(job)
            Result.failure(e)
        }
    }

    // Actualizar trabajo
    suspend fun updateJob(job: Job): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateJob(job.id, job)
            if (response.isSuccessful && response.body() != null) {
                val updatedJob = response.body()!!
                jobDao.updateJob(updatedJob)
                Result.success(updatedJob)
            } else {
                Result.failure(Exception("Error al actualizar trabajo: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si falla el servidor, actualizar solo localmente
            jobDao.updateJob(job)
            Result.failure(e)
        }
    }

    // Eliminar trabajo
    suspend fun deleteJob(job: Job): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteJob(job.id)
            if (response.isSuccessful) {
                jobDao.deleteJob(job)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar trabajo: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si falla el servidor, eliminar solo localmente
            jobDao.deleteJob(job)
            Result.failure(e)
        }
    }

    // Insertar trabajos de ejemplo (solo local)
    suspend fun insertSampleJobs() = withContext(Dispatchers.IO) {
        val count = jobDao.getJobCount()
        if (count == 0) {
            val sampleJobs = listOf(
                Job(
                    id = "1",
                    title = "Pintura y remodelación",
                    description = "Servicio de pintura profesional",
                    price = "$300/h",
                    type = JobType.OFFER,
                    providerName = "María López",
                    latitude = 19.4326,
                    longitude = -99.1332,
                    distance = 0.5,
                    rating = null,
                    category = "Construcción"
                ),
                Job(
                    id = "2",
                    title = "Se busca plomero",
                    description = "Fuga en baño",
                    price = "Presupuesto",
                    type = JobType.DEMAND,
                    providerName = "Usuario",
                    latitude = 19.4340,
                    longitude = -99.1350,
                    distance = 0.8,
                    rating = null,
                    category = "Plomería"
                ),
                Job(
                    id = "3",
                    title = "Cuidado de niños",
                    description = "Cuidadora profesional con experiencia",
                    price = "$180/h",
                    type = JobType.OFFER,
                    providerName = "Paola Ramírez",
                    latitude = 19.4310,
                    longitude = -99.1310,
                    distance = 0.9,
                    rating = 4.8,
                    category = "Cuidado"
                ),
                Job(
                    id = "4",
                    title = "Se requiere jardinería",
                    description = "Podar y limpiar jardín",
                    price = "Presupuesto",
                    type = JobType.DEMAND,
                    providerName = "Usuario",
                    latitude = 19.4360,
                    longitude = -99.1380,
                    distance = 1.3,
                    rating = null,
                    category = "Jardinería"
                ),
                Job(
                    id = "5",
                    title = "Electricista certificado",
                    description = "Instalaciones eléctricas",
                    price = "$250/h",
                    type = JobType.OFFER,
                    providerName = "Carlos Mendoza",
                    latitude = 19.4290,
                    longitude = -99.1300,
                    distance = 1.5,
                    rating = 4.9,
                    category = "Electricidad"
                )
            )
            jobDao.insertJobs(sampleJobs)
        }
    }

    // Limpiar caché local
    suspend fun clearLocalCache() = withContext(Dispatchers.IO) {
        jobDao.deleteAllJobs()
    }
}

