package com.example.agrokrishiseva

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addCallback(AppDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    // Pre-populate the database with the mock data
                    val productDao = it.productDao()
                    if (productDao.getCount() == 0) {
                        productDao.insertAll(getMockProducts())
                    }
                }
            }
        }

        // --- This is the same mock data from ProductsActivity ---
        private fun getMockProducts(): List<Product> {
            return listOf(
                Product(name = "Hybrid Corn Seeds", description = "High-yield, disease-resistant corn seeds.", category = "Seeds", price = 250.00, imageResId = R.drawable.img),
                Product(name = "Urea Fertilizer", description = "46% Nitrogen content, essential for growth.", category = "Fertilizers", price = 550.50, imageResId = R.drawable.img_1),
                Product(name = "Neem Oil Pesticide", description = "Organic pest control solution.", category = "Pesticides", price = 180.99, imageResId = R.drawable.img_2),
                Product(name = "Shovel", description = "Durable steel shovel for digging.", category = "Tools", price = 350.00, imageResId = R.drawable.img_3),
                Product(name = "Wheat Seeds", description = "Quality grain seeds for Rabi season.", category = "Seeds", price = 320.00, imageResId = R.drawable.img_4),
                Product(name = "Potash Fertilizer", description = "Boosts flowering and fruiting.", category = "Fertilizers", price = 450.00, imageResId = R.drawable.img_5),
                Product(name = "Broad Spectrum Herbicide", description = "For weed control in most crops.", category = "Pesticides", price = 620.00, imageResId = R.drawable.img_6),
                Product(name = "Hand Trowel", description = "Small, durable trowel for planting and weeding.", category = "Tools", price = 150.00, imageResId = R.drawable.img_7),
                Product(name = "Organic Compost", description = "Nutrient-rich compost to improve soil fertility.", category = "Fertilizers", price = 300.00, imageResId = R.drawable.img_8),
                Product(name = "Tomato Seeds", description = "Heirloom tomato seeds for home gardens.", category = "Seeds", price = 80.00, imageResId = R.drawable.img_9),
                Product(name = "Pruning Shears", description = "Sharp shears for trimming and shaping plants.", category = "Tools", price = 220.00, imageResId = R.drawable.img_10),
                Product(name = "Vermicompost", description = "High-quality organic fertilizer from earthworms.", category = "Fertilizers", price = 180.00, imageResId = R.drawable.img_11),
                Product(name = "Spinach Seeds", description = "Easy-to-grow spinach seeds for a healthy harvest.", category = "Seeds", price = 60.00, imageResId = R.drawable.img_12)
            )
        }
    }
}
