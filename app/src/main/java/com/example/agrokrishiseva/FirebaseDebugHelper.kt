package com.example.agrokrishiseva

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseDebugHelper {
    
    private const val TAG = "FirebaseDebug"
    
    suspend fun testFirebaseConnection(): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            
            Log.d(TAG, "Testing Firebase connection...")
            
            // Test 1: Simple write operation
            val testData = mapOf(
                "test" to "connection",
                "timestamp" to System.currentTimeMillis()
            )
            
            Log.d(TAG, "Attempting to write test document...")
            firestore.collection("debug_test")
                .document("connection_test")
                .set(testData)
                .await()
            
            Log.d(TAG, "Test document written successfully")
            
            // Test 2: Read the document back
            Log.d(TAG, "Attempting to read test document...")
            val document = firestore.collection("debug_test")
                .document("connection_test")
                .get()
                .await()
            
            if (document.exists()) {
                Log.d(TAG, "Test document read successfully: ${document.data}")
                return true
            } else {
                Log.e(TAG, "Test document does not exist after write")
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Firebase connection test failed", e)
            false
        }
    }
    
    suspend fun testProductWrite(): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            
            Log.d(TAG, "Testing product write...")
            
            val testProduct = mapOf(
                "name" to "Debug Test Product",
                "description" to "This is a test product for debugging",
                "category" to "Seeds",
                "price" to 99.99,
                "imageResId" to 0,
                "firestoreId" to "debug_test_product"
            )
            
            Log.d(TAG, "Attempting to write test product...")
            firestore.collection("products")
                .document("debug_test_product")
                .set(testProduct)
                .await()
            
            Log.d(TAG, "Test product written successfully")
            
            // Verify the write
            Log.d(TAG, "Verifying test product write...")
            val document = firestore.collection("products")
                .document("debug_test_product")
                .get()
                .await()
            
            if (document.exists()) {
                Log.d(TAG, "Test product verified successfully: ${document.data}")
                return true
            } else {
                Log.e(TAG, "Test product does not exist after write")
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Product write test failed", e)
            false
        }
    }
    
    suspend fun listAllProducts(): List<Map<String, Any?>> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            
            Log.d(TAG, "Listing all products from Firestore...")
            val snapshot = firestore.collection("products").get().await()
            
            val products = mutableListOf<Map<String, Any?>>()
            for (document in snapshot.documents) {
                Log.d(TAG, "Found product: ${document.id} -> ${document.data}")
                document.data?.let { products.add(it) }
            }
            
            Log.d(TAG, "Total products found: ${products.size}")
            products
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list products", e)
            emptyList()
        }
    }
}