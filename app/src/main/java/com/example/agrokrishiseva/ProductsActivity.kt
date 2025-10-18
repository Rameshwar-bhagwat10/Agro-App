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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var productAdapter: ProductAdapter
    private var allProductsFromDb: List<Product> = emptyList()
    private lateinit var productDao: ProductDao
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)



        // Get the DAO instance
        productDao = AppDatabase.getDatabase(this).productDao()

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
        val categories = listOf("All Categories", "Seeds", "Fertilizers", "Pesticides", "Tools")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                filterProducts(selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            productDao.getAllProducts().collectLatest { products ->
                allProductsFromDb = products
                // Re-apply filter whenever the data changes
                filterProducts(categorySpinner.selectedItem.toString())
            }
        }
    }

    private fun filterProducts(category: String) {
        val filteredList = if (category == "All Categories") {
            allProductsFromDb
        } else {
            allProductsFromDb.filter { it.category == category }
        }
        productAdapter.updateProducts(filteredList)
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
