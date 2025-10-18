package com.example.agrokrishiseva

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var products: List<Product>, // Changed to var
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Inner class for holding the views in the item layout
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.product_name)
        val priceTextView: TextView = itemView.findViewById(R.id.product_price)
        val categoryTextView: TextView = itemView.findViewById(R.id.product_category)
        val imageView: ImageView = itemView.findViewById(R.id.product_image)
        val addToCartButton: ImageButton = itemView.findViewById(R.id.btn_add_to_cart)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(products[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.nameTextView.text = product.name
        holder.priceTextView.text = "₹${String.format("%.2f", product.price)}" // Changed to Rupee symbol
        holder.categoryTextView.text = product.category
        holder.imageView.setImageResource(product.imageResId) // Set the image

        holder.addToCartButton.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "${product.name} added to cart",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = products.size

    // Function to update the product list
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged() // Refreshes the RecyclerView
    }
}
