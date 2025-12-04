package com.example.chambaya.data.local

import androidx.room.*
import com.example.chambaya.model.Job
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    @Query("SELECT * FROM jobs ORDER BY distance ASC")
    fun getAllJobs(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: Int): Job?

    @Query("SELECT * FROM jobs WHERE type = :type ORDER BY distance ASC")
    fun getJobsByType(type: String): Flow<List<Job>>

    @Query("SELECT * FROM jobs ORDER BY distance ASC LIMIT :limit")
    fun getNearbyJobs(limit: Int = 10): Flow<List<Job>>

    @Query("SELECT * FROM jobs ORDER BY id DESC LIMIT :limit")
    fun getNewJobs(limit: Int = 5): Flow<List<Job>>

    @Query("""
        SELECT * FROM jobs 
        WHERE (:minLat IS NULL OR latitude >= :minLat)
        AND (:maxLat IS NULL OR latitude <= :maxLat)
        AND (:minLon IS NULL OR longitude >= :minLon)
        AND (:maxLon IS NULL OR longitude <= :maxLon)
        ORDER BY distance ASC
    """)
    fun getJobsInBounds(
        minLat: Double?,
        maxLat: Double?,
        minLon: Double?,
        maxLon: Double?
    ): Flow<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<Job>)

    @Update
    suspend fun updateJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)

    @Query("DELETE FROM jobs")
    suspend fun deleteAllJobs()

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun getJobCount(): Int
}

