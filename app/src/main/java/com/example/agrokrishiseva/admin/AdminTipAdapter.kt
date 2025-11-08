package com.example.agrokrishiseva.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrokrishiseva.R
import com.example.agrokrishiseva.Tip

class AdminTipAdapter(
    private val tips: List<Tip>,
    private val onEditClick: (Tip) -> Unit,
    private val onDeleteClick: (Tip) -> Unit
) : RecyclerView.Adapter<AdminTipAdapter.TipViewHolder>() {

    class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTipTitle: TextView = itemView.findViewById(R.id.tv_tip_title)
        val tvTipCategory: TextView = itemView.findViewById(R.id.tv_tip_category)
        val tvTipContent: TextView = itemView.findViewById(R.id.tv_tip_content)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        
        holder.tvTipTitle.text = tip.title
        holder.tvTipCategory.text = tip.category
        
        // Show truncated content
        val maxLength = 100
        holder.tvTipContent.text = if (tip.content.length > maxLength) {
            "${tip.content.substring(0, maxLength)}..."
        } else {
            tip.content
        }
        
        holder.btnEdit.setOnClickListener {
            onEditClick(tip)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(tip)
        }
    }

    override fun getItemCount(): Int = tips.size
}