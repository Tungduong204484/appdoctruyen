package com.example.appctruyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appctruyn.databinding.ActivityReadChapterBinding;
import com.example.appctruyn.databinding.DialogReadingSettingsBinding;
import com.example.appctruyn.databinding.LayoutTocBottomSheetBinding;
import com.example.appctruyn.model.Chapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int themeMode = 2; // 0: Light, 1: Sepia, 2: Dark (default)

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
        incrementViewCountOnce();

        loadPreferences();
        applyReadingSettings();
        setupListeners();

        if (chapterId != null) {
            loadChapterById(chapterId);
        } else {
            loadChapterByNumber(currentChapterNumber);
        }
    }

    private void incrementViewCountOnce() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentStoryId == null) return;

        String userId = user.getUid();
        DocumentReference readerRef = db.collection("stories").document(currentStoryId)
                .collection("readers").document(userId);

        readerRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                // Người dùng này lần đầu đọc truyện này
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", FieldValue.serverTimestamp());
                
                readerRef.set(data).addOnSuccessListener(aVoid -> {
                    // Tăng số lượt đọc trong tài liệu truyện
                    db.collection("stories").document(currentStoryId)
                            .update("views", FieldValue.increment(1));
                });
            }
        });
    }

    private void fetchStoryMeta(String storyId) {
        db.collection("stories").document(storyId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        storyTitle = doc.getString("title");
                        storyCoverUrl = doc.getString("coverUrl");
                        Long total = doc.getLong("totalChapters");
                        storyTotalChap = total != null ? total.intValue() : 0;
                        updateNavigationButtons();
                    }
                });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSettings.setOnClickListener(v -> showSettingsDialog());
        binding.btnPrevChapter.setOnClickListener(v -> navigateToChapter(currentChapterNumber - 1));
        binding.btnNextChapter.setOnClickListener(v -> navigateToChapter(currentChapterNumber + 1));
        
        binding.btnTOC.setOnClickListener(v -> showTOC());
        
        binding.btnShowComments.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommentsActivity.class);
            intent.putExtra("storyId", currentStoryId);
            intent.putExtra("chapterNumber", currentChapterNumber);
            startActivity(intent);
        });
    }

    private void showTOC() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LayoutTocBottomSheetBinding tocBinding = LayoutTocBottomSheetBinding.inflate(getLayoutInflater());
        dialog.setContentView(tocBinding.getRoot());

        List<Chapter> chapterList = new ArrayList<>();
        ChapterAdapter adapter = new ChapterAdapter(chapterList, chapter -> {
            loadChapterByNumber(chapter.getNumber());
            dialog.dismiss();
        });

        tocBinding.rvTOC.setLayoutManager(new LinearLayoutManager(this));
        tocBinding.rvTOC.setAdapter(adapter);

        db.collection("stories").document(currentStoryId)
                .collection("chapters")
                .orderBy("number", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Chapter chapter = doc.toObject(Chapter.class);
                        if (chapter != null) {
                            chapter.setId(doc.getId());
                            chapterList.add(chapter);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });

        dialog.show();
    }

    private void navigateToChapter(int number) {
        if (number < 1) {
            Toast.makeText(this, getString(R.string.first_chap_already), Toast.LENGTH_SHORT).show();
            return;
        }
        if (storyTotalChap > 0 && number > storyTotalChap) {
            Toast.makeText(this, getString(R.string.last_chap_already), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, getString(R.string.no_chapters_yet), Toast.LENGTH_SHORT).show();
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
            LibraryFragment.saveToHistory(this, currentStoryId, storyTitle, storyCoverUrl, chapter.getNumber(), storyTotalChap);
        }

        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        binding.btnPrevChapter.setEnabled(currentChapterNumber > 1);
        binding.btnNextChapter.setEnabled(storyTotalChap == 0 || currentChapterNumber < storyTotalChap);
        
        float alphaPrev = binding.btnPrevChapter.isEnabled() ? 1.0f : 0.5f;
        float alphaNext = binding.btnNextChapter.isEnabled() ? 1.0f : 0.5f;
        binding.btnPrevChapter.setAlpha(alphaPrev);
        binding.btnNextChapter.setAlpha(alphaNext);
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

        // Set initial values
        dialogBinding.sliderTextSize.setValue(textSize);
        dialogBinding.toggleFont.check(isSerif ? R.id.btnFontSerif : R.id.btnFontSans);
        
        int themeBtnId = R.id.btnThemeDark;
        if (themeMode == 0) themeBtnId = R.id.btnThemeLight;
        else if (themeMode == 1) themeBtnId = R.id.btnThemeSepia;
        dialogBinding.toggleTheme.check(themeBtnId);

        // Listeners
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

        dialogBinding.toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnThemeLight) themeMode = 0;
                else if (checkedId == R.id.btnThemeSepia) themeMode = 1;
                else themeMode = 2;
                applyReadingSettings();
                savePreferences();
            }
        });

        dialog.show();
    }

    private void applyReadingSettings() {
        binding.tvChapterContent.setTextSize(textSize);
        binding.tvChapterContent.setTypeface(isSerif ? Typeface.SERIF : Typeface.SANS_SERIF);

        int bgColor, textColor, surfaceColor, dividerColor;
        
        if (themeMode == 0) { // Light
            bgColor = Color.parseColor("#FFFFFF");
            surfaceColor = Color.parseColor("#F1F5F9");
            textColor = Color.parseColor("#1E293B");
            dividerColor = Color.parseColor("#E2E8F0");
        } else if (themeMode == 1) { // Sepia
            bgColor = Color.parseColor("#F4ECD8");
            surfaceColor = Color.parseColor("#E8DFCA");
            textColor = Color.parseColor("#5B4636");
            dividerColor = Color.parseColor("#D6CCB8");
        } else { // Dark
            bgColor = ContextCompat.getColor(this, R.color.background_main);
            surfaceColor = ContextCompat.getColor(this, R.color.background_surface);
            textColor = ContextCompat.getColor(this, R.color.text_primary);
            dividerColor = ContextCompat.getColor(this, R.color.divider);
        }

        binding.mainLayout.setBackgroundColor(bgColor);
        binding.nestedScrollView.setBackgroundColor(bgColor);
        binding.appBar.setBackgroundColor(surfaceColor);
        binding.toolbar.setBackgroundColor(surfaceColor);
        binding.toolbar.setTitleTextColor(textColor);
        binding.tvChapterTitle.setTextColor(textColor);
        binding.tvChapterContent.setTextColor(textColor);
        binding.bottomNav.setBackgroundColor(surfaceColor);
        
        binding.dividerTOC.setBackgroundColor(dividerColor);
        binding.dividerComments.setBackgroundColor(dividerColor);
        
        binding.btnPrevChapter.setTextColor(textColor);
        binding.btnPrevChapter.setIconTint(ColorStateList.valueOf(textColor));
        binding.btnNextChapter.setTextColor(textColor);
        binding.btnNextChapter.setIconTint(ColorStateList.valueOf(textColor));
        binding.btnTOC.setTextColor(textColor);
        binding.btnSettings.setColorFilter(textColor);
        binding.btnShowComments.setTextColor(textColor);
    }

    private void savePreferences() {
        SharedPreferences prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat("textSize", textSize)
                .putBoolean("isSerif", isSerif)
                .putInt("themeMode", themeMode)
                .apply();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE);
        textSize = prefs.getFloat("textSize", 18f);
        isSerif = prefs.getBoolean("isSerif", false);
        themeMode = prefs.getInt("themeMode", 2);
    }
}
