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
        
        tvResult = TextView(this).apply {
            text = "Click button to test Firebase connection"
            setPadding(0, 50, 0, 0)
        }
        
        layout.addView(btnTest)
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
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}