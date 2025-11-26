package com.example.chambaya.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chambaya.data.local.ChambaYaDatabase
import com.example.chambaya.data.remote.RetrofitClient
import com.example.chambaya.data.repository.JobRepository
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobType
import kotlinx.coroutines.launch

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JobRepository

    // LiveData from database
    val jobs: LiveData<List<Job>>
    val nearbyJobs: LiveData<List<Job>>
    val newJobs: LiveData<List<Job>>

    private val _selectedJob = MutableLiveData<Job?>()
    val selectedJob: LiveData<Job?> = _selectedJob

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        val jobDao = ChambaYaDatabase.getDatabase(application).jobDao()
        val apiService = RetrofitClient.jobApiService
        repository = JobRepository(jobDao, apiService)

        // Observar flows desde la base de datos
        jobs = repository.getAllJobsFlow().asLiveData()
        nearbyJobs = repository.getNearbyJobsFlow(10).asLiveData()
        newJobs = repository.getNewJobsFlow(2).asLiveData()

        // Cargar datos iniciales
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Insertar datos de ejemplo si la base está vacía
                repository.insertSampleJobs()

                // Intentar sincronizar con servidor
                syncJobsFromServer()
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncJobsFromServer() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.syncJobsFromServer()
            result.onFailure { exception ->
                _errorMessage.value = "Error de sincronización: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun syncNearbyJobs(latitude: Double, longitude: Double, radius: Double = 10.0) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.syncNearbyJobsFromServer(latitude, longitude, radius)
            result.onFailure { exception ->
                _errorMessage.value = "Error al obtener trabajos cercanos: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun createJob(job: Job) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createJob(job)
            result.onSuccess {
                _errorMessage.value = "Trabajo creado exitosamente"
            }.onFailure { exception ->
                _errorMessage.value = "Error al crear trabajo: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateJob(job: Job) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateJob(job)
            result.onFailure { exception ->
                _errorMessage.value = "Error al actualizar trabajo: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun deleteJob(job: Job) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteJob(job)
            result.onFailure { exception ->
                _errorMessage.value = "Error al eliminar trabajo: ${exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun selectJob(job: Job) {
        _selectedJob.value = job
    }

    fun clearSelectedJob() {
        _selectedJob.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
