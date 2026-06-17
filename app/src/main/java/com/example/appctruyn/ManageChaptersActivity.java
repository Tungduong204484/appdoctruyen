package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appctruyn.databinding.ActivityManageChaptersBinding;
import com.example.appctruyn.model.Chapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ManageChaptersActivity extends AppCompatActivity {

    private ActivityManageChaptersBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String storyId;
    private ManageChapterAdapter adapter;
    private final List<Chapter> chapterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageChaptersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyId = getIntent().getStringExtra("storyId");
        if (storyId == null) {
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        setupRecyclerView();
        loadChapters();

        binding.fabAddChapter.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddChapterActivity.class);
            intent.putExtra("storyId", storyId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new ManageChapterAdapter(chapterList, new ManageChapterAdapter.OnChapterActionListener() {
            @Override
            public void onEdit(Chapter chapter) {
                Intent intent = new Intent(ManageChaptersActivity.this, AddChapterActivity.class);
                intent.putExtra("storyId", storyId);
                intent.putExtra("chapterId", chapter.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Chapter chapter) {
                showDeleteConfirmDialog(chapter);
            }
        });
        binding.rvManageChapters.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageChapters.setAdapter(adapter);
    }

    private void loadChapters() {
        db.collection("stories").document(storyId)
                .collection("chapters")
                .orderBy("number", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        chapterList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Chapter chapter = doc.toObject(Chapter.class);
                            if (chapter != null) {
                                chapter.setId(doc.getId());
                                chapterList.add(chapter);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showDeleteConfirmDialog(Chapter chapter) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa chương")
                .setMessage("Bạn có chắc muốn xóa " + (chapter.getTitle().isEmpty() ? "Chương " + chapter.getNumber() : chapter.getTitle()) + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("stories").document(storyId)
                            .collection("chapters").document(chapter.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                db.collection("stories").document(storyId)
                                        .update("totalChapters", FieldValue.increment(-1));
                                Toast.makeText(this, "Đã xóa chương", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
