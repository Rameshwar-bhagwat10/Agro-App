package com.example.projectsection

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class TipsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tipAdapter: TipAdapter
    private val allTips = getMockTips() // Mock data source
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)



        recyclerView = findViewById(R.id.tips_recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupRecyclerView()
        setupBottomNavigation()

        // Set tips as selected in bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_tips
    }



    private fun setupRecyclerView() {
        tipAdapter = TipAdapter(allTips,
            onBookmarkClick = { tip ->
                // Toggle the bookmark status
                tip.isBookmarked = !tip.isBookmarked
                val message = if (tip.isBookmarked) "Bookmarked '${tip.title}'" else "Removed bookmark for '${tip.title}'"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                tipAdapter.notifyDataSetChanged()
            },
            onItemClick = { tip ->
                // Launch the TipDetailsActivity
                val intent = Intent(this, TipDetailsActivity::class.java)
                intent.putExtra(TipDetailsActivity.EXTRA_TIP, tip)
                startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tipAdapter
    }

    // --- Mock Data Function ---
    private fun getMockTips(): List<Tip> {
        return listOf(
            Tip(1, "Proper Irrigation Techniques", "Proper irrigation is crucial for crop health. Avoid over-watering, which can lead to root rot. Water early in the morning to minimize evaporation and allow leaves to dry, which helps prevent fungal diseases.", "Irrigation"),
            Tip(2, "Choosing the Right Fertilizer", "Different crops have different nutrient requirements. A soil test can help you choose a fertilizer with the right NPK (Nitrogen, Phosphorus, Potassium) ratio for your specific soil and crop type.", "Fertilizers"),
            Tip(3, "Seasonal Crop Rotation", "Rotating crops each season helps to prevent soil depletion and reduces the buildup of pests and diseases specific to one type of crop. Never plant the same crop in the same spot for two consecutive years.", "Seasonal Advice"),
            Tip(4, "Organic Pest Control", "Use natural predators like ladybugs or lacewings, or apply neem oil sprays to manage pests without resorting to chemical pesticides. This protects your crops, beneficial insects, and the environment.", "Pesticides"),
            Tip(5, "Soil Health Management", "Incorporate compost and cover crops like clover or vetch to improve soil structure, water retention, and nutrient content over time. Healthy soil is the foundation of a healthy farm.", "Soil Health"),
            Tip(6, "Companion Planting", "Planting certain crops together can deter pests and improve growth. For example, planting marigolds around your vegetable beds can help repel nematodes and other harmful insects.", "Planting Strategy"),
            Tip(7, "Effective Weed Control", "Use a thick layer of organic mulch (like straw or wood chips) to suppress weeds and retain soil moisture. Regular, shallow cultivation can also prevent weeds from establishing themselves.", "Weed Control"),
            Tip(8, "Understanding Soil pH", "The pH of your soil affects nutrient availability. Most crops prefer a neutral pH (6.0-7.0). Use a soil test kit and amend as needed with lime (to raise pH) or sulfur (to lower pH).", "Soil Health"),
            Tip(9, "Harvesting at the Right Time", "Harvesting crops at their peak maturity ensures the best flavor, nutritional value, and storage life. Research the specific signs of ripeness for each crop you grow.", "Harvesting"),
            Tip(10, "Water Conservation", "Use drip irrigation or soaker hoses to deliver water directly to the plant's roots, minimizing waste from evaporation and runoff. This is especially important in dry climates.", "Irrigation"),
            Tip(11, "Beneficial Insects", "Attract pollinators like bees and butterflies by planting a variety of flowers. These insects are essential for the pollination of many fruit and vegetable crops.", "Pollination"),
            Tip(12, "Composting Basics", "Start a compost pile with a mix of 'green' materials (like kitchen scraps and grass clippings) and 'brown' materials (like dried leaves and small twigs) to create a nutrient-rich soil amendment for free.", "Soil Health")
        )
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
                    startActivity(Intent(this, ProductsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tips -> {
                    // Already on tips page
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
