package com.example.agrokrishiseva.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agrokrishiseva.AppDatabase
import com.example.agrokrishiseva.FirebaseDebugHelper
import com.example.agrokrishiseva.Product
import com.example.agrokrishiseva.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerImage: Spinner
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var database: AppDatabase
    private lateinit var firestore: FirebaseFirestore
    private var isEditMode = false
    private var currentProduct: Product? = null
    
    private val categories = listOf("Seeds", "Fertilizers", "Pesticides", "Tools")
    private val imageOptions = mapOf(
        "Default Seed" to R.drawable.ic_launcher_foreground,
        "Default Fertilizer" to R.drawable.ic_launcher_foreground,
        "Default Pesticide" to R.drawable.ic_launcher_foreground,
        "Default Tool" to R.drawable.ic_launcher_foreground
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)
        
        database = AppDatabase.getDatabase(this)
        firestore = FirebaseFirestore.getInstance()
        
        currentProduct = intent.getParcelableExtra("product")
        isEditMode = currentProduct != null
        
        supportActionBar?.title = if (isEditMode) "Edit Product" else "Add New Product"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        setupSpinners()
        
        if (isEditMode) {
            loadProductData()
        }
    }

    private fun initViews() {
        etProductName = findViewById(R.id.et_product_name)
        etProductDescription = findViewById(R.id.et_product_description)
        etProductPrice = findViewById(R.id.et_product_price)
        spinnerCategory = findViewById(R.id.spinner_category)
        spinnerImage = findViewById(R.id.spinner_image)
        btnSave = findViewById(R.id.btn_save)
        progressBar = findViewById(R.id.progress_bar)
        
        btnSave.setOnClickListener {
            if (isEditMode) {
                updateProduct()
            } else {
                createProduct()
            }
        }
    }

    private fun setupSpinners() {
        // Category spinner
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        
        // Image spinner
        val imageNames = imageOptions.keys.toList()
        val imageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, imageNames)
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerImage.adapter = imageAdapter
    }

    private fun loadProductData() {
        currentProduct?.let { product ->
            etProductName.setText(product.name)
            etProductDescription.setText(product.description)
            etProductPrice.setText(product.price.toString())
            
            // Set category spinner
            val categoryPosition = categories.indexOf(product.category)
            if (categoryPosition >= 0) {
                spinnerCategory.setSelection(categoryPosition)
            }
            
            // Set image spinner (find matching image or use first)
            val imagePosition = imageOptions.values.indexOf(product.imageResId)
            if (imagePosition >= 0) {
                spinnerImage.setSelection(imagePosition)
            }
        }
    }

    private fun createProduct() {
        if (!validateInput()) return
        
        lifecycleScope.launch {
            try {
                Log.d("AddEditProduct", "Starting product creation...")
                progressBar.visibility = View.VISIBLE
                btnSave.isEnabled = false
                
                val selectedImageName = spinnerImage.selectedItem.toString()
                val imageResId = imageOptions[selectedImageName] ?: R.drawable.ic_launcher_foreground
                
                // Generate Firestore document ID
                val firestoreId = firestore.collection("products").document().id
                Log.d("AddEditProduct", "Generated Firestore ID: $firestoreId")
                
                val product = Product(
                    name = etProductName.text.toString().trim(),
                    description = etProductDescription.text.toString().trim(),
                    category = spinnerCategory.selectedItem.toString(),
                    price = etProductPrice.text.toString().toDouble(),
                    imageResId = imageResId,
                    firestoreId = firestoreId
                )
                
                Log.d("AddEditProduct", "Product object created: $product")
                
                // Save to Room Database first
                Log.d("AddEditProduct", "Saving to Room Database...")
                database.productDao().insertProduct(product)
                Log.d("AddEditProduct", "Saved to Room Database successfully")
                
                // Save to Firestore
                Log.d("AddEditProduct", "Saving to Firestore...")
                
                // First test if Firebase is working
                val connectionTest = FirebaseDebugHelper.testFirebaseConnection()
                if (!connectionTest) {
                    throw Exception("Firebase connection test failed")
                }
                
                firestore.collection("products").document(firestoreId).set(product).await()
                Log.d("AddEditProduct", "Saved to Firestore successfully")
                
                // Verify the save
                val verifyDoc = firestore.collection("products").document(firestoreId).get().await()
                if (verifyDoc.exists()) {
                    Log.d("AddEditProduct", "Firestore save verified: ${verifyDoc.data}")
                } else {
                    Log.w("AddEditProduct", "Firestore save verification failed - document not found")
                }
                
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddEditProductActivity, "Product created successfully", Toast.LENGTH_SHORT).show()
                finish()
                
            } catch (e: Exception) {
                Log.e("AddEditProduct", "Error creating product", e)
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(this@AddEditProductActivity, "Error creating product: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateProduct() {
        if (!validateInput()) return
        
        currentProduct?.let { product ->
            lifecycleScope.launch {
                try {
                    progressBar.visibility = View.VISIBLE
                    btnSave.isEnabled = false
                    
                    val selectedImageName = spinnerImage.selectedItem.toString()
                    val imageResId = imageOptions[selectedImageName] ?: product.imageResId
                    
                    val updatedProduct = product.copy(
                        name = etProductName.text.toString().trim(),
                        description = etProductDescription.text.toString().trim(),
                        category = spinnerCategory.selectedItem.toString(),
                        price = etProductPrice.text.toString().toDouble(),
                        imageResId = imageResId
                    )
                    
                    // Update both Room Database and Firestore
                    database.productDao().updateProduct(updatedProduct)
                    
                    // Update in Firestore using existing firestoreId or create new one
                    val firestoreDocId = if (product.firestoreId.isNotEmpty()) {
                        product.firestoreId
                    } else {
                        firestore.collection("products").document().id
                    }
                    
                    val productWithFirestoreId = updatedProduct.copy(firestoreId = firestoreDocId)
                    firestore.collection("products").document(firestoreDocId).set(productWithFirestoreId).await()
                    
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AddEditProductActivity, "Product updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                    
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(this@AddEditProductActivity, "Error updating product: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = etProductName.text.toString().trim()
        val description = etProductDescription.text.toString().trim()
        val priceText = etProductPrice.text.toString().trim()
        
        if (name.isEmpty()) {
            etProductName.error = "Product name is required"
            return false
        }
        
        if (description.isEmpty()) {
            etProductDescription.error = "Product description is required"
            return false
        }
        
        if (priceText.isEmpty()) {
            etProductPrice.error = "Price is required"
            return false
        }
        
        try {
            val price = priceText.toDouble()
            if (price <= 0) {
                etProductPrice.error = "Price must be greater than 0"
                return false
            }
        } catch (e: NumberFormatException) {
            etProductPrice.error = "Invalid price format"
            return false
        }
        
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}