package com.example.agrokrishiseva.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.Tip
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddEditTipActivity : AppCompatActivity() {

    private lateinit var etTipTitle: EditText
    private lateinit var etTipContent: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var firestore: FirebaseFirestore
    private var isEditMode = false
    private var currentTip: Tip? = null
    
    private val categories = listOf("Irrigation", "Fertilizers", "Pest Control", "Seasonal Advice", "Crop Management", "Soil Health")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_tip)
        
        firestore = FirebaseFirestore.getInstance()
        
        currentTip = intent.getParcelableExtra("tip")
        isEditMode = currentTip != null
        
        supportActionBar?.title = if (isEditMode) "Edit Tip" else "Add New Tip"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        setupCategorySpinner()
        
        if (isEditMode) {
            loadTipData()
        }
    }

    private fun initViews() {
        etTipTitle = findViewById(R.id.et_tip_title)
        etTipContent = findViewById(R.id.et_tip_content)
        spinnerCategory = findViewById(R.id.spinner_category)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)
        
        btnSave.setOnClickListener {
            if (isEditMode) {
                updateTip()
            } else {
                createTip()
            }
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun loadTipData() {
        currentTip?.let { tip ->
            etTipTitle.setText(tip.title)
            etTipContent.setText(tip.content)
            
            // Set category spinner
            val categoryPosition = categories.indexOf(tip.category)
            if (categoryPosition >= 0) {
                spinnerCategory.setSelection(categoryPosition)
            }
        }
    }

    private fun createTip() {
        if (!validateInput()) return
        
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnSave.isEnabled = false
                
                // Generate new ID
                val newId = System.currentTimeMillis().toInt()
                
                val tip = Tip(
                    id = newId,
                    title = etTipTitle.text.toString().trim(),
                    content = etTipContent.text.toString().trim(),
                    category = spinnerCategory.selectedItem.toString()
                )
                
                firestore.collection("tips").document(newId.toString()).set(tip).await()
                
                // Trigger sync to ensure customers see the new tip
                val syncService = AdminDataSyncService(this@AddEditTipActivity)
                syncService.syncTipToCustomers(tip)
                syncService.forceDataRefresh()
                
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddEditTipActivity, "Tip created successfully", Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(this@AddEditTipActivity, "Error creating tip: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateTip() {
        if (!validateInput()) return
        
        currentTip?.let { tip ->
            lifecycleScope.launch {
                try {
                    progressBar.visibility = View.VISIBLE
                    btnSave.isEnabled = false
                    
                    val updatedTip = tip.copy(
                        title = etTipTitle.text.toString().trim(),
                        content = etTipContent.text.toString().trim(),
                        category = spinnerCategory.selectedItem.toString()
                    )
                    
                    firestore.collection("tips").document(tip.id.toString()).set(updatedTip).await()
                    
                    // Trigger sync to ensure customers see the updated tip
                    val syncService = AdminDataSyncService(this@AddEditTipActivity)
                    syncService.syncTipToCustomers(updatedTip)
                    syncService.forceDataRefresh()
                    
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AddEditTipActivity, "Tip updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                    
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(this@AddEditTipActivity, "Error updating tip: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = etTipTitle.text.toString().trim()
        val content = etTipContent.text.toString().trim()
        
        if (title.isEmpty()) {
            etTipTitle.error = "Tip title is required"
            return false
        }
        
        if (content.isEmpty()) {
            etTipContent.error = "Tip content is required"
            return false
        }
        
        if (content.length < 20) {
            etTipContent.error = "Tip content should be at least 20 characters"
            return false
        }
        
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}