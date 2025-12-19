package com.example.chambaya.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.chambaya.data.local.ChambaYaDatabase
import com.example.chambaya.data.local.ContratoDao
import com.example.chambaya.data.local.PagoDao
import com.example.chambaya.data.local.UserDao
import com.example.chambaya.data.remote.RetrofitClient
import com.example.chambaya.data.repository.JobRepository
import com.example.chambaya.model.Contrato
import com.example.chambaya.model.Job
import com.example.chambaya.model.Pago
import com.example.chambaya.worker.TransactionNotificationWorker
import kotlinx.coroutines.launch

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JobRepository
    private val userDao: UserDao
    private val pagoDao: PagoDao
    private val contratoDao: ContratoDao

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

    private val _hireStatus = MutableLiveData<Result<Unit>?>()
    val hireStatus: LiveData<Result<Unit>?> = _hireStatus

    init {
        val database = ChambaYaDatabase.getDatabase(application)
        val jobDao = database.jobDao()
        userDao = database.userDao()
        pagoDao = database.pagoDao()
        contratoDao = database.contratoDao()
        val apiService = RetrofitClient.jobApiService
        repository = JobRepository(jobDao, apiService)

        jobs = repository.getAllJobsFlow().asLiveData()
        nearbyJobs = repository.getNearbyJobsFlow(10).asLiveData()
        newJobs = repository.getNewJobsFlow(2).asLiveData()

        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                syncJobsFromServer()
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hireJob() {
        Log.d("JobViewModel", "hireJob() llamado")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val job = _selectedJob.value
                Log.d("JobViewModel", "Job seleccionado: ${job?.title}")
                if (job == null) {
                    Log.e("JobViewModel", "No hay job seleccionado")
                    _errorMessage.value = "No se ha seleccionado ningún trabajo."
                    _isLoading.value = false
                    return@launch
                }

                val solicitante = userDao.getLoggedInUser()
                Log.d("JobViewModel", "Usuario logueado: ${solicitante?.email}")
                if (solicitante == null) {
                    Log.e("JobViewModel", "No hay usuario logueado")
                    _errorMessage.value = "Debes iniciar sesión para contratar un trabajo."
                    _isLoading.value = false
                    return@launch
                }

                if (solicitante.id == job.userId) {
                    _errorMessage.value = "No puedes contratar tu propio trabajo."
                    _isLoading.value = false
                    return@launch
                }

                val oferente = userDao.getUserById(job.userId)
                if (oferente == null) {
                    _errorMessage.value = "No se encontró al proveedor del servicio."
                    _isLoading.value = false
                    return@launch
                }

                val priceString = job.price.filter { it.isDigit() }
                val price = priceString.toIntOrNull()
                if (price == null) {
                    _errorMessage.value = "El precio del trabajo no es válido."
                    _isLoading.value = false
                    return@launch
                }

                if (solicitante.billetera < price) {
                    _errorMessage.value = "No tienes fondos suficientes en tu billetera."
                    _isLoading.value = false
                    return@launch
                }

                val pago = Pago(
                    montoPagado = price,
                    fecha = System.currentTimeMillis(),
                    metodo = "Billetera",
                    estado = "COMPLETADO"
                )
                val pagoId = pagoDao.insertPago(pago)

                val contrato = Contrato(
                    jobId = job.id,
                    oferenteId = oferente.id,
                    solicitanteId = solicitante.id,
                    estado = "PAGADO",
                    pagoId = pagoId.toInt()
                )
                contratoDao.insertContrato(contrato)

                val updatedSolicitante = solicitante.copy(billetera = solicitante.billetera - price)
                val updatedOferente = oferente.copy(billetera = oferente.billetera + price)
                userDao.updateUser(updatedSolicitante)
                userDao.updateUser(updatedOferente)

                // Enviar notificación al solicitante (quien paga)
                scheduleTransactionNotification(
                    transactionType = TransactionNotificationWorker.TYPE_DEBIT,
                    amount = price,
                    userName = oferente.name,
                    serviceName = job.title
                )

                // Enviar notificación al oferente (quien recibe el pago)
                scheduleTransactionNotification(
                    transactionType = TransactionNotificationWorker.TYPE_CREDIT,
                    amount = price,
                    userName = solicitante.name,
                    serviceName = job.title
                )

                Log.d("JobViewModel", "Contratación exitosa")
                _hireStatus.value = Result.success(Unit)

            } catch (e: Exception) {
                Log.e("JobViewModel", "Error al contratar: ${e.message}", e)
                _errorMessage.value = "Error al procesar la contratación: ${e.message}"
                _hireStatus.value = Result.failure(e)
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
            val result = repository.insertJob(job)
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

    /**
     * Programa una notificación de transacción usando WorkManager
     */
    private fun scheduleTransactionNotification(
        transactionType: String,
        amount: Int,
        userName: String,
        serviceName: String
    ) {
        val inputData = Data.Builder()
            .putString(TransactionNotificationWorker.KEY_TRANSACTION_TYPE, transactionType)
            .putInt(TransactionNotificationWorker.KEY_AMOUNT, amount)
            .putString(TransactionNotificationWorker.KEY_USER_NAME, userName)
            .putString(TransactionNotificationWorker.KEY_SERVICE_NAME, serviceName)
            .build()

        val notificationWork = OneTimeWorkRequestBuilder<TransactionNotificationWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(notificationWork)
    }
}
