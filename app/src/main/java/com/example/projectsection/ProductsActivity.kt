package com.example.projectsection

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var productAdapter: ProductAdapter
    private var allProductsFromDb: List<Product> = emptyList()
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        // Get the DAO instance
        productDao = AppDatabase.getDatabase(this).productDao()

        recyclerView = findViewById(R.id.product_recycler_view)
        categorySpinner = findViewById(R.id.category_spinner)

        setupRecyclerView()
        setupCategorySpinner()
        observeProducts()
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
}
