package com.example.agrokrishiseva.admin

/**
 * Admin Credentials Reference
 * 
 * This file contains the predefined admin credentials for the AgroKrishi Seva app.
 * These accounts will be automatically created in Firebase Authentication when first used.
 */
object AdminCredentials {
    
    const val ADMIN_EMAIL_1 = "admin@agrokrishiseva.com"
    const val ADMIN_PASSWORD_1 = "admin123"
    
    const val ADMIN_EMAIL_2 = "superadmin@agrokrishiseva.com"
    const val ADMIN_PASSWORD_2 = "super123"
    
    /**
     * Get all admin credentials as a formatted string for display
     */
    fun getCredentialsInfo(): String {
        return """
            Admin Credentials:
            
            1. Regular Admin:
               Email: $ADMIN_EMAIL_1
               Password: $ADMIN_PASSWORD_1
            
            2. Super Admin:
               Email: $ADMIN_EMAIL_2
               Password: $ADMIN_PASSWORD_2
            
            Note: These accounts are automatically created in Firebase when first used.
        """.trimIndent()
    }
    
    /**
     * Get credentials as a map for programmatic access
     */
    fun getCredentialsMap(): Map<String, String> {
        return mapOf(
            ADMIN_EMAIL_1 to ADMIN_PASSWORD_1,
            ADMIN_EMAIL_2 to ADMIN_PASSWORD_2
        )
    }
}