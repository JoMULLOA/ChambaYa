package com.example.chambaya.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chambaya.data.local.ChambaYaDatabase
import com.example.chambaya.data.repository.UserRepository
import com.example.chambaya.model.User
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _profileImageUpdated = MutableLiveData<Boolean>()
    val profileImageUpdated: LiveData<Boolean> = _profileImageUpdated

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val userDao = ChambaYaDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    /**
     * Carga el usuario actualmente logueado
     */
    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getLoggedInUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza la imagen de perfil
     */
    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = _currentUser.value ?: return@launch

                // Copiar imagen a almacenamiento interno
                val imagePath = saveImageToInternalStorage(uri)

                // Actualizar en BD
                userRepository.updateProfileImage(user.id, imagePath)

                // Recargar usuario
                loadCurrentUser()

                _profileImageUpdated.value = true
            } catch (e: Exception) {
                _profileImageUpdated.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina la imagen de perfil
     */
    fun removeProfileImage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = _currentUser.value ?: return@launch

                // Eliminar archivo si existe
                user.profileImage?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                // Actualizar BD
                userRepository.updateProfileImage(user.id, null)

                // Recargar usuario
                loadCurrentUser()

                _profileImageUpdated.value = true
            } catch (e: Exception) {
                _profileImageUpdated.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda la imagen en el almacenamiento interno
     */
    private fun saveImageToInternalStorage(uri: Uri): String {
        val context = getApplication<Application>()
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val directory = File(context.filesDir, "profile_images")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    /**
     * Cierra sesión
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}

