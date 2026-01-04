package com.example.viaterra.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EmergencyContact::class], version = 1, exportSchema = false)
abstract class EmergencyDatabase : RoomDatabase() {
    abstract fun contactDao(): EmergencyContactDao

    companion object {
        @Volatile
        private var INSTANCE: EmergencyDatabase? = null

        fun getDatabase(context: Context): EmergencyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmergencyDatabase::class.java,
                    "emergency_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
