package com.example.agrokrishiseva.admin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvFarmLocation: TextView
    private lateinit var tvFarmSize: TextView
    private lateinit var tvCropTypes: TextView
    private lateinit var tvJoinDate: TextView
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTipsSaved: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var currentUser: UserData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        
        supportActionBar?.title = "User Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        userId = intent.getStringExtra("user_id") ?: ""
        
        initViews()
        loadUserDetails()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvPhoneNumber = findViewById(R.id.tv_phone_number)
        tvFarmLocation = findViewById(R.id.tv_farm_location)
        tvFarmSize = findViewById(R.id.tv_farm_size)
        tvCropTypes = findViewById(R.id.tv_crop_types)
        tvJoinDate = findViewById(R.id.tv_join_date)
        tvTotalOrders = findViewById(R.id.tv_total_orders)
        tvTipsSaved = findViewById(R.id.tv_tips_saved)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun loadUserDetails() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val document = firestore.collection("users").document(userId).get().await()
                currentUser = document.toObject(UserData::class.java)
                
                currentUser?.let { user ->
                    displayUserDetails(user)
                } ?: run {
                    Toast.makeText(this@UserDetailsActivity, "User not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@UserDetailsActivity, "Error loading user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserDetails(user: UserData) {
        tvUserName.text = user.name.ifEmpty { "No Name Set" }
        tvUserEmail.text = user.email
        tvPhoneNumber.text = user.phoneNumber.ifEmpty { "Not provided" }
        tvFarmLocation.text = user.farmLocation.ifEmpty { "Not provided" }
        tvFarmSize.text = user.farmSize.ifEmpty { "Not provided" }
        
        tvCropTypes.text = if (user.cropTypes.isNotEmpty()) {
            user.cropTypes.joinToString(", ")
        } else {
            "Not specified"
        }
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        tvJoinDate.text = dateFormat.format(Date(user.joinDate))
        
        tvTotalOrders.text = user.totalOrders.toString()
        tvTipsSaved.text = user.tipsSaved.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_user -> {
                showDeleteConfirmation()
                true
            }
            R.id.action_reset_password -> {
                resetUserPassword()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete this user? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteUser()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUser() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                // Delete from Firestore
                firestore.collection("users").document(userId).delete().await()
                
                progressBar.visibility = View.GONE
                Toast.makeText(this@UserDetailsActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@UserDetailsActivity, "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetUserPassword() {
        currentUser?.let { user ->
            if (user.email.isNotEmpty()) {
                auth.sendPasswordResetEmail(user.email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent to ${user.email}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}