package com.example.agrokrishiseva

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TipAdapter(
    private var tips: List<Tip>,
    private val onBookmarkClick: (Tip) -> Unit,
    private val onItemClick: (Tip) -> Unit
) : RecyclerView.Adapter<TipAdapter.TipViewHolder>() {

    inner class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tip_title)
        val contentSnippetTextView: TextView = itemView.findViewById(R.id.tip_content_snippet)
        val bookmarkButton: ImageButton = itemView.findViewById(R.id.btn_bookmark)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(tips[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.titleTextView.text = tip.title
        holder.contentSnippetTextView.text = tip.content

        // Set the correct bookmark icon
        val bookmarkIcon = if (tip.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
        holder.bookmarkButton.setImageResource(bookmarkIcon)

        // Handle bookmark click
        holder.bookmarkButton.setOnClickListener {
            onBookmarkClick(tip)
        }
    }

    override fun getItemCount() = tips.size

    // Function to update the tip list
    fun updateTips(newTips: List<Tip>) {
        tips = newTips
        notifyDataSetChanged()
    }
}
