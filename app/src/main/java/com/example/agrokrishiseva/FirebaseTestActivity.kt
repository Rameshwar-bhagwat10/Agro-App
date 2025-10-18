package com.example.agrokrishiseva

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseTestActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var statusText: TextView
    private lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create simple layout programmatically for testing
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        statusText = TextView(this).apply {
            text = "Firebase Status: Checking..."
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }
        
        testButton = Button(this).apply {
            text = "Test Firebase Connection"
            setOnClickListener { testFirebaseConnection() }
        }
        
        layout.addView(statusText)
        layout.addView(testButton)
        setContentView(layout)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // Test connection immediately
        testFirebaseConnection()
    }

    private fun testFirebaseConnection() {
        lifecycleScope.launch {
            try {
                statusText.text = "Testing Firebase connection..."
                
                // Test Firestore connection
                val testDoc = firestore.collection("test").document("connection")
                testDoc.set(mapOf("timestamp" to System.currentTimeMillis())).await()
                
                // Test Auth connection
                val authStatus = if (auth.currentUser != null) {
                    "User logged in: ${auth.currentUser?.email}"
                } else {
                    "No user logged in (Auth working)"
                }
                
                statusText.text = """
                    ✅ Firebase Connected Successfully!
                    
                    Project: agro-krishi-app
                    Package: com.example.agrokrishiseva
                    
                    🔥 Firestore: Working
                    🔐 Auth: Working
                    👤 $authStatus
                    
                    Ready to use in your app!
                """.trimIndent()
                
                Toast.makeText(this@FirebaseTestActivity, "Firebase connection successful!", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                statusText.text = """
                    ❌ Firebase Connection Failed
                    
                    Error: ${e.message}
                    
                    Please check:
                    1. Internet connection
                    2. Firebase project setup
                    3. google-services.json file
                """.trimIndent()
                
                Toast.makeText(this@FirebaseTestActivity, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}