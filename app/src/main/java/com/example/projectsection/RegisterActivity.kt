package com.example.projectsection

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferences = getSharedPreferences("AgroApp", MODE_PRIVATE)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        nameEditText = findViewById(R.id.et_name)
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_password)
        confirmPasswordEditText = findViewById(R.id.et_confirm_password)
        registerButton = findViewById(R.id.btn_register)
        loginTextView = findViewById(R.id.tv_login)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            performRegistration()
        }

        loginTextView.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun performRegistration() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Save user data
        sharedPreferences.edit()
            .putString("userName", name)
            .putString("userEmail", email)
            .putString("userPassword", password)
            .putBoolean("isLoggedIn", true)
            .putString("currentUserEmail", email)
            .apply()

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        
        // Navigate to home
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}