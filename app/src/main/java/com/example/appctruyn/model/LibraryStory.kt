package com.example.appctruyn.model

data class LibraryStory(
    val storyId: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val lastChap: Int = 0,
    val totalChap: Int = 0,
    val notifyEnabled: Boolean = false
)