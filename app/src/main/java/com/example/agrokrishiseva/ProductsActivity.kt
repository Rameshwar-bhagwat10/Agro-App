package com.example.agrokrishiseva

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var productAdapter: ProductAdapter
    private var allProductsFromDb: List<Product> = emptyList()
    private lateinit var productDao: ProductDao
    private lateinit var database: AppDatabase
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)



        // Get the database and DAO instance
        database = AppDatabase.getDatabase(this)
        productDao = database.productDao()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.product_recycler_view)
        categorySpinner = findViewById(R.id.category_spinner)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupRecyclerView()
        setupCategorySpinner()
        setupBottomNavigation()
        observeProducts()

        // Set products as selected in bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_products
    }



    private fun setupRecyclerView() {
        // Initialize adapter with an empty list
        productAdapter = ProductAdapter(emptyList()) { product ->
            val intent = Intent(this, ProductDetailsActivity::class.java)
            intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT, product)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            getString(R.string.all_categories),
            getString(R.string.category_seeds),
            getString(R.string.category_fertilizers),
            getString(R.string.category_pesticides),
            getString(R.string.category_tools)
        )
        val categoryKeys = listOf("All Categories", "Seeds", "Fertilizers", "Pesticides", "Tools")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categoryKeys[position]
                filterProducts(selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            // Initialize default data if needed
            val syncManager = DataSyncManager(this@ProductsActivity)
            syncManager.initializeDefaultData()
            
            // Load products from both Room and Firestore
            loadProductsFromBothSources()
            
            // Observe Room database changes
            productDao.getAllProducts().collectLatest { roomProducts ->
                // Merge with Firestore products
                mergeProductSources(roomProducts)
            }
            
            // Set up real-time listener for admin changes
            setupProductRefreshListener()
        }
    }

    private fun loadProductsFromBothSources() {
        lifecycleScope.launch {
            try {
                // Load from Firestore
                val firestoreProducts = mutableListOf<Product>()
                val snapshot = firestore.collection("products").get().await()
                
                for (document in snapshot.documents) {
                    val product: Product? = document.toObject(Product::class.java)
                    product?.let { firestoreProducts.add(it) }
                }
                
                // Sync Firestore products to Room database
                for (firestoreProduct in firestoreProducts) {
                    // Check if product exists in Room by firestoreId
                    val existingProducts = database.productDao().getAllProductsList()
                    val existingProduct = existingProducts.find { it.firestoreId == firestoreProduct.firestoreId }
                    
                    if (existingProduct == null) {
                        // Product doesn't exist in Room, add it
                        database.productDao().insertProduct(firestoreProduct)
                    } else if (existingProduct != firestoreProduct) {
                        // Product exists but is different, update it
                        val updatedProduct = firestoreProduct.copy(id = existingProduct.id)
                        database.productDao().updateProduct(updatedProduct)
                    }
                }
                
            } catch (e: Exception) {
                // If Firestore fails, just use Room database
            }
        }
    }

    private fun mergeProductSources(roomProducts: List<Product>) {
        allProductsFromDb = roomProducts
        filterProducts(categorySpinner.selectedItem.toString())
    }

    private fun filterProducts(category: String) {
        val filteredList = if (category == "All Categories") {
            allProductsFromDb
        } else {
            allProductsFromDb.filter { it.category == category }
        }
        productAdapter.updateProducts(filteredList)
    }

    private fun setupProductRefreshListener() {
        // Listen for data refresh triggers from admin panel
        firestore.collection("app_settings").document("data_refresh")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                snapshot?.let { doc ->
                    val trigger = doc.getString("trigger")
                    if (trigger == "admin_update") {
                        // Reload products from both sources when admin makes changes
                        loadProductsFromBothSources()
                        android.util.Log.d("ProductsActivity", "Admin triggered data refresh")
                    }
                }
            }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_products -> {
                    // Already on products page
                    true
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, TipsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("show_profile", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
