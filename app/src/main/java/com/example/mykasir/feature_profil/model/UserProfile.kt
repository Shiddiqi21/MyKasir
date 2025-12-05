package com.example.mykasir.feature_profil.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val profileImage: String? = null,
    val joinDate: String = ""
)
