package com.example.agrokrishiseva

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseResetHelper {
    
    private const val TAG = "DatabaseReset"
    
    /**
     * Reset the database by clearing all data and recreating with fresh schema
     * Use this if you encounter persistent Room integrity issues during development
     */
    suspend fun resetDatabase(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting database reset...")
                
                // Close existing database instance
                AppDatabase.INSTANCE?.close()
                AppDatabase.INSTANCE = null
                
                // Delete the database file
                val dbFile = context.getDatabasePath("app_database")
                if (dbFile.exists()) {
                    val deleted = dbFile.delete()
                    Log.d(TAG, "Database file deleted: $deleted")
                }
                
                // Delete WAL and SHM files if they exist
                val walFile = context.getDatabasePath("app_database-wal")
                val shmFile = context.getDatabasePath("app_database-shm")
                
                if (walFile.exists()) {
                    walFile.delete()
                    Log.d(TAG, "WAL file deleted")
                }
                
                if (shmFile.exists()) {
                    shmFile.delete()
                    Log.d(TAG, "SHM file deleted")
                }
                
                // Create new database instance
                val newDatabase = AppDatabase.getDatabase(context)
                
                // Verify the database is working
                val count = newDatabase.productDao().getCount()
                Log.d(TAG, "Database reset successful. Product count: $count")
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Database reset failed", e)
                false
            }
        }
    }
    
    /**
     * Check if database needs reset due to integrity issues
     */
    suspend fun checkDatabaseIntegrity(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(context)
                database.productDao().getCount()
                Log.d(TAG, "Database integrity check passed")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Database integrity check failed", e)
                false
            }
        }
    }
}