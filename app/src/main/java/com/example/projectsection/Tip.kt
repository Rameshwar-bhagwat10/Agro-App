package com.example.projectsection

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tip(
    val id: Int,
    val title: String,
    val content: String,
    val category: String, // e.g., "Irrigation", "Seasonal Advice"
    var isBookmarked: Boolean = false
) : Parcelable
