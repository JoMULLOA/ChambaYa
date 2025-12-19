package com.example.chambaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chambaya.model.Contrato
import com.example.chambaya.model.DateConverter
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobTypeConverter
import com.example.chambaya.model.Pago
import com.example.chambaya.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Insertar usuarios y trabajos de prueba al crear la BD
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database.userDao())
                                    // Esperar un momento para que los usuarios se inserten
                                    kotlinx.coroutines.delay(100)
                                    // Usuario 1 tiene ID 1, crear trabajo para ese usuario
                                    populateJobs(database.jobDao(), 1)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }


        private suspend fun populateDatabase(userDao: UserDao) {
            // Usuario  1
            val user1Id = userDao.insertUser(User(
                email = "cjamett@alumnos.ubiobio.cl",
                password = "123321",
                name = "Christian Jamett",
                billetera = 300000
            ))
            
            // Usuario 2
            val user2Id = userDao.insertUser(User(
                email = "jmanriquez@alumnos.ubiobio.cl",
                password = "123321",
                name = "José Manriquez",
                billetera = 250000
            ))
        }

        private suspend fun populateJobs(jobDao: JobDao, userId: Long) {
            val testJob = Job(
                title = "Jardinería y Poda",
                description = "Servicio de jardinería profesional. Incluye poda de árboles, corte de césped y mantenimiento general de jardín.",
                price = "$15000",
                type = com.example.chambaya.model.JobType.OFFER,
                providerName = "Christian Jamett",
                latitude = -36.8201,
                longitude = -73.0444,
                distance = 0.5,
                rating = 4.8,
                category = "Jardinería",
                userId = userId.toInt()
            )
            
            jobDao.insertJob(testJob)
        }
    }
}

