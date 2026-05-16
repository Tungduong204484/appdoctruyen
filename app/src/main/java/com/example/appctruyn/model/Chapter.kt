package com.example.appctruyn.model

import com.google.firebase.firestore.DocumentId

data class Chapter(
    @DocumentId var id: String = "",
    val title: String = "",
    val number: Int = 0,
    val chapterNumber: Int = 0,
    val content: String = "",
    var storyId: String = "",
    val timestamp: String = ""
)