package com.example.chambaya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chambaya.data.local.ChambaYaDatabase
import com.example.chambaya.data.remote.RetrofitClient
import com.example.chambaya.data.repository.JobRepository
import com.example.chambaya.data.repository.UserRepository
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobType
import kotlinx.coroutines.launch

class PublishViewModel(application: Application) : AndroidViewModel(application) {
    
    private val jobRepository: JobRepository
    private val userRepository: UserRepository
    
    private val _publishResult = MutableLiveData<PublishResult>()
    val publishResult: LiveData<PublishResult> = _publishResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        val database = ChambaYaDatabase.getDatabase(application)
        val apiService = RetrofitClient.jobApiService
        jobRepository = JobRepository(database.jobDao(), apiService)
        userRepository = UserRepository(database.userDao())
    }

    /**
     * Publica un nuevo trabajo
     */
    fun publishJob(
        title: String,
        description: String,
        price: String,
        type: JobType,
        category: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validaciones
                if (title.isBlank()) {
                    _publishResult.value = PublishResult.Error("El título es requerido")
                    _isLoading.value = false
                    return@launch
                }

                if (description.isBlank()) {
                    _publishResult.value = PublishResult.Error("La descripción es requerida")
                    _isLoading.value = false
                    return@launch
                }

                if (price.isBlank()) {
                    _publishResult.value = PublishResult.Error("El precio es requerido")
                    _isLoading.value = false
                    return@launch
                }

                // Validar formato de precio
                val priceValue = price.toDoubleOrNull()
                if (priceValue == null || priceValue < 0) {
                    _publishResult.value = PublishResult.Error("Precio inválido")
                    _isLoading.value = false
                    return@launch
                }

                if (category.isBlank()) {
                    _publishResult.value = PublishResult.Error("La categoría es requerida")
                    _isLoading.value = false
                    return@launch
                }

                // Obtener usuario logueado
                val currentUser = userRepository.getLoggedInUser()
                if (currentUser == null) {
                    _publishResult.value = PublishResult.Error("Debes iniciar sesión para publicar")
                    _isLoading.value = false
                    return@launch
                }

                // Crear trabajo
                val job = Job(
                    title = title,
                    description = description,
                    price = "S/ $price",
                    type = type,
                    providerName = currentUser.name,
                    latitude = latitude,
                    longitude = longitude,
                    category = category,
                    userId = currentUser.id
                )

                // Guardar en base de datos
                jobRepository.insertJob(job)

                _publishResult.value = PublishResult.Success
            } catch (e: Exception) {
                _publishResult.value = PublishResult.Error("Error al publicar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia los resultados
     */
    fun clearResults() {
        _publishResult.value = null
    }
}

/**
 * Sealed class para representar el resultado de publicar
 */
sealed class PublishResult {
    object Success : PublishResult()
    data class Error(val message: String) : PublishResult()
}

