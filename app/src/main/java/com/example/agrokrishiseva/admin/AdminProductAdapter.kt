package com.example.agrokrishiseva.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrokrishiseva.Product
import com.example.agrokrishiseva.R

class AdminProductAdapter(
    private val products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductCategory: TextView = itemView.findViewById(R.id.tv_product_category)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvProductDescription: TextView = itemView.findViewById(R.id.tv_product_description)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        
        holder.ivProductImage.setImageResource(product.imageResId)
        holder.tvProductName.text = product.name
        holder.tvProductCategory.text = product.category
        holder.tvProductPrice.text = "₹${product.price}"
        holder.tvProductDescription.text = product.description
        
        holder.btnEdit.setOnClickListener {
            onEditClick(product)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int = products.size
}