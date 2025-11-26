package com.example.chambaya.data.remote

import com.example.chambaya.model.Job
import retrofit2.Response
import retrofit2.http.*

interface JobApiService {

    @GET("jobs")
    suspend fun getAllJobs(): Response<List<Job>>

    @GET("jobs/{id}")
    suspend fun getJobById(@Path("id") jobId: String): Response<Job>

    @GET("jobs/nearby")
    suspend fun getNearbyJobs(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 10.0
    ): Response<List<Job>>

    @GET("jobs/type/{type}")
    suspend fun getJobsByType(@Path("type") type: String): Response<List<Job>>

    @POST("jobs")
    suspend fun createJob(@Body job: Job): Response<Job>

    @PUT("jobs/{id}")
    suspend fun updateJob(
        @Path("id") jobId: String,
        @Body job: Job
    ): Response<Job>

    @DELETE("jobs/{id}")
    suspend fun deleteJob(@Path("id") jobId: String): Response<Unit>

    @GET("jobs/search")
    suspend fun searchJobs(
        @Query("query") query: String,
        @Query("category") category: String? = null
    ): Response<List<Job>>
}

