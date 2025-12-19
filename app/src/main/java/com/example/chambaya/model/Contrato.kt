package com.example.chambaya.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contratos",
    foreignKeys = [
        ForeignKey(entity = Job::class, parentColumns = ["id"], childColumns = ["jobId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["oferenteId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["solicitanteId"]),
        ForeignKey(entity = Pago::class, parentColumns = ["id"], childColumns = ["pagoId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index(value = ["jobId"]), Index(value = ["oferenteId"]), Index(value = ["solicitanteId"]), Index(value = ["pagoId"])]
)
data class Contrato(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val jobId: Int,
    val oferenteId: Int,
    val solicitanteId: Int,
    var estado: String,
    var pagoId: Int? = null
)
