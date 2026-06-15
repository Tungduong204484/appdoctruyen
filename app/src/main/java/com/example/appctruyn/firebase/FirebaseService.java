package com.example.appctruyn.firebase;

import com.example.appctruyn.model.Chapter;
import com.example.appctruyn.model.Story;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseService {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static Task<List<Story>> getHotStories(int limit) {
        return db.collection("stories")
                .whereEqualTo("isHot", true)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Story.class);
                    }
                    return new ArrayList<>();
                });
    }

    public static Task<List<Story>> getRecommendedStories(int limit) {
        return db.collection("stories")
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Story.class);
                    }
                    return new ArrayList<>();
                });
    }

    public static Task<List<Story>> getNewStories(int limit) {
        return db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return Tasks.forResult(task.getResult().toObjects(Story.class));
                    }
                    // Fallback if createdAt doesn't exist yet
                    return db.collection("stories").limit(limit).get()
                            .continueWith(task2 -> {
                                if (task2.isSuccessful()) {
                                    return task2.getResult().toObjects(Story.class);
                                }
                                return new ArrayList<Story>();
                            });
                });
    }

    public static Task<List<Story>> getCompletedStories(int limit) {
        return db.collection("stories")
                .whereEqualTo("status", "Full")
                .limit(limit)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return Tasks.forResult(task.getResult().toObjects(Story.class));
                    }
                    return db.collection("stories")
                            .whereEqualTo("status", "Hoàn thành")
                            .limit(limit)
                            .get()
                            .continueWithTask(task2 -> {
                                if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                    return Tasks.forResult(task2.getResult().toObjects(Story.class));
                                }
                                return db.collection("stories")
                                        .limit(limit)
                                        .get()
                                        .continueWith(task3 -> {
                                            if (task3.isSuccessful()) {
                                                return task3.getResult().toObjects(Story.class);
                                            }
                                            return new ArrayList<Story>();
                                        });
                            });
                });
    }

    public static Task<Story> getStoryDetail(String storyId) {
        return db.collection("stories").document(storyId).get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObject(Story.class);
                    }
                    return null;
                });
    }

    public static Task<List<Story>> getRanking(int limit) {
        return db.collection("stories")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Story.class);
                    }
                    return new ArrayList<>();
                });
    }

    public static Task<List<Chapter>> getChapters(String storyId) {
        return db.collection("stories")
                .document(storyId)
                .collection("chapters")
                .orderBy("number")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(Chapter.class);
                    }
                    return new ArrayList<>();
                });
    }
}
