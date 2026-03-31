package com.example.appctruyn.model

import com.google.firebase.firestore.DocumentId

data class Chapter(
    val title: String = "",
    val number: Int = 0,
    val content: String = "",
    val images: List<String> = emptyList()
)