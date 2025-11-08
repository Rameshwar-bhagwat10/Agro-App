package com.example.agrokrishiseva.admin

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.agrokrishiseva.AppDatabase
import com.example.agrokrishiseva.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvActiveUsers: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalTips: TextView
    private lateinit var tvNewUsersToday: TextView
    private lateinit var tvNewUsersWeek: TextView
    private lateinit var tvNewUsersMonth: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvRecentUsers: RecyclerView
    
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: AppDatabase
    private lateinit var recentUsersAdapter: RecentUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        
        supportActionBar?.title = "Analytics & Reports"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        firestore = FirebaseFirestore.getInstance()
        database = AppDatabase.getDatabase(this)
        
        initViews()
        setupRecyclerView()
        loadAnalytics()
    }

    private fun initViews() {
        tvTotalUsers = findViewById(R.id.tv_total_users)
        tvActiveUsers = findViewById(R.id.tv_active_users)
        tvTotalProducts = findViewById(R.id.tv_total_products)
        tvTotalTips = findViewById(R.id.tv_total_tips)
        tvNewUsersToday = findViewById(R.id.tv_new_users_today)
        tvNewUsersWeek = findViewById(R.id.tv_new_users_week)
        tvNewUsersMonth = findViewById(R.id.tv_new_users_month)
        progressBar = findViewById(R.id.progress_bar)
        rvRecentUsers = findViewById(R.id.rv_recent_users)
    }

    private fun setupRecyclerView() {
        recentUsersAdapter = RecentUsersAdapter(emptyList())
        rvRecentUsers.layoutManager = LinearLayoutManager(this)
        rvRecentUsers.adapter = recentUsersAdapter
    }

    private fun loadAnalytics() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                // Load user statistics
                loadUserStatistics()
                
                // Load product statistics
                loadProductStatistics()
                
                // Load tips statistics
                loadTipsStatistics()
                
                // Load recent users
                loadRecentUsers()
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@AnalyticsActivity, "Error loading analytics: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadUserStatistics() {
        try {
            // Check authentication
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_LONG).show()
                return
            }
            
            val usersSnapshot = firestore.collection("users").get().await()
            val totalUsers = usersSnapshot.size()
            
            val now = System.currentTimeMillis()
            val oneDayAgo = now - (24 * 60 * 60 * 1000)
            val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
            val oneMonthAgo = now - (30 * 24 * 60 * 60 * 1000)
            
            var activeUsers = 0
            var newUsersToday = 0
            var newUsersWeek = 0
            var newUsersMonth = 0
            
            for (document in usersSnapshot.documents) {
                val joinDate = document.getLong("joinDate") ?: 0
                
                if (joinDate > oneMonthAgo) {
                    activeUsers++
                }
                
                if (joinDate > oneDayAgo) {
                    newUsersToday++
                }
                
                if (joinDate > oneWeekAgo) {
                    newUsersWeek++
                }
                
                if (joinDate > oneMonthAgo) {
                    newUsersMonth++
                }
            }
            
            tvTotalUsers.text = totalUsers.toString()
            tvActiveUsers.text = activeUsers.toString()
            tvNewUsersToday.text = newUsersToday.toString()
            tvNewUsersWeek.text = newUsersWeek.toString()
            tvNewUsersMonth.text = newUsersMonth.toString()
            
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    "Permission denied. Please check Firestore security rules."
                e.message?.contains("UNAUTHENTICATED") == true -> 
                    "Authentication failed. Please login again."
                else -> "Error loading user statistics: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            android.util.Log.e("Analytics", "Error loading user statistics", e)
        }
    }

    private suspend fun loadProductStatistics() {
        try {
            val products = database.productDao().getAllProductsList()
            tvTotalProducts.text = products.size.toString()
        } catch (e: Exception) {
            tvTotalProducts.text = "0"
        }
    }

    private suspend fun loadTipsStatistics() {
        try {
            val tipsSnapshot = firestore.collection("tips").get().await()
            tvTotalTips.text = tipsSnapshot.size().toString()
        } catch (e: Exception) {
            tvTotalTips.text = "0"
        }
    }

    private suspend fun loadRecentUsers() {
        try {
            val usersSnapshot = firestore.collection("users")
                .orderBy("joinDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            
            val recentUsers = mutableListOf<RecentUserData>()
            
            for (document in usersSnapshot.documents) {
                val name = document.getString("name") ?: "Unknown"
                val email = document.getString("email") ?: ""
                val joinDate = document.getLong("joinDate") ?: 0
                
                recentUsers.add(RecentUserData(name, email, joinDate))
            }
            
            recentUsersAdapter.updateUsers(recentUsers)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading recent users: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class RecentUserData(
    val name: String,
    val email: String,
    val joinDate: Long
)