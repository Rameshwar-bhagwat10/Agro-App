package com.example.agrokrishiseva.admin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminManager {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val ADMIN_COLLECTION = "admins"
        
        // Predefined admin credentials - centralized in AdminCredentials
        private val ADMIN_CREDENTIALS = AdminCredentials.getCredentialsMap()
        
        private val ADMIN_EMAILS = ADMIN_CREDENTIALS.keys
    }
    
    fun getAdminCredentials(): Map<String, String> {
        return ADMIN_CREDENTIALS
    }
    
    fun isValidAdminCredentials(email: String, password: String): Boolean {
        return ADMIN_CREDENTIALS[email.lowercase()] == password
    }
    
    suspend fun isUserAdmin(email: String): Boolean {
        return try {
            // Check if email is in predefined admin list
            if (ADMIN_EMAILS.contains(email.lowercase())) {
                return true
            }
            
            // Check Firestore for dynamic admin list
            val adminDoc = firestore.collection(ADMIN_COLLECTION)
                .document(email.lowercase())
                .get()
                .await()
            
            adminDoc.exists() && adminDoc.getBoolean("isActive") == true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getCurrentAdminData(): AdminData? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null && isUserAdmin(currentUser.email ?: "")) {
                AdminData(
                    email = currentUser.email ?: "",
                    name = currentUser.displayName ?: "Admin",
                    uid = currentUser.uid
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getUserStats(): UserStats {
        return try {
            val usersSnapshot = firestore.collection("users").get().await()
            val totalUsers = usersSnapshot.size()
            
            // Count active users (logged in within last 30 days)
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            val activeUsers = usersSnapshot.documents.count { doc ->
                val joinDate = doc.getLong("joinDate") ?: 0
                joinDate > thirtyDaysAgo
            }
            
            UserStats(
                totalUsers = totalUsers,
                activeUsers = activeUsers,
                newUsersToday = 0 // You can implement this based on your needs
            )
        } catch (e: Exception) {
            UserStats(0, 0, 0)
        }
    }
}

data class AdminData(
    val email: String,
    val name: String,
    val uid: String
)

data class UserStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val newUsersToday: Int
)