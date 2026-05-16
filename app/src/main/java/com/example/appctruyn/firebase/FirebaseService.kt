package com.example.appctruyn.firebase

import com.example.appctruyn.model.Chapter
import com.example.appctruyn.model.Story
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseService {
    private val db = Firebase.firestore

    suspend fun getHotStories(limit: Int = 5): List<Story> {
        return try {
            db.collection("stories")
                .whereEqualTo("isHot", true)
                .limit(limit.toLong())
                .get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getRecommendedStories(limit: Int = 6): List<Story> {
        return try {
            db.collection("stories")
                .limit(limit.toLong())
                .get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getNewStories(limit: Int = 10): List<Story> {
        return try {
            db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) {
            // Fallback if createdAt doesn't exist yet
            try {
                db.collection("stories").limit(limit.toLong()).get().await().toObjects(Story::class.java)
            } catch (e2: Exception) { emptyList() }
        }
    }

    suspend fun getCompletedStories(limit: Int = 4): List<Story> {
        return try {
            var result = db.collection("stories")
                .whereEqualTo("status", "Full")
                .limit(limit.toLong())
                .get().await()
                .toObjects(Story::class.java)
            
            if (result.isEmpty()) {
                result = db.collection("stories")
                    .whereEqualTo("status", "Hoàn thành")
                    .limit(limit.toLong())
                    .get().await()
                    .toObjects(Story::class.java)
            }
            
            if (result.isEmpty()) {
                result = db.collection("stories")
                    .limit(limit.toLong())
                    .get().await()
                    .toObjects(Story::class.java)
            }
            result
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getStoryDetail(storyId: String): Story? {
        return try {
            val doc = db.collection("stories").document(storyId).get().await()
            doc.toObject(Story::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun getRanking(limit: Int = 20): List<Story> {
        return try {
            db.collection("stories")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()
                .toObjects(Story::class.java)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getChapters(storyId: String): List<Chapter> {
        return try {
            db.collection("stories")
                .document(storyId)
                .collection("chapters")
                .orderBy("number")
                .get().await()
                .toObjects(Chapter::class.java)
        } catch (e: Exception) { emptyList() }
    }
}