package com.example.chambaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobTypeConverter

@Database(
    entities = [Job::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(JobTypeConverter::class)
abstract class ChambaYaDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao

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

