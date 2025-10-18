package com.example.agrokrishiseva

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TipDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TIP = "extra_tip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_details)

        val tip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TIP, Tip::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Tip>(EXTRA_TIP)
        }

        if (tip != null) {
            val titleTextView: TextView = findViewById(R.id.detail_tip_title)
            val categoryTextView: TextView = findViewById(R.id.detail_tip_category)
            val contentTextView: TextView = findViewById(R.id.detail_tip_content)

            titleTextView.text = tip.title
            categoryTextView.text = tip.category
            contentTextView.text = tip.content
        }
    }
}
