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

    // Insertar trabajos de ejemplo (solo local) - Región de Biobío, Chile
    suspend fun insertSampleJobs() = withContext(Dispatchers.IO) {
        val count = jobDao.getJobCount()
        if (count == 0) {
            val sampleJobs = listOf(
                Job(
                    id = "1",
                    title = "Pintura y remodelación",
                    description = "Profesional con 8 años de experiencia en pintura residencial e interiores. Trabajo limpio, puntual y con garantía.",
                    price = "$300/h",
                    type = JobType.OFFER,
                    providerName = "María López",
                    latitude = -36.8270, // Concepción, Biobío
                    longitude = -73.0497,
                    distance = 0.5,
                    rating = 4.8,
                    category = "Pintura y Remodelación"
                ),
                Job(
                    id = "2",
                    title = "Pintar sala 20 m²",
                    description = "Necesito pintar sala de estar de 20 metros cuadrados. Incluye materiales.",
                    price = "$1,200 MXN",
                    type = JobType.DEMAND,
                    providerName = "Carlos M.",
                    latitude = -36.8340, // Talcahuano
                    longitude = -73.1150,
                    distance = 1.2,
                    rating = null,
                    category = "Pintura"
                ),
                Job(
                    id = "3",
                    title = "Cuidado de niños",
                    description = "Niñera con 3 años de experiencia. Puedo cuidar a niños de 2 a 10 años, apoyo con tareas, juegos y preparación de comidas y fines de semana.",
                    price = "$180/h",
                    type = JobType.OFFER,
                    providerName = "Paola Ramírez",
                    latitude = -36.8210,
                    longitude = -73.0410,
                    distance = 0.9,
                    rating = 4.8,
                    category = "Cuidado de niños"
                ),
                Job(
                    id = "4",
                    title = "Se busca plomero",
                    description = "Urgente: Fuga en baño. Necesito plomero hoy mismo.",
                    price = "Presupuesto",
                    type = JobType.DEMAND,
                    providerName = "Juan Pérez",
                    latitude = -36.8190,
                    longitude = -73.0520,
                    distance = 0.8,
                    rating = null,
                    category = "Plomería"
                ),
                Job(
                    id = "5",
                    title = "Limpieza de departamento",
                    description = "Servicio de limpieza profunda para departamentos y casas. Incluye baños, cocina y habitaciones.",
                    price = "$250/h",
                    type = JobType.OFFER,
                    providerName = "Limpieza y Más",
                    latitude = -36.8290,
                    longitude = -73.0500,
                    distance = 0.3,
                    rating = 4.5,
                    category = "Limpieza"
                ),
                Job(
                    id = "6",
                    title = "Electricista certificado",
                    description = "Instalaciones eléctricas, reparaciones y mantención. Certificado SEC.",
                    price = "$350/h",
                    type = JobType.OFFER,
                    providerName = "Electricidad Segura",
                    latitude = -36.8250,
                    longitude = -73.0450,
                    distance = 0.6,
                    rating = 4.9,
                    category = "Electricidad"
                ),
                Job(
                    id = "7",
                    title = "Se requiere jardinero",
                    description = "Necesito podar árboles y limpiar jardín grande.",
                    price = "A convenir",
                    type = JobType.DEMAND,
                    providerName = "Ana Torres",
                    latitude = -36.8360,
                    longitude = -73.0580,
                    distance = 1.5,
                    rating = null,
                    category = "Jardinería"
                ),
                Job(
                    id = "8",
                    title = "Carpintería y muebles",
                    description = "Fabricación de muebles a medida y reparaciones de carpintería en general.",
                    price = "$400/h",
                    type = JobType.OFFER,
                    providerName = "Taller Maderas del Sur",
                    latitude = -36.8300,
                    longitude = -73.0530,
                    distance = 0.7,
                    rating = 4.7,
                    category = "Carpintería"
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
