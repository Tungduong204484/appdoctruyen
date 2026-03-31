package com.example.appctruyn.model

import com.google.firebase.firestore.DocumentId

data class Story(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val genre: String = "",
    val status: String = "",
    val chap: Int = 0,
    val totalChapters: Int = 0,
    val views: Int = 0,
    val rating: Float = 0f,
    val isHot: Boolean = false,
    val chapters: List<Chapter> = emptyList()
)