package com.example.agrokrishiseva

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseConnectionTest : AppCompatActivity() {
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btnTest: Button
    private lateinit var tvResult: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create simple layout programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        btnTest = Button(this).apply {
            text = "Test Firebase Connection"
            setOnClickListener { testFirebaseConnection() }
        }
        
        val btnDbTest = Button(this).apply {
            text = "Test Database Integrity"
            setOnClickListener { testDatabaseIntegrity() }
        }
        
        val btnDbReset = Button(this).apply {
            text = "Reset Database (Dev Only)"
            setOnClickListener { resetDatabase() }
        }
        
        tvResult = TextView(this).apply {
            text = "Click buttons to test Firebase connection or database integrity"
            setPadding(0, 50, 0, 0)
        }
        
        layout.addView(btnTest)
        layout.addView(btnDbTest)
        layout.addView(btnDbReset)
        layout.addView(tvResult)
        setContentView(layout)
        
        firestore = FirebaseFirestore.getInstance()
        
        supportActionBar?.title = "Firebase Connection Test"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun testFirebaseConnection() {
        lifecycleScope.launch {
            try {
                tvResult.text = "Testing Firebase connection..."
                Log.d("FirebaseTest", "Starting comprehensive Firebase test")
                
                // Test 1: Basic connection
                val connectionSuccess = FirebaseDebugHelper.testFirebaseConnection()
                if (!connectionSuccess) {
                    tvResult.text = "❌ Basic Firebase connection failed"
                    return@launch
                }
                
                tvResult.text = "✅ Basic connection successful!\n\nTesting product operations..."
                
                // Test 2: Product write test
                val productWriteSuccess = FirebaseDebugHelper.testProductWrite()
                if (!productWriteSuccess) {
                    tvResult.text = tvResult.text.toString() + "\n❌ Product write test failed"
                    return@launch
                }
                
                tvResult.text = tvResult.text.toString() + "\n✅ Product write successful!"
                
                // Test 3: List all products
                val products = FirebaseDebugHelper.listAllProducts()
                tvResult.text = tvResult.text.toString() + "\n\n📋 Products in database: ${products.size}"
                
                if (products.isNotEmpty()) {
                    tvResult.text = tvResult.text.toString() + "\n\nSample products:"
                    products.take(3).forEach { product ->
                        val name = product["name"] ?: "Unknown"
                        tvResult.text = tvResult.text.toString() + "\n• $name"
                    }
                }
                
                tvResult.text = tvResult.text.toString() + "\n\n🎉 All tests passed!"
                
            } catch (e: Exception) {
                Log.e("FirebaseTest", "Firebase test failed", e)
                tvResult.text = "❌ Firebase test failed:\n${e.message}\n\nCheck logs for details"
            }
        }
    }
    
    private fun testDatabaseIntegrity() {
        lifecycleScope.launch {
            try {
                tvResult.text = "Testing database integrity..."
                Log.d("DatabaseTest", "Starting database integrity test")
                
                val isIntegrityOk = DatabaseResetHelper.checkDatabaseIntegrity(this@FirebaseConnectionTest)
                
                if (isIntegrityOk) {
                    tvResult.text = "✅ Database integrity check passed!\n\nDatabase is working correctly."
                } else {
                    tvResult.text = "❌ Database integrity check failed!\n\nConsider resetting the database."
                }
                
            } catch (e: Exception) {
                Log.e("DatabaseTest", "Database integrity test failed", e)
                tvResult.text = "❌ Database integrity test failed:\n${e.message}"
            }
        }
    }
    
    private fun resetDatabase() {
        lifecycleScope.launch {
            try {
                tvResult.text = "Resetting database..."
                Log.d("DatabaseTest", "Starting database reset")
                
                val resetSuccess = DatabaseResetHelper.resetDatabase(this@FirebaseConnectionTest)
                
                if (resetSuccess) {
                    tvResult.text = "✅ Database reset successful!\n\nDatabase has been recreated with fresh schema."
                } else {
                    tvResult.text = "❌ Database reset failed!\n\nCheck logs for details."
                }
                
            } catch (e: Exception) {
                Log.e("DatabaseTest", "Database reset failed", e)
                tvResult.text = "❌ Database reset failed:\n${e.message}"
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}