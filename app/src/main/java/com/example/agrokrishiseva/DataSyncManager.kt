package com.example.agrokrishiseva

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DataSyncManager(private val context: Context) {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(context)
    
    /**
     * Sync tips from Firestore to local database if needed
     */
    suspend fun syncTipsFromFirestore(): List<Tip> {
        return try {
            val snapshot = firestore.collection("tips").get().await()
            val tips = mutableListOf<Tip>()
            
            for (document in snapshot.documents) {
                val tip = document.toObject(Tip::class.java)
                tip?.let { tips.add(it) }
            }
            
            tips
        } catch (e: Exception) {
            // Return empty list if sync fails
            emptyList()
        }
    }
    
    /**
     * Sync products from Room database (admin and customer use same source)
     */
    suspend fun getAllProducts(): List<Product> {
        return try {
            database.productDao().getAllProductsList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Initialize default data if databases are empty
     */
    suspend fun initializeDefaultData() {
        try {
            // Check if products exist
            val productCount = database.productDao().getCount()
            if (productCount == 0) {
                // Add some default products
                val defaultProducts = getDefaultProducts()
                database.productDao().insertAll(defaultProducts)
            }
            
            // Check if tips exist in Firestore
            val tipsSnapshot = firestore.collection("tips").get().await()
            if (tipsSnapshot.isEmpty) {
                // Add default tips to Firestore
                val defaultTips = getDefaultTips()
                for (tip in defaultTips) {
                    firestore.collection("tips").document(tip.id.toString()).set(tip).await()
                }
            }
            
        } catch (e: Exception) {
            // Ignore initialization errors
        }
    }
    
    private fun getDefaultProducts(): List<Product> {
        return listOf(
            Product(0, "Organic Wheat Seeds", "High-quality organic wheat seeds for better yield", "Seeds", 150.0, R.drawable.ic_launcher_foreground),
            Product(0, "NPK Fertilizer", "Balanced NPK fertilizer for all crops", "Fertilizers", 200.0, R.drawable.ic_launcher_foreground),
            Product(0, "Organic Pesticide", "Natural pesticide safe for crops", "Pesticides", 300.0, R.drawable.ic_launcher_foreground),
            Product(0, "Garden Hoe", "Durable garden hoe for farming", "Tools", 500.0, R.drawable.ic_launcher_foreground),
            Product(0, "Rice Seeds", "Premium quality rice seeds", "Seeds", 120.0, R.drawable.ic_launcher_foreground),
            Product(0, "Compost Fertilizer", "Organic compost fertilizer", "Fertilizers", 180.0, R.drawable.ic_launcher_foreground)
        )
    }
    
    private fun getDefaultTips(): List<Tip> {
        return listOf(
            Tip(1, "Proper Irrigation Techniques", "Proper irrigation is crucial for crop health. Avoid over-watering, which can lead to root rot. Water early in the morning to minimize evaporation and allow leaves to dry, which helps prevent fungal diseases.", "Irrigation"),
            Tip(2, "Choosing the Right Fertilizer", "Different crops have different nutrient requirements. A soil test can help you choose a fertilizer with the right NPK (Nitrogen, Phosphorus, Potassium) ratio for your specific soil and crop type.", "Fertilizers"),
            Tip(3, "Seasonal Crop Rotation", "Rotating crops each season helps to prevent soil depletion and reduces the buildup of pests and diseases specific to one type of crop. Never plant the same crop in the same spot for two consecutive years.", "Seasonal Advice"),
            Tip(4, "Organic Pest Control", "Use natural predators like ladybugs or lacewings, or apply neem oil sprays to manage pests without resorting to chemical pesticides. This protects your crops, beneficial insects, and the environment.", "Pest Control"),
            Tip(5, "Soil Health Management", "Incorporate compost and cover crops like clover or vetch to improve soil structure, water retention, and nutrient content over time. Healthy soil is the foundation of a healthy farm.", "Soil Health")
        )
    }
}