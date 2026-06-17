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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddChapterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyId = getIntent().getStringExtra("storyId");
        if (storyId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveChapter());
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
                    // Tự động tăng số lượng chương ở document Truyện gốc
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
}
