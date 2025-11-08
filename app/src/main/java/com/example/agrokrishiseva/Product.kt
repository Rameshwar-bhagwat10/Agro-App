package com.example.agrokrishiseva

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageResId: Int = 0,
    val firestoreId: String = "" // For Firestore document ID
) : Parcelable {
    // No-argument constructor for Firestore
    constructor() : this(0, "", "", "", 0.0, 0, "")
}
