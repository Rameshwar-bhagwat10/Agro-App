package com.example.agrokrishiseva.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.agrokrishiseva.AppDatabase
import com.example.agrokrishiseva.Product
import com.example.agrokrishiseva.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerCategory: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddProduct: FloatingActionButton
    
    private lateinit var productAdapter: AdminProductAdapter
    private lateinit var database: AppDatabase
    private var allProducts = mutableListOf<Product>()
    private var filteredProducts = mutableListOf<Product>()
    
    private val categories = listOf("All Categories", "Seeds", "Fertilizers", "Pesticides", "Tools")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_management)
        
        supportActionBar?.title = "Product Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        database = AppDatabase.getDatabase(this)
        
        initViews()
        setupRecyclerView()
        setupSearchView()
        setupCategorySpinner()
        loadProducts()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_products)
        searchView = findViewById(R.id.search_view)
        spinnerCategory = findViewById(R.id.spinner_category)
        progressBar = findViewById(R.id.progress_bar)
        fabAddProduct = findViewById(R.id.fab_add_product)
        
        fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        productAdapter = AdminProductAdapter(
            products = filteredProducts,
            onEditClick = { product ->
                val intent = Intent(this, AddEditProductActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            },
            onDeleteClick = { product ->
                deleteProduct(product)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText ?: "", spinnerCategory.selectedItem.toString())
                return true
            }
        })
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterProducts(searchView.query.toString(), categories[position])
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filterProducts(query: String, category: String) {
        filteredProducts.clear()
        
        var productsToFilter = allProducts
        
        // Filter by category first
        if (category != "All Categories") {
            productsToFilter = allProducts.filter { it.category == category }.toMutableList()
        }
        
        // Then filter by search query
        if (query.isEmpty()) {
            filteredProducts.addAll(productsToFilter)
        } else {
            filteredProducts.addAll(productsToFilter.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            })
        }
        
        productAdapter.notifyDataSetChanged()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val products = database.productDao().getAllProductsList()
                allProducts.clear()
                allProducts.addAll(products)
                
                filteredProducts.clear()
                filteredProducts.addAll(allProducts)
                productAdapter.notifyDataSetChanged()
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ProductManagementActivity, "Error loading products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            try {
                // Delete from Room Database
                database.productDao().deleteProduct(product)
                
                // Delete from Firestore if it has a firestoreId
                if (product.firestoreId.isNotEmpty()) {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("products").document(product.firestoreId).delete().await()
                }
                
                loadProducts() // Refresh the list
                Toast.makeText(this@ProductManagementActivity, "Product deleted successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ProductManagementActivity, "Error deleting product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadProducts() // Refresh data when returning to this activity
    }
}