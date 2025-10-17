package com.example.projectsection

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProductDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT = "extra_product"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val product = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_PRODUCT, Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Product>(EXTRA_PRODUCT)
        }

        if (product != null) {
            val nameTextView: TextView = findViewById(R.id.detail_product_name)
            val priceTextView: TextView = findViewById(R.id.detail_product_price)
            val descriptionTextView: TextView = findViewById(R.id.detail_product_description)
            val imageView: ImageView = findViewById(R.id.detail_product_image)

            nameTextView.text = product.name
            priceTextView.text = "₹${String.format("%.2f", product.price)}"
            descriptionTextView.text = product.description
            imageView.setImageResource(product.imageResId)
        }
    }
}
