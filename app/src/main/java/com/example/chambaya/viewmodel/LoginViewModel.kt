package com.example.chambaya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chambaya.data.local.ChambaYaDatabase
import com.example.chambaya.data.repository.UserRepository
import com.example.chambaya.model.User
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val userDao = ChambaYaDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    /**
     * Intenta hacer login
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validaciones básicas
                if (email.isBlank() || password.isBlank()) {
                    _loginResult.value = LoginResult.Error("Por favor completa todos los campos")
                    _isLoading.value = false
                    return@launch
                }

                if (!isValidEmail(email)) {
                    _loginResult.value = LoginResult.Error("Email inválido")
                    _isLoading.value = false
                    return@launch
                }

                // Intentar login
                val user = userRepository.login(email, password)

                if (user != null) {
                    // Marcar como logueado
                    userRepository.setUserLoggedIn(user.id)
                    _loginResult.value = LoginResult.Success(user)
                } else {
                    _loginResult.value = LoginResult.Error("Email o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error("Error al iniciar sesión: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Registra un nuevo usuario
     */
    fun register(email: String, password: String, confirmPassword: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validaciones
                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                    _registerResult.value = RegisterResult.Error("Por favor completa todos los campos")
                    _isLoading.value = false
                    return@launch
                }

                if (!isValidEmail(email)) {
                    _registerResult.value = RegisterResult.Error("Email inválido")
                    _isLoading.value = false
                    return@launch
                }

                if (password.length < 6) {
                    _registerResult.value = RegisterResult.Error("La contraseña debe tener al menos 6 caracteres")
                    _isLoading.value = false
                    return@launch
                }

                if (password != confirmPassword) {
                    _registerResult.value = RegisterResult.Error("Las contraseñas no coinciden")
                    _isLoading.value = false
                    return@launch
                }

                // Intentar registrar
                val success = userRepository.register(email, password, name)

                if (success) {
                    _registerResult.value = RegisterResult.Success
                } else {
                    _registerResult.value = RegisterResult.Error("El email ya está registrado")
                }
            } catch (e: Exception) {
                _registerResult.value = RegisterResult.Error("Error al registrar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Valida formato de email
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Limpia los resultados
     */
    fun clearResults() {
        _loginResult.value = null
        _registerResult.value = null
    }
}

/**
 * Sealed class para representar el resultado del login
 */
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

/**
 * Sealed class para representar el resultado del registro
 */
sealed class RegisterResult {
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

