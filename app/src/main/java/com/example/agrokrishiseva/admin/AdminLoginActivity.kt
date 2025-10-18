package com.example.agrokrishiseva.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.LoginActivity
import com.example.agrokrishiseva.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var quickFillButton: Button
    private lateinit var backToUserLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var adminManager: AdminManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        // Initialize Firebase Auth and AdminManager
        auth = Firebase.auth
        adminManager = AdminManager()

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.et_admin_email)
        passwordEditText = findViewById(R.id.et_admin_password)
        loginButton = findViewById(R.id.btn_admin_login)
        quickFillButton = findViewById(R.id.btn_quick_fill)
        backToUserLogin = findViewById(R.id.tv_back_to_user_login)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performAdminLogin()
        }

        quickFillButton.setOnClickListener {
            // Fill with default admin credentials
            emailEditText.setText(AdminCredentials.ADMIN_EMAIL_1)
            passwordEditText.setText(AdminCredentials.ADMIN_PASSWORD_1)
            Toast.makeText(this, "Demo credentials filled!", Toast.LENGTH_SHORT).show()
        }

        backToUserLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performAdminLogin() {
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

        // Show loading
        setLoading(true)

        // Check if credentials match predefined admin credentials
        lifecycleScope.launch {
            val isAdmin = adminManager.isUserAdmin(email)
            val isValidCredentials = adminManager.isValidAdminCredentials(email, password)
            
            if (!isAdmin) {
                setLoading(false)
                Toast.makeText(this@AdminLoginActivity, "Access denied. Not an admin account.", Toast.LENGTH_LONG).show()
                return@launch
            }

            if (!isValidCredentials) {
                setLoading(false)
                Toast.makeText(this@AdminLoginActivity, "Invalid admin credentials.", Toast.LENGTH_LONG).show()
                return@launch
            }

            // Try to sign in with Firebase, if account doesn't exist, create it
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@AdminLoginActivity) { task ->
                    if (task.isSuccessful) {
                        // Login successful
                        setLoading(false)
                        Toast.makeText(this@AdminLoginActivity, "Admin login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AdminLoginActivity, AdminDashboardActivity::class.java))
                        finish()
                    } else {
                        // Account might not exist, try to create it
                        createAdminAccount(email, password)
                    }
                }
        }
    }

    private fun createAdminAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    // Account created successfully
                    Toast.makeText(this, "Admin account created and logged in!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                } else {
                    // Account creation failed
                    val errorMessage = task.exception?.message ?: "Failed to create admin account"
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
            loginButton.text = "Login as Admin"
        }
    }
}