package com.example.appctruyn.model

import com.google.firebase.firestore.DocumentId

data class Chapter(
    @DocumentId var id: String = "",
    var storyId: String = "",
    var title: String = "",
    var number: Int = 0,
    var content: String = "",
    var timestamp: String = "",
    var images: List<String> = emptyList()
)