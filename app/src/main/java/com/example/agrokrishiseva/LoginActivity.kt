package com.example.agrokrishiseva

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var adminAccessTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        sharedPreferences = getSharedPreferences("AgroApp", MODE_PRIVATE)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToHome()
            return
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_password)
        loginButton = findViewById(R.id.btn_login)
        registerTextView = findViewById(R.id.tv_register)
        adminAccessTextView = findViewById(R.id.tv_admin_access)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        adminAccessTextView.setOnClickListener {
            startActivity(Intent(this, com.example.agrokrishiseva.admin.AdminLoginActivity::class.java))
        }
        
        // Add Firebase test button (temporary - remove after testing)
        findViewById<TextView>(R.id.tv_register).setOnLongClickListener {
            startActivity(Intent(this, FirebaseTestActivity::class.java))
            true
        }
        
        // Add admin access - triple tap on login button
        var tapCount = 0
        var lastTapTime = 0L
        loginButton.setOnLongClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime < 500) {
                tapCount++
            } else {
                tapCount = 1
            }
            lastTapTime = currentTime
            
            if (tapCount >= 3) {
                startActivity(Intent(this, com.example.agrokrishiseva.admin.AdminLoginActivity::class.java))
                tapCount = 0
            }
            true
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        setLoading(true)

        // Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    
                    // Save user info to SharedPreferences
                    sharedPreferences.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("currentUserEmail", user?.email)
                        .putString("userName", user?.displayName ?: "Farmer")
                        .apply()

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    // If sign in fails, display a message to the user.
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.text = "Signing in..."
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
            loginButton.text = "Login"
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}