package com.example.appmobile.data.database

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseCleaner(private val context: Context) {
    
    suspend fun clearAllDatabases() = withContext(Dispatchers.IO) {
        try {
            // Limpiar base de datos de usuarios
            val userDatabase = UserDatabase.getDatabase(context)
            userDatabase.clearAllTables()
            Log.d("DatabaseCleaner", "✅ Base de datos de usuarios limpiada")
            
            // Limpiar base de datos de sueño
            val sleepDatabase = SleepDatabase.getDatabase(context)
            sleepDatabase.clearAllTables()
            Log.d("DatabaseCleaner", "✅ Base de datos de sueño limpiada")
            
        } catch (e: Exception) {
            Log.e("DatabaseCleaner", "❌ Error limpiando bases de datos: ${e.message}")
        }
    }
}
