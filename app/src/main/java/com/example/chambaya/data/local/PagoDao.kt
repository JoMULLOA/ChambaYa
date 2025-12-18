package com.example.chambaya.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.chambaya.model.Pago

@Dao
interface PagoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPago(pago: Pago): Long
}
