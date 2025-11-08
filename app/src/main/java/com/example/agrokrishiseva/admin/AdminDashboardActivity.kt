package com.example.agrokrishiseva.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.FirebaseConnectionTest
import com.example.agrokrishiseva.LoginActivity
import com.example.agrokrishiseva.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var adminNameTextView: TextView
    private lateinit var totalUsersTextView: TextView
    private lateinit var activeUsersTextView: TextView
    private lateinit var totalProductsTextView: TextView
    private lateinit var totalTipsTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var manageUsersCard: MaterialCardView
    private lateinit var viewReportsCard: MaterialCardView
    private lateinit var productManagementCard: MaterialCardView
    private lateinit var tipsManagementCard: MaterialCardView
    private lateinit var firebaseTestCard: MaterialCardView
    
    private lateinit var auth: FirebaseAuth
    private lateinit var adminManager: AdminManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initialize Firebase Auth and AdminManager
        auth = Firebase.auth
        adminManager = AdminManager()

        // Check if user is authenticated and is admin
        checkAdminAccess()

        initViews()
        setupClickListeners()
        loadDashboardData()
    }

    private fun checkAdminAccess() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not authenticated, redirect to login
            redirectToLogin()
            return
        }

        // Check if user is admin
        lifecycleScope.launch {
            val isAdmin = adminManager.isUserAdmin(currentUser.email ?: "")
            if (!isAdmin) {
                Toast.makeText(this@AdminDashboardActivity, "Access denied", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
        }
    }

    private fun initViews() {
        adminNameTextView = findViewById(R.id.tv_admin_name)
        totalUsersTextView = findViewById(R.id.tv_total_users)
        activeUsersTextView = findViewById(R.id.tv_active_users)
        totalProductsTextView = findViewById(R.id.tv_total_products)
        totalTipsTextView = findViewById(R.id.tv_total_tips)
        logoutButton = findViewById(R.id.btn_logout)
        manageUsersCard = findViewById(R.id.card_manage_users)
        viewReportsCard = findViewById(R.id.card_view_reports)
        productManagementCard = findViewById(R.id.card_product_management)
        tipsManagementCard = findViewById(R.id.card_tips_management)
        firebaseTestCard = findViewById(R.id.card_firebase_test)
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            performLogout()
        }

        manageUsersCard.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        viewReportsCard.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }

        productManagementCard.setOnClickListener {
            startActivity(Intent(this, ProductManagementActivity::class.java))
        }

        tipsManagementCard.setOnClickListener {
            startActivity(Intent(this, TipsManagementActivity::class.java))
        }

        firebaseTestCard.setOnClickListener {
            startActivity(Intent(this, FirebaseConnectionTest::class.java))
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                // Load admin data
                val adminData = adminManager.getCurrentAdminData()
                adminData?.let {
                    adminNameTextView.text = "Welcome, ${it.name}"
                }

                // Load user statistics
                val userStats = adminManager.getUserStats()
                totalUsersTextView.text = userStats.totalUsers.toString()
                activeUsersTextView.text = userStats.activeUsers.toString()
                
                // Load data sync statistics
                val syncService = AdminDataSyncService(this@AdminDashboardActivity)
                val dataStats = syncService.getDataStatistics()
                totalProductsTextView.text = dataStats.totalProducts.toString()
                totalTipsTextView.text = dataStats.totalTips.toString()

            } catch (e: Exception) {
                Toast.makeText(this@AdminDashboardActivity, "Error loading dashboard data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back to previous activity
        // Admin should logout to exit
        Toast.makeText(this, "Please use logout to exit", Toast.LENGTH_SHORT).show()
    }
}