package com.example.agrokrishiseva.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.UserData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddUser: FloatingActionButton
    
    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var firestore: FirebaseFirestore
    private var allUsers = mutableListOf<UserData>()
    private var filteredUsers = mutableListOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)
        
        supportActionBar?.title = "User Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        firestore = FirebaseFirestore.getInstance()
        
        initViews()
        setupRecyclerView()
        setupSearchView()
        loadUsers()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_users)
        searchView = findViewById(R.id.search_view)
        progressBar = findViewById(R.id.progress_bar)
        fabAddUser = findViewById(R.id.fab_add_user)
        
        fabAddUser.setOnClickListener {
            startActivity(Intent(this, AddEditUserActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        userAdapter = AdminUserAdapter(filteredUsers) { user ->
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("user_id", user.uid)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText ?: "")
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        filteredUsers.clear()
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers)
        } else {
            filteredUsers.addAll(allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) ||
                user.farmLocation.contains(query, ignoreCase = true)
            })
        }
        userAdapter.notifyDataSetChanged()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                // Check if user is authenticated
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@UserManagementActivity, "User not authenticated. Please login again.", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                // Try to read from Firestore with detailed error handling
                val snapshot = firestore.collection("users").get().await()
                allUsers.clear()
                
                for (document in snapshot.documents) {
                    val user = document.toObject(UserData::class.java)
                    user?.let { allUsers.add(it) }
                }
                
                filteredUsers.clear()
                filteredUsers.addAll(allUsers)
                userAdapter.notifyDataSetChanged()
                
                progressBar.visibility = View.GONE
                
                if (allUsers.isEmpty()) {
                    Toast.makeText(this@UserManagementActivity, "No users found. This might be normal if no users have registered yet.", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                val errorMessage = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> 
                        "Permission denied. Please check Firestore security rules. Error: ${e.message}"
                    e.message?.contains("UNAUTHENTICATED") == true -> 
                        "Authentication failed. Please login again. Error: ${e.message}"
                    else -> "Error loading users: ${e.message}"
                }
                Toast.makeText(this@UserManagementActivity, errorMessage, Toast.LENGTH_LONG).show()
                android.util.Log.e("UserManagement", "Error loading users", e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadUsers() // Refresh data when returning to this activity
    }
}