package com.example.projectsection

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

class ProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("AgroApp", 0)

        initViews(view)
        loadUserData()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        nameTextView = view.findViewById(R.id.tv_user_name)
        emailTextView = view.findViewById(R.id.tv_user_email)
        logoutButton = view.findViewById(R.id.btn_logout)
        editProfileButton = view.findViewById(R.id.btn_edit_profile)
    }

    private fun loadUserData() {
        val userName = sharedPreferences.getString("userName", "User")
        val userEmail = sharedPreferences.getString("userEmail", "user@example.com")

        nameTextView.text = userName
        emailTextView.text = userEmail
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        editProfileButton.setOnClickListener {
            // For now, just show a toast - you can implement edit functionality later
            android.widget.Toast.makeText(requireContext(), "Edit profile feature coming soon!", android.widget.Toast.LENGTH_SHORT).show()
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
        sharedPreferences.edit()
            .putBoolean("isLoggedIn", false)
            .remove("currentUserEmail")
            .apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}