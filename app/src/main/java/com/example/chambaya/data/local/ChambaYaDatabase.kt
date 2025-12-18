package com.example.chambaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chambaya.model.Contrato
import com.example.chambaya.model.DateConverter
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobTypeConverter
import com.example.chambaya.model.Pago
import com.example.chambaya.model.User

@Database(
    entities = [Job::class, User::class, Pago::class, Contrato::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(JobTypeConverter::class, DateConverter::class)
abstract class ChambaYaDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
    abstract fun pagoDao(): PagoDao
    abstract fun contratoDao(): ContratoDao

    companion object {
        @Volatile
        private var INSTANCE: ChambaYaDatabase? = null

        fun getDatabase(context: Context): ChambaYaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChambaYaDatabase::class.java,
                    "chambaya_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

