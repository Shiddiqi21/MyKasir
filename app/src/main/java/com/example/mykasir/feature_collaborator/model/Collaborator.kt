package com.example.mykasir.feature_collaborator.model

data class Collaborator(
    val id: Int,
    val email: String,
    val name: String,
    val role: String = "cashier",
    val createdAt: String? = null
)

data class CollaboratorRequest(
    val email: String,
    val password: String,
    val name: String
)
