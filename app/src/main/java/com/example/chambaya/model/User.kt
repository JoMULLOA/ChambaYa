package com.example.chambaya.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val email: String,

    val password: String, // En producción debería estar hasheada

    val name: String,

    val createdAt: Long = System.currentTimeMillis(),

    val isLoggedIn: Boolean = false,

    @ColumnInfo(name = "profile_image")
    val profileImage: String? = null,  // Ruta de la imagen de perfil

    val billetera: Int = 200000
)
