package com.example.appctruyn;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appctruyn.databinding.ActivityReadChapterBinding;
import com.example.appctruyn.databinding.DialogReadingSettingsBinding;
import com.example.appctruyn.model.Chapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReadChapterActivity extends AppCompatActivity {

    private ActivityReadChapterBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentStoryId;
    private int currentChapterNumber = 0;

    private String storyTitle = "";
    private String storyCoverUrl = "";
    private int storyTotalChap = 0;

    private float textSize = 18f;
    private boolean isSerif = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadChapterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentStoryId = getIntent().getStringExtra("storyId");
        currentChapterNumber = getIntent().getIntExtra("chapterNumber", 0);
        String chapterId = getIntent().getStringExtra("chapterId");

        if (currentStoryId == null || (chapterId == null && currentChapterNumber == 0)) {
            Toast.makeText(this, getString(R.string.story_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchStoryMeta(currentStoryId);

        loadPreferences();
        applyReadingSettings();
        setupListeners();

        if (chapterId != null) {
            loadChapterById(chapterId);
        } else {
            loadChapterByNumber(currentChapterNumber);
        }
    }

    private void fetchStoryMeta(String storyId) {
        db.collection("stories").document(storyId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        storyTitle = doc.getString("title");
                        storyCoverUrl = doc.getString("coverUrl");
                        Long total = doc.getLong("totalChapters");
                        storyTotalChap = total != null ? total.intValue() : 0;
                    }
                });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSettings.setOnClickListener(v -> showSettingsDialog());

        binding.btnPrevChapter.setOnClickListener(v -> navigateToChapter(currentChapterNumber - 1));

        binding.btnNextChapter.setOnClickListener(v -> navigateToChapter(currentChapterNumber + 1));
    }

    private void navigateToChapter(int number) {
        if (number < 1) {
            Toast.makeText(this, "Đây là chương đầu tiên", Toast.LENGTH_SHORT).show();
            return;
        }
        loadChapterByNumber(number);
    }

    private void loadChapterByNumber(int number) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (currentStoryId != null) {
            db.collection("stories").document(currentStoryId)
                    .collection("chapters")
                    .whereEqualTo("number", number)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        if (!snapshots.isEmpty()) {
                            DocumentSnapshot doc = snapshots.getDocuments().get(0);
                            Chapter chapter = doc.toObject(Chapter.class);
                            if (chapter != null) {
                                chapter.setId(doc.getId());
                                chapter.setStoryId(currentStoryId);
                                displayChapter(chapter);
                            }
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Không tìm thấy chương này", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadChapterById(String chapterId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (currentStoryId != null) {
            db.collection("stories").document(currentStoryId)
                    .collection("chapters").document(chapterId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Chapter chapter = doc.toObject(Chapter.class);
                        if (chapter != null) {
                            chapter.setId(doc.getId());
                            chapter.setStoryId(currentStoryId);
                            displayChapter(chapter);
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void displayChapter(Chapter chapter) {
        binding.progressBar.setVisibility(View.GONE);
        currentChapterNumber = chapter.getNumber();

        String title = getString(R.string.chapter_title_format, chapter.getNumber(), chapter.getTitle());
        binding.toolbar.setTitle(title);
        binding.tvChapterTitle.setText(title);
        
        String content = chapter.getContent();
        binding.tvChapterContent.setText((content == null || content.isEmpty()) ? getString(R.string.no_content) : content);

        binding.nestedScrollView.smoothScrollTo(0, 0);

        if (currentStoryId != null) {
            saveLastReadChapter(currentStoryId, chapter.getId(), chapter.getNumber());

            LibraryFragment.saveToHistory(
                    this,
                    currentStoryId,
                    storyTitle,
                    storyCoverUrl,
                    chapter.getNumber(),
                    storyTotalChap
            );
        }

        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        binding.btnPrevChapter.setEnabled(currentChapterNumber > 1);
        binding.btnNextChapter.setEnabled(true);
    }

    private void saveLastReadChapter(String storyId, String chapterId, int chapterNumber) {
        SharedPreferences prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("last_chapter_id_" + storyId, chapterId)
                .putInt("last_chapter_number_" + storyId, chapterNumber)
                .apply();
    }

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogReadingSettingsBinding dialogBinding = DialogReadingSettingsBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.sliderTextSize.setValue(textSize);
        dialogBinding.toggleFont.check(isSerif ? R.id.btnFontSerif : R.id.btnFontSans);

        dialogBinding.sliderTextSize.addOnChangeListener((slider, value, fromUser) -> {
            textSize = value;
            applyReadingSettings();
            savePreferences();
        });

        dialogBinding.toggleFont.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isSerif = checkedId == R.id.btnFontSerif;
                applyReadingSettings();
                savePreferences();
            }
        });

        dialog.show();
    }

    private void applyReadingSettings() {
        binding.tvChapterContent.setTextSize(textSize);
        binding.tvChapterContent.setTypeface(isSerif ? Typeface.SERIF : Typeface.SANS_SERIF);
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences("ReadingSettings", Context.MODE_PRIVATE);
        textSize = prefs.getFloat("text_size", 19f);
        isSerif = prefs.getBoolean("is_serif", false);
    }

    private void savePreferences() {
        SharedPreferences prefs = getSharedPreferences("ReadingSettings", Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat("text_size", textSize)
                .putBoolean("is_serif", isSerif)
                .apply();
    }
}
