package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appctruyn.databinding.ActivityAdminManageStoriesBinding;
import com.example.appctruyn.model.Story;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class AdminManageStoriesActivity extends AppCompatActivity {

    private ActivityAdminManageStoriesBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageStoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadAllStories();
    }

    private void setupRecyclerView() {
        binding.rvAdminStories.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAllStories() {
        db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        List<Story> stories = value.toObjects(Story.class);
                        ManageStoryAdapter adapter = new ManageStoryAdapter(stories, true, new ManageStoryAdapter.OnStoryActionListener() {
                            @Override
                            public void onEdit(Story story) {
                                Intent intent = new Intent(AdminManageStoriesActivity.this, AddStoryActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }

                            @Override
                            public void onDelete(Story story) {
                                showDeleteConfirmDialog(story);
                            }

                            @Override
                            public void onClick(Story story) {
                                Intent intent = new Intent(AdminManageStoriesActivity.this, StoryDetailActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }

                            @Override
                            public void onAddChapter(Story story) {
                                Intent intent = new Intent(AdminManageStoriesActivity.this, ManageChaptersActivity.class);
                                intent.putExtra("storyId", story.getId());
                                startActivity(intent);
                            }
                        });
                        binding.rvAdminStories.setAdapter(adapter);
                    }
                });
    }

    private void showDeleteConfirmDialog(Story story) {
        new AlertDialog.Builder(this)
                .setTitle("Admin - Xóa truyện")
                .setMessage("Bạn chắc chắn muốn xóa truyện '" + story.getTitle() + "'?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("stories").document(story.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa truyện", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
