package com.example.projectsection

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("AgroApp", MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
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
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Simple validation - in real app, you'd validate against server/database
        val savedEmail = sharedPreferences.getString("userEmail", "")
        val savedPassword = sharedPreferences.getString("userPassword", "")

        if (email == savedEmail && password == savedPassword) {
            // Login successful
            sharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("currentUserEmail", email)
                .apply()

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}