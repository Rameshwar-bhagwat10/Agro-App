package com.example.agrokrishiseva

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditProfileActivity : BaseActivity() {

    // Declare UI elements
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var saveButton: Button

    // Declare a FirestoreManager instance to interact with the database
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize the FirestoreManager
        firestoreManager = FirestoreManager()

        // Connect the declared views to the layout file
        nameEditText = findViewById(R.id.et_edit_name)
        emailEditText = findViewById(R.id.et_edit_email)
        saveButton = findViewById(R.id.btn_save_profile)

        // Fetch the user's current data as soon as the screen loads
        loadCurrentUserData()

        // Set up the click listener for the "Save Changes" button
        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()

            if (newName.isNotEmpty()) {
                // If the name is not blank, proceed to update it in the database
                updateUserDataInFirestore(newName)
            } else {
                // Show an error if the user tries to save an empty name
                nameEditText.error = "Name cannot be empty"
                Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Fetches the current user's data from Firestore and populates the EditText fields.
     */
    private fun loadCurrentUserData() {
        showLoading(true)
        lifecycleScope.launch {
            val result = firestoreManager.getUserData()
            if (result.isSuccess) {
                val userData = result.getOrNull()
                if (userData != null) {
                    // Set the fetched data to the UI elements
                    nameEditText.setText(userData.name)
                    emailEditText.setText(userData.email)
                }
            } else {
                // Handle failure to load data
                Toast.makeText(this@EditProfileActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
            showLoading(false)
        }
    }

    /**
     * Updates the user's name in the Firestore database.
     * @param newName The new name to be saved.
     */
    private fun updateUserDataInFirestore(newName: String) {
        showLoading(true)

        // A map is used to specify which fields to update in Firestore
        val updatedData = mapOf("name" to newName)

        lifecycleScope.launch {
            // --- THIS IS THE CORRECTED LINE ---
            // It now calls updateUserProfile, which exists in your FirestoreManager
            val result = firestoreManager.updateUserProfile(updatedData)

            if (result.isSuccess) {
                // On successful update, show a message and close the activity
                Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish() // This closes the EditProfileActivity and returns to the previous screen
            } else {
                // On failure, show an error message
                Toast.makeText(this@EditProfileActivity, "Failed to update profile. Please try again.", Toast.LENGTH_LONG).show()
            }
            showLoading(false)
        }
    }

    /**
     * A helper function to manage the UI's loading state.
     */
    private fun showLoading(isLoading: Boolean) {
        saveButton.isEnabled = !isLoading
        saveButton.text = if (isLoading) "Saving..." else "Save Changes"
    }
}
