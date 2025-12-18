package com.example.chambaya.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pagos")
data class Pago(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val montoPagado: Int,
    val fecha: Date,
    val metodo: String,
    val estado: String
)
