package com.example.appctruyn;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appctruyn.databinding.ActivityAddStoryBinding;
import com.example.appctruyn.model.Story;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStoryActivity extends AppCompatActivity {

    private ActivityAddStoryBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String storyId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        storyId = getIntent().getStringExtra("storyId");
        if (storyId != null) {
            binding.toolbar.setTitle("Chỉnh sửa truyện");
            loadStoryData();
        }

        binding.btnSave.setOnClickListener(v -> saveStory());
    }

    private void loadStoryData() {
        db.collection("stories").document(storyId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Story story = doc.toObject(Story.class);
                if (story != null) {
                    binding.etTitle.setText(story.getTitle());
                    binding.etAuthor.setText(story.getAuthor());
                    binding.etDescription.setText(story.getDescription());
                    binding.etCoverUrl.setText(story.getCoverUrl());
                    setSelectedGenres(story.getGenre());
                    setSelectedStatus(story.getStatus());
                }
            }
        });
    }

    private void setSelectedStatus(String status) {
        if (status == null) return;
        if (status.equals("Đang ra")) binding.chipOngoing.setChecked(true);
        else if (status.equals("Hoàn thành")) binding.chipCompleted.setChecked(true);
        else if (status.equals("Tạm dừng")) binding.chipPaused.setChecked(true);
    }

    private String getSelectedStatus() {
        if (binding.chipOngoing.isChecked()) return "Đang ra";
        if (binding.chipCompleted.isChecked()) return "Hoàn thành";
        if (binding.chipPaused.isChecked()) return "Tạm dừng";
        return "Đang ra";
    }

    private void setSelectedGenres(String genreString) {
        if (genreString == null || genreString.isEmpty()) return;
        String[] genres = genreString.split(", ");
        for (int i = 0; i < binding.cgGenre.getChildCount(); i++) {
            View child = binding.cgGenre.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                for (String g : genres) {
                    if (chip.getText().toString().trim().equalsIgnoreCase(g.trim())) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    private String getSelectedGenres() {
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < binding.cgGenre.getChildCount(); i++) {
            View child = binding.cgGenre.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    if (genres.length() > 0) genres.append(", ");
                    genres.append(chip.getText().toString());
                }
            }
        }
        return genres.toString();
    }

    private void saveStory() {
        String title = binding.etTitle.getText().toString().trim();
        String authorName = binding.etAuthor.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String coverUrl = binding.etCoverUrl.getText().toString().trim();
        String genre = getSelectedGenres();
        String status = getSelectedStatus();

        if (title.isEmpty() || authorName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên truyện và bút danh", Toast.LENGTH_SHORT).show();
            return;
        }

        if (genre.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một thể loại", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        binding.btnSave.setEnabled(false);

        if (storyId == null) {
            // Thêm mới
            Story story = new Story();
            story.setTitle(title);
            story.setAuthor(authorName);
            story.setAuthorId(user.getUid());
            story.setAuthorEmail(user.getEmail());
            story.setDescription(description);
            story.setCoverUrl(coverUrl);
            story.setGenre(genre);
            story.setStatus(status);

            db.collection("stories").add(story)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đăng truyện thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Cập nhật
            db.collection("stories").document(storyId)
                    .update("title", title,
                            "author", authorName,
                            "description", description,
                            "coverUrl", coverUrl,
                            "genre", genre,
                            "status", status)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
