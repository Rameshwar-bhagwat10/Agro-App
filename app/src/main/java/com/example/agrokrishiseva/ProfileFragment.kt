package com.example.agrokrishiseva

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// This class is the backend for your original fragment_profile.xml layout.
class ProfileFragment : Fragment() {

    // --- Views ---
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: Button

    // --- Firebase & System ---
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflates your original layout file
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase and other components
        auth = Firebase.auth
        firestoreManager = FirestoreManager()
        sharedPreferences = requireActivity().getSharedPreferences("AgroApp", 0)

        // Connect the views and set up listeners
        initViews(view)
        loadUserData()
        setupClickListeners()
    }

    // This function will be called every time you return to this screen,
    // ensuring the user's name is updated after editing.
    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun initViews(view: View) {
        // This connects your Kotlin code to the TextViews and Buttons in your XML
        nameTextView = view.findViewById(R.id.tv_user_name)
        emailTextView = view.findViewById(R.id.tv_user_email)
        logoutButton = view.findViewById(R.id.btn_logout)
        editProfileButton = view.findViewById(R.id.btn_edit_profile)
        // You can add more findViewById calls here for your other stats if you give them IDs
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If the user is logged in, load data from Firestore
            lifecycleScope.launch {
                val result = firestoreManager.getUserData()
                if (result.isSuccess) {
                    val userData = result.getOrNull()
                    if (userData != null) {
                        nameTextView.text = userData.name
                        emailTextView.text = userData.email
                        // Here you would update your stats TextViews
                        // For example: view.findViewById<TextView>(R.id.tv_total_orders).text = userData.totalOrders.toString()
                    } else {
                        // Fallback to Firebase Auth user info if document doesn't exist yet
                        nameTextView.text = currentUser.displayName ?: "User"
                        emailTextView.text = currentUser.email ?: "user@example.com"
                    }
                } else {
                    // Fallback if there's an error fetching data
                    nameTextView.text = currentUser.displayName ?: "User"
                    emailTextView.text = currentUser.email ?: "user@example.com"
                }
            }
        } else {
            // Fallback to SharedPreferences if no user is logged in
            val userName = sharedPreferences.getString("userName", "User")
            val userEmail = sharedPreferences.getString("userEmail", "user@example.com")
            nameTextView.text = userName
            emailTextView.text = userEmail
        }
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        editProfileButton.setOnClickListener {
            // This correctly starts your EditProfileActivity
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        auth.signOut()

        // Clear any saved login data
        sharedPreferences.edit()
            .putBoolean("isLoggedIn", false)
            .remove("currentUserEmail")
            .remove("userName")
            .remove("userEmail")
            .apply()

        // Go back to the Login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
