package pt.ipp.estg.cmu.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserProfileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Função que permite aceder ao nosso DAO
    abstract fun userProfileDao(): UserProfileDao

    // Companion object para garantir que só existe uma única instância da base de dados (Singleton)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cmu_app_database" // Nome do ficheiro da base de dados no dispositivo
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}