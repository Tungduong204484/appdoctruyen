package com.example.appctruyn.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "reader", // "reader" | "author" | "admin"
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
