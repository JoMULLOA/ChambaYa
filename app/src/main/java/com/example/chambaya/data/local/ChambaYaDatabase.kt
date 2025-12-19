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
                            // Insertar datos iniciales al crear la BD
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = INSTANCE
                                if (database != null) {
                                    populateInitialData(database)
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Verificar y crear usuarios iniciales si no existen
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = INSTANCE
                                if (database != null) {
                                    val userCount = database.userDao().getUserCount()
                                    if (userCount == 0) {
                                        populateInitialData(database)
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: ChambaYaDatabase) {
            val userDao = database.userDao()
            val jobDao = database.jobDao()

            // Usuario 1
            userDao.insertUser(User(
                id = 1,
                email = "cjamett@alumnos.ubiobio.cl",
                password = "123321",
                name = "Christian Jamett",
                billetera = 300000
            ))

            // Usuario 2
            userDao.insertUser(User(
                id = 2,
                email = "jmanriquez@alumnos.ubiobio.cl",
                password = "123321",
                name = "José Manriquez",
                billetera = 250000
            ))

            // Usuario 3
            userDao.insertUser(User(
                id = 3,
                email = "brodriguez@alumnos.ubiobio.cl",
                password = "123321",
                name = "Bastian Rodriguez",
                billetera = 300000
            ))

            // Esperar un momento para que los usuarios se inserten
            kotlinx.coroutines.delay(100)

            // Crear trabajo de prueba para el usuario 1
            val testJob = Job(
                id = 0,
                title = "Jardinería y Poda",
                description = "Servicio de jardinería profesional. Incluye poda de árboles, corte de césped y mantenimiento general de jardín.",
                price = "S/ 15000",
                type = com.example.chambaya.model.JobType.OFFER,
                providerName = "Christian Jamett",
                latitude = -36.8201,
                longitude = -73.0444,
                distance = 0.5,
                rating = 4.8,
                category = "Jardinería",
                userId = 1
            )

            jobDao.insertJob(testJob)
        }
    }
}
