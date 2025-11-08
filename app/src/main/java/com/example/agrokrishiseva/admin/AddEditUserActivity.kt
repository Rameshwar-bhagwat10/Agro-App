package com.example.agrokrishiseva.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddEditUserActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etFarmLocation: EditText
    private lateinit var etFarmSize: EditText
    private lateinit var etCropTypes: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isEditMode = false
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_user)
        
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        userId = intent.getStringExtra("user_id") ?: ""
        isEditMode = userId.isNotEmpty()
        
        supportActionBar?.title = if (isEditMode) "Edit User" else "Add New User"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        
        if (isEditMode) {
            loadUserData()
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        etFarmLocation = findViewById(R.id.et_farm_location)
        etFarmSize = findViewById(R.id.et_farm_size)
        etCropTypes = findViewById(R.id.et_crop_types)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)
        
        btnSave.setOnClickListener {
            if (isEditMode) {
                updateUser()
            } else {
                createUser()
            }
        }
        
        if (isEditMode) {
            etEmail.isEnabled = false
            etPassword.visibility = View.GONE
            findViewById<View>(R.id.password_label).visibility = View.GONE
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val document = firestore.collection("users").document(userId).get().await()
                val user = document.toObject(UserData::class.java)
                
                user?.let {
                    etName.setText(it.name)
                    etEmail.setText(it.email)
                    etPhoneNumber.setText(it.phoneNumber)
                    etFarmLocation.setText(it.farmLocation)
                    etFarmSize.setText(it.farmSize)
                    etCropTypes.setText(it.cropTypes.joinToString(", "))
                }
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddEditUserActivity, "Error loading user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val farmLocation = etFarmLocation.text.toString().trim()
        val farmSize = etFarmSize.text.toString().trim()
        val cropTypesText = etCropTypes.text.toString().trim()
        
        if (!validateInput(name, email, password)) return
        
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnSave.isEnabled = false
                
                // Create user in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val newUserId = authResult.user?.uid ?: throw Exception("Failed to create user")
                
                // Create user data
                val cropTypes = if (cropTypesText.isNotEmpty()) {
                    cropTypesText.split(",").map { it.trim() }
                } else {
                    emptyList()
                }
                
                val userData = UserData(
                    uid = newUserId,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    farmLocation = farmLocation,
                    farmSize = farmSize,
                    cropTypes = cropTypes,
                    joinDate = System.currentTimeMillis()
                )
                
                // Save to Firestore
                firestore.collection("users").document(newUserId).set(userData).await()
                
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddEditUserActivity, "User created successfully", Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(this@AddEditUserActivity, "Error creating user: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUser() {
        val name = etName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val farmLocation = etFarmLocation.text.toString().trim()
        val farmSize = etFarmSize.text.toString().trim()
        val cropTypesText = etCropTypes.text.toString().trim()
        
        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }
        
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnSave.isEnabled = false
                
                val cropTypes = if (cropTypesText.isNotEmpty()) {
                    cropTypesText.split(",").map { it.trim() }
                } else {
                    emptyList()
                }
                
                val updates = mapOf(
                    "name" to name,
                    "phoneNumber" to phoneNumber,
                    "farmLocation" to farmLocation,
                    "farmSize" to farmSize,
                    "cropTypes" to cropTypes
                )
                
                firestore.collection("users").document(userId).update(updates).await()
                
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddEditUserActivity, "User updated successfully", Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(this@AddEditUserActivity, "Error updating user: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            etName.error = "Name is required"
            return false
        }
        
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            return false
        }
        
        if (!isEditMode && password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}