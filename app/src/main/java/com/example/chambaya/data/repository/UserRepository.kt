package com.example.chambaya.data.repository

import com.example.chambaya.data.local.UserDao
import com.example.chambaya.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    /**
     * Intenta hacer login con email y password
     * @return User si las credenciales son correctas, null si no
     */
    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    /**
     * Registra un nuevo usuario
     * @return true si el registro fue exitoso, false si el email ya existe
     */
    suspend fun register(email: String, password: String, name: String): Boolean {
        return try {
            // Verificar si el email ya está registrado
            if (userDao.isEmailRegistered(email) > 0) {
                return false
            }

            val user = User(
                email = email,
                password = password, // En producción, hashear la contraseña
                name = name
            )
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Marca un usuario como logueado
     */
    suspend fun setUserLoggedIn(userId: Int) {
        userDao.logoutAllUsers() // Primero desloguear a todos
        userDao.setUserLoggedIn(userId)
    }

    /**
     * Desloguea a todos los usuarios
     */
    suspend fun logout() {
        userDao.logoutAllUsers()
    }

    /**
     * Obtiene el usuario actualmente logueado
     */
    suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()
    }

    /**
     * Flow del usuario logueado para observar cambios
     */
    fun getLoggedInUserFlow(): Flow<User?> {
        return userDao.getLoggedInUserFlow()
    }

    /**
     * Verifica si un email ya está registrado
     */
    suspend fun isEmailRegistered(email: String): Boolean {
        return userDao.isEmailRegistered(email) > 0
    }

    /**
     * Obtiene un usuario por email
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    /**
     * Actualiza la imagen de perfil del usuario
     */
    suspend fun updateProfileImage(userId: Int, imagePath: String?) {
        userDao.updateProfileImage(userId, imagePath)
    }
}

