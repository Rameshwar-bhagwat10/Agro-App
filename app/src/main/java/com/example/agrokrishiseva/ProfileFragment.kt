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
    private lateinit var changeLanguageButton: Button

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
        changeLanguageButton = view.findViewById(R.id.btn_change_language)
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

        changeLanguageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_hindi),
            getString(R.string.language_marathi)
        )
        val languageCodes = arrayOf("en", "hi", "mr")
        
        val currentLanguage = LocaleHelper.getLanguage(requireContext())
        val selectedIndex = languageCodes.indexOf(currentLanguage)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val selectedLanguageCode = languageCodes[which]
                if (selectedLanguageCode != currentLanguage) {
                    LocaleHelper.setLocale(requireContext(), selectedLanguageCode)
                    // Restart the activity to apply language change
                    requireActivity().recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no, null)
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
