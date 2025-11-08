package com.example.agrokrishiseva.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.Tip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TipsManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerCategory: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddTip: FloatingActionButton
    
    private lateinit var tipAdapter: AdminTipAdapter
    private lateinit var firestore: FirebaseFirestore
    private var allTips = mutableListOf<Tip>()
    private var filteredTips = mutableListOf<Tip>()
    
    private val categories = listOf("All Categories", "Irrigation", "Fertilizers", "Pest Control", "Seasonal Advice", "Crop Management", "Soil Health")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_management)
        
        supportActionBar?.title = "Tips Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        firestore = FirebaseFirestore.getInstance()
        
        initViews()
        setupRecyclerView()
        setupSearchView()
        setupCategorySpinner()
        loadTips()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_tips)
        searchView = findViewById(R.id.search_view)
        spinnerCategory = findViewById(R.id.spinner_category)
        progressBar = findViewById(R.id.progress_bar)
        fabAddTip = findViewById(R.id.fab_add_tip)
        
        fabAddTip.setOnClickListener {
            startActivity(Intent(this, AddEditTipActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        tipAdapter = AdminTipAdapter(
            tips = filteredTips,
            onEditClick = { tip ->
                val intent = Intent(this, AddEditTipActivity::class.java)
                intent.putExtra("tip", tip)
                startActivity(intent)
            },
            onDeleteClick = { tip ->
                deleteTip(tip)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tipAdapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTips(newText ?: "", spinnerCategory.selectedItem.toString())
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
                filterTips(searchView.query.toString(), categories[position])
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filterTips(query: String, category: String) {
        filteredTips.clear()
        
        var tipsToFilter = allTips
        
        // Filter by category first
        if (category != "All Categories") {
            tipsToFilter = allTips.filter { it.category == category }.toMutableList()
        }
        
        // Then filter by search query
        if (query.isEmpty()) {
            filteredTips.addAll(tipsToFilter)
        } else {
            filteredTips.addAll(tipsToFilter.filter { tip ->
                tip.title.contains(query, ignoreCase = true) ||
                tip.content.contains(query, ignoreCase = true)
            })
        }
        
        tipAdapter.notifyDataSetChanged()
    }

    private fun loadTips() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val snapshot = firestore.collection("tips").get().await()
                allTips.clear()
                
                for (document in snapshot.documents) {
                    val tip = document.toObject(Tip::class.java)
                    tip?.let { 
                        allTips.add(it) 
                    }
                }
                
                filteredTips.clear()
                filteredTips.addAll(allTips)
                tipAdapter.notifyDataSetChanged()
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@TipsManagementActivity, "Error loading tips: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTip(tip: Tip) {
        lifecycleScope.launch {
            try {
                firestore.collection("tips").document(tip.id.toString()).delete().await()
                loadTips() // Refresh the list
                Toast.makeText(this@TipsManagementActivity, "Tip deleted successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@TipsManagementActivity, "Error deleting tip: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadTips() // Refresh data when returning to this activity
    }
}