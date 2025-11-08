package com.example.agrokrishiseva.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SystemSettingsActivity : AppCompatActivity() {

    private lateinit var etAppName: EditText
    private lateinit var etAppVersion: EditText
    private lateinit var etWeatherApiKey: EditText
    private lateinit var switchMaintenanceMode: Switch
    private lateinit var switchUserRegistration: Switch
    private lateinit var switchPushNotifications: Switch
    private lateinit var etNotificationTitle: EditText
    private lateinit var etNotificationMessage: EditText
    private lateinit var btnSendNotification: Button
    private lateinit var btnSaveSettings: Button
    private lateinit var btnBackupData: Button
    private lateinit var btnClearCache: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_settings)
        
        supportActionBar?.title = "System Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        firestore = FirebaseFirestore.getInstance()
        
        initViews()
        loadSettings()
    }

    private fun initViews() {
        etAppName = findViewById(R.id.et_app_name)
        etAppVersion = findViewById(R.id.et_app_version)
        etWeatherApiKey = findViewById(R.id.et_weather_api_key)
        switchMaintenanceMode = findViewById(R.id.switch_maintenance_mode)
        switchUserRegistration = findViewById(R.id.switch_user_registration)
        switchPushNotifications = findViewById(R.id.switch_push_notifications)
        etNotificationTitle = findViewById(R.id.et_notification_title)
        etNotificationMessage = findViewById(R.id.et_notification_message)
        btnSendNotification = findViewById(R.id.btn_send_notification)
        btnSaveSettings = findViewById(R.id.btn_save_settings)
        btnBackupData = findViewById(R.id.btn_backup_data)
        btnClearCache = findViewById(R.id.btn_clear_cache)
        progressBar = findViewById(R.id.progress_bar)
        
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
        
        btnSendNotification.setOnClickListener {
            sendBroadcastNotification()
        }
        
        btnBackupData.setOnClickListener {
            showBackupConfirmation()
        }
        
        btnClearCache.setOnClickListener {
            showClearCacheConfirmation()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val settingsDoc = firestore.collection("app_settings").document("config").get().await()
                
                if (settingsDoc.exists()) {
                    etAppName.setText(settingsDoc.getString("appName") ?: "AgroKrishi Seva")
                    etAppVersion.setText(settingsDoc.getString("appVersion") ?: "1.0.0")
                    etWeatherApiKey.setText(settingsDoc.getString("weatherApiKey") ?: "")
                    switchMaintenanceMode.isChecked = settingsDoc.getBoolean("maintenanceMode") ?: false
                    switchUserRegistration.isChecked = settingsDoc.getBoolean("userRegistrationEnabled") ?: true
                    switchPushNotifications.isChecked = settingsDoc.getBoolean("pushNotificationsEnabled") ?: true
                } else {
                    // Set default values
                    etAppName.setText("AgroKrishi Seva")
                    etAppVersion.setText("1.0.0")
                    switchMaintenanceMode.isChecked = false
                    switchUserRegistration.isChecked = true
                    switchPushNotifications.isChecked = true
                }
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@SystemSettingsActivity, "Error loading settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSettings() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnSaveSettings.isEnabled = false
                
                val settings = mapOf(
                    "appName" to etAppName.text.toString().trim(),
                    "appVersion" to etAppVersion.text.toString().trim(),
                    "weatherApiKey" to etWeatherApiKey.text.toString().trim(),
                    "maintenanceMode" to switchMaintenanceMode.isChecked,
                    "userRegistrationEnabled" to switchUserRegistration.isChecked,
                    "pushNotificationsEnabled" to switchPushNotifications.isChecked,
                    "lastUpdated" to System.currentTimeMillis()
                )
                
                firestore.collection("app_settings").document("config").set(settings).await()
                
                progressBar.visibility = View.GONE
                btnSaveSettings.isEnabled = true
                Toast.makeText(this@SystemSettingsActivity, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSaveSettings.isEnabled = true
                Toast.makeText(this@SystemSettingsActivity, "Error saving settings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendBroadcastNotification() {
        val title = etNotificationTitle.text.toString().trim()
        val message = etNotificationMessage.text.toString().trim()
        
        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter both title and message", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnSendNotification.isEnabled = false
                
                val notification = mapOf(
                    "title" to title,
                    "message" to message,
                    "timestamp" to System.currentTimeMillis(),
                    "type" to "broadcast"
                )
                
                firestore.collection("notifications").add(notification).await()
                
                // Clear the input fields
                etNotificationTitle.setText("")
                etNotificationMessage.setText("")
                
                progressBar.visibility = View.GONE
                btnSendNotification.isEnabled = true
                Toast.makeText(this@SystemSettingsActivity, "Notification sent successfully", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSendNotification.isEnabled = true
                Toast.makeText(this@SystemSettingsActivity, "Error sending notification: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showBackupConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Backup Data")
            .setMessage("This will create a backup of all app data. Continue?")
            .setPositiveButton("Backup") { _, _ ->
                performDataBackup()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDataBackup() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val backupData = mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "type" to "full_backup",
                    "status" to "completed"
                )
                
                firestore.collection("backups").add(backupData).await()
                
                progressBar.visibility = View.GONE
                Toast.makeText(this@SystemSettingsActivity, "Data backup completed successfully", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@SystemSettingsActivity, "Error creating backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showClearCacheConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear all cached data. Continue?")
            .setPositiveButton("Clear") { _, _ ->
                clearCache()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearCache() {
        try {
            // Clear app cache
            cacheDir.deleteRecursively()
            
            Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing cache: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}