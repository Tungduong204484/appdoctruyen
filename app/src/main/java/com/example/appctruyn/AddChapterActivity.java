package com.example.appctruyn;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appctruyn.databinding.ActivityAddChapterBinding;
import com.example.appctruyn.model.Chapter;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

public class AddChapterActivity extends AppCompatActivity {

    private ActivityAddChapterBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String storyId;
    private String chapterId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddChapterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyId = getIntent().getStringExtra("storyId");
        chapterId = getIntent().getStringExtra("chapterId");
        
        if (storyId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (chapterId != null) {
            isEditMode = true;
            binding.toolbar.setTitle("Sửa chương");
            binding.btnSave.setText("Cập nhật");
            loadChapterData();
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveChapter());
    }

    private void loadChapterData() {
        db.collection("stories").document(storyId)
                .collection("chapters").document(chapterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Chapter chapter = documentSnapshot.toObject(Chapter.class);
                    if (chapter != null) {
                        binding.etChapterNumber.setText(String.valueOf(chapter.getNumber()));
                        binding.etChapterTitle.setText(chapter.getTitle());
                        binding.etContent.setText(chapter.getContent());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu chương", Toast.LENGTH_SHORT).show());
    }

    private void saveChapter() {
        String numStr = binding.etChapterNumber.getText().toString().trim();
        String title = binding.etChapterTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        if (numStr.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số chương và nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        int chapterNum;
        try {
            chapterNum = Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số chương không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);

        if (isEditMode) {
            updateChapter(chapterNum, title, content);
        } else {
            addNewChapter(chapterNum, title, content);
        }
    }

    private void addNewChapter(int chapterNum, String title, String content) {
        Chapter chapter = new Chapter();
        chapter.setStoryId(storyId);
        chapter.setChapterNumber(chapterNum);
        chapter.setNumber(chapterNum);
        chapter.setTitle(title.isEmpty() ? "Chương " + chapterNum : title);
        chapter.setContent(content);
        chapter.setTimestamp(String.valueOf(new Date().getTime()));

        db.collection("stories").document(storyId)
                .collection("chapters")
                .add(chapter)
                .addOnSuccessListener(documentReference -> {
                    db.collection("stories").document(storyId)
                            .update("totalChapters", FieldValue.increment(1));

                    Toast.makeText(this, "Đăng chương thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChapter(int chapterNum, String title, String content) {
        db.collection("stories").document(storyId)
                .collection("chapters").document(chapterId)
                .update(
                        "number", chapterNum,
                        "chapterNumber", chapterNum,
                        "title", title.isEmpty() ? "Chương " + chapterNum : title,
                        "content", content,
                        "timestamp", String.valueOf(new Date().getTime())
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật chương thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
