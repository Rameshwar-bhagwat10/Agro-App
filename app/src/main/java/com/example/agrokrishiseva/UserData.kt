package com.example.agrokrishiseva

data class UserData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val farmLocation: String = "",
    val farmSize: String = "",
    val cropTypes: List<String> = emptyList(),
    val joinDate: Long = System.currentTimeMillis(),
    val totalOrders: Int = 0,
    val tipsSaved: Int = 0,
    val profileImageUrl: String = ""
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "", "", emptyList(), 0L, 0, 0, "")
}