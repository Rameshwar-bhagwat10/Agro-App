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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = Firebase.auth
        firestoreManager = FirestoreManager()

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
        progressBar = findViewById(R.id.progress_bar)
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

        // Show loading
        setLoading(true)

        // Firebase Authentication - Create user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration success, update user profile
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Save user data to Firestore
                                val userData = UserData(
                                    uid = user.uid,
                                    name = name,
                                    email = email,
                                    joinDate = System.currentTimeMillis()
                                )
                                
                                lifecycleScope.launch {
                                    val result = firestoreManager.saveUserData(userData)
                                    setLoading(false)
                                    
                                    if (result.isSuccess) {
                                        // Save user info to SharedPreferences
                                        sharedPreferences.edit()
                                            .putString("userName", name)
                                            .putString("userEmail", email)
                                            .putBoolean("isLoggedIn", true)
                                            .putString("currentUserEmail", email)
                                            .apply()

                                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                                        
                                        // Navigate to home
                                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this@RegisterActivity, "Failed to save user data: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                setLoading(false)
                                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // If registration fails, display a message to the user.
                    setLoading(false)
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            registerButton.isEnabled = false
            registerButton.text = "Creating account..."
        } else {
            progressBar.visibility = View.GONE
            registerButton.isEnabled = true
            registerButton.text = "Register"
        }
    }
}