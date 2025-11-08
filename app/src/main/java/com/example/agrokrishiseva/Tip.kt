package com.example.agrokrishiseva

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tip(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val category: String = "", // e.g., "Irrigation", "Seasonal Advice"
    var isBookmarked: Boolean = false
) : Parcelable {
    // No-argument constructor for Firestore
    constructor() : this(0, "", "", "", false)
}
