package com.example.agrokrishiseva

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeActivity : BaseActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase Auth
        auth = Firebase.auth

        sharedPreferences = getSharedPreferences("AgroApp", MODE_PRIVATE)

        // Check if user is logged in with Firebase
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupBottomNavigation()

        // Check if we should show profile (from other activities)
        val showProfile = intent.getBooleanExtra("show_profile", false)
        
        // Load default fragment
        if (savedInstanceState == null) {
            if (showProfile) {
                loadFragment(ProfileFragment())
                bottomNavigation.selectedItemId = R.id.nav_profile
            } else {
                loadFragment(HomeFragment())
                bottomNavigation.selectedItemId = R.id.nav_home
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_products -> {
                    startActivity(Intent(this, ProductsActivity::class.java))
                    false
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, TipsActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}