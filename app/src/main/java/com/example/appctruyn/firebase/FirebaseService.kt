package com.example.appctruyn.firebase

import com.example.appctruyn.model.Chapter
import com.example.appctruyn.model.Story
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseService {
    private val db = Firebase.firestore

    suspend fun getStories(): List<Story> {
        return try {
            db.collection("stories").get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getStoriesByGenre(genre: String): List<Story> {
        return try {
            db.collection("stories")
                .whereEqualTo("genre", genre)
                .get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getRanking(): List<Story> {
        return try {
            db.collection("stories")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(20)
                .get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getChapters(storyId: String): List<Chapter> {
        return try {
            db.collection("stories")
                .document(storyId)
                .collection("chapters")
                .orderBy("chapterNumber")
                .get().await()
                .toObjects(Chapter::class.java)
        } catch (e: Exception) { emptyList() }
    }
}