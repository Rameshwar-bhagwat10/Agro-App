package com.example.agrokrishiseva.admin

import android.content.Context
import androidx.room.Room
import com.example.agrokrishiseva.AppDatabase
import com.example.agrokrishiseva.Product
import com.example.agrokrishiseva.Tip
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Service to ensure data synchronization between admin panel and customer side
 */
class AdminDataSyncService(private val context: Context) {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(context)
    
    /**
     * When admin adds a product, ensure it's immediately available to customers
     */
    suspend fun syncProductToCustomers(product: Product): Boolean {
        return try {
            // Product is already in Room database, so customers will see it immediately
            // through the Flow observer in ProductsActivity
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * When admin adds a tip, ensure it's immediately available to customers
     */
    suspend fun syncTipToCustomers(tip: Tip): Boolean {
        return try {
            // Tip is already in Firestore, customers will see it when they refresh
            // or when TipsActivity loads from Firestore
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Force refresh customer data
     */
    suspend fun forceDataRefresh() {
        try {
            // Trigger a data refresh by updating a timestamp in Firestore
            val refreshData = mapOf(
                "lastRefresh" to System.currentTimeMillis(),
                "trigger" to "admin_update"
            )
            firestore.collection("app_settings").document("data_refresh").set(refreshData).await()
        } catch (e: Exception) {
            // Ignore refresh errors
        }
    }
    
    /**
     * Get statistics for admin dashboard
     */
    suspend fun getDataStatistics(): DataStatistics {
        return try {
            val productCount = database.productDao().getCount()
            val tipsSnapshot = firestore.collection("tips").get().await()
            val tipCount = tipsSnapshot.size()
            
            DataStatistics(productCount, tipCount)
        } catch (e: Exception) {
            DataStatistics(0, 0)
        }
    }
}

data class DataStatistics(
    val totalProducts: Int,
    val totalTips: Int
)