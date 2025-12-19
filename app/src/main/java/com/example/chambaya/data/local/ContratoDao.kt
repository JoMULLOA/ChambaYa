package com.example.chambaya.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chambaya.model.Contrato
import com.example.chambaya.model.ContratoInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ContratoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContrato(contrato: Contrato): Long

    // Función para obtener todos los contratos de un solicitante
    @Query("SELECT * FROM contratos WHERE solicitanteId = :userId")
    fun getContratosBySolicitanteId(userId: Int): Flow<List<Contrato>>

    // Función para obtener contratos con información del Job
    @Query("""
        SELECT 
            c.id AS contrato_id,
            c.jobId AS contrato_jobId,
            c.oferenteId AS contrato_oferenteId,
            c.solicitanteId AS contrato_solicitanteId,
            c.estado AS contrato_estado,
            c.pagoId AS contrato_pagoId,
            j.id AS job_id,
            j.title AS job_title,
            j.description AS job_description,
            j.price AS job_price,
            j.type AS job_type,
            j.providerName AS job_providerName,
            j.latitude AS job_latitude,
            j.longitude AS job_longitude,
            j.distance AS job_distance,
            j.rating AS job_rating,
            j.imageUrl AS job_imageUrl,
            j.category AS job_category,
            j.userId AS job_userId
        FROM contratos AS c
        INNER JOIN jobs AS j ON c.jobId = j.id
        WHERE c.solicitanteId = :userId
    """)
    fun getContratosInfoBySolicitanteId(userId: Int): Flow<List<ContratoInfo>>
}
