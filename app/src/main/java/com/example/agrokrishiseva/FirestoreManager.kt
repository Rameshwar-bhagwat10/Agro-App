package com.example.agrokrishiseva

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val PRODUCTS_COLLECTION = "products"
        private const val TIPS_COLLECTION = "tips"
    }
    
    // User Data Operations
    suspend fun saveUserData(userData: UserData): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                db.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .set(userData)
                    .await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserData(): Result<UserData?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val document = db.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val userData = document.toObject(UserData::class.java)
                    Result.success(userData)
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                db.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update(updates)
                    .await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Analytics Operations
    suspend fun incrementOrderCount(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userRef = db.collection(USERS_COLLECTION).document(currentUser.uid)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentCount = snapshot.getLong("totalOrders") ?: 0
                    transaction.update(userRef, "totalOrders", currentCount + 1)
                }.await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun incrementTipsSaved(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userRef = db.collection(USERS_COLLECTION).document(currentUser.uid)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentCount = snapshot.getLong("tipsSaved") ?: 0
                    transaction.update(userRef, "tipsSaved", currentCount + 1)
                }.await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}