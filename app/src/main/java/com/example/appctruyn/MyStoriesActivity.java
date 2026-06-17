package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appctruyn.databinding.ActivityMyStoriesBinding;
import com.example.appctruyn.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class MyStoriesActivity extends AppCompatActivity {

    private ActivityMyStoriesBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ManageStoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyStoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadMyStories();
    }

    private void setupRecyclerView() {
        binding.rvMyStories.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadMyStories() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("stories")
                .whereEqualTo("authorId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        List<Story> stories = value.toObjects(Story.class);
                        adapter = new ManageStoryAdapter(stories, false, new ManageStoryAdapter.OnStoryActionListener() {
                            @Override
                            public void onEdit(Story story) {
                                Intent intent = new Intent(MyStoriesActivity.this, AddStoryActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }

                            @Override
                            public void onDelete(Story story) {
                                showDeleteConfirmDialog(story);
                            }

                            @Override
                            public void onClick(Story story) {
                                Intent intent = new Intent(MyStoriesActivity.this, StoryDetailActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }

                            @Override
                            public void onAddChapter(Story story) {
                                Intent intent = new Intent(MyStoriesActivity.this, AddChapterActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }
                        });
                        binding.rvMyStories.setAdapter(adapter);
                    }
                });
    }

    private void showDeleteConfirmDialog(Story story) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa truyện")
                .setMessage("Bạn có chắc chắn muốn xóa truyện '" + story.getTitle() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("stories").document(story.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa truyện", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
