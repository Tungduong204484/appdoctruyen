package com.example.appctruyn.model

import com.google.firebase.firestore.DocumentId

data class Story(
    @DocumentId var id: String = "",
    var title: String = "",
    var author: String = "",
    var description: String = "",
    var coverUrl: String = "",
    var genre: String = "",
    var status: String = "",
    var chap: Int = 0,
    var totalChapters: Int = 0,
    var views: Int = 0,
    var rating: Float = 0f,
    var isHot: Boolean = false,
    var chapters: List<Chapter> = emptyList()
)