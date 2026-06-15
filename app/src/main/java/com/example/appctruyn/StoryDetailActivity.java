package com.example.appctruyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appctruyn.databinding.ActivityStoryDetailBinding;
import com.example.appctruyn.model.Chapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoryDetailActivity extends AppCompatActivity {

    private ActivityStoryDetailBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String firstChapterId;
    private String storyId;
    private String lastChapterId;
    private int lastChapterNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyId = getIntent().getStringExtra("storyId");
        if (storyId == null || storyId.isEmpty()) {
            Toast.makeText(this, getString(R.string.story_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupTabs();
        fetchStoryDetail(storyId);
        fetchChapters(storyId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReadButtonState();
    }

    private void updateReadButtonState() {
        SharedPreferences prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE);
        lastChapterId = prefs.getString("last_chapter_id_" + storyId, null);
        lastChapterNumber = prefs.getInt("last_chapter_number_" + storyId, 1);

        if (lastChapterId != null) {
            binding.btnRead.setText(getString(R.string.btn_read_continue, lastChapterNumber));
        } else {
            binding.btnRead.setText(getString(R.string.btn_read_now));
        }
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnRead.setOnClickListener(v -> {
            String targetChapterId = lastChapterId != null ? lastChapterId : firstChapterId;
            int targetChapterNumber = lastChapterId != null ? lastChapterNumber : 1;

            if (targetChapterId != null) {
                Intent intent = new Intent(this, ReadChapterActivity.class);
                intent.putExtra("chapterId", targetChapterId);
                intent.putExtra("storyId", storyId);
                intent.putExtra("chapterNumber", targetChapterNumber);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.err_no_chapters), Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnAddLibrary.setOnClickListener(v -> 
            Toast.makeText(this, getString(R.string.added_to_library), Toast.LENGTH_SHORT).show()
        );
    }

    private void setupTabs() {
        String[] tabTitles = {
            getString(R.string.tab_intro),
            getString(R.string.tab_reviews),
            getString(R.string.tab_comments),
            getString(R.string.tab_chapters)
        };
        for (String title : tabTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) { // Giới Thiệu
                    binding.layoutIntro.setVisibility(View.VISIBLE);
                    binding.layoutChapters.setVisibility(View.GONE);
                } else if (position == 3) { // D.S Chương
                    binding.layoutIntro.setVisibility(View.GONE);
                    binding.layoutChapters.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutIntro.setVisibility(View.GONE);
                    binding.layoutChapters.setVisibility(View.GONE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchStoryDetail(String storyId) {
        db.collection("stories").document(storyId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, getString(R.string.story_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String title = doc.getString("title");
                binding.tvTitle.setText(title != null ? title : getString(R.string.no_title));
                
                String author = doc.getString("author");
                binding.tvAuthor.setText(author != null ? author : getString(R.string.no_author));
                
                String genre = doc.getString("genre");
                binding.tvGenre.setText(genre != null ? genre : getString(R.string.no_genre));
                
                String description = doc.getString("description");
                binding.tvDescription.setText(description != null ? description : getString(R.string.no_description));

                Double rating = doc.getDouble("rating");
                if (rating == null) rating = 0.0;
                binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
                binding.ratingBar.setRating(rating.floatValue());

                Long totalChap = doc.getLong("totalChapters");
                binding.tvChapterCountStats.setText(totalChap != null ? totalChap.toString() : "0");
                
                String status = doc.getString("status");
                if (status == null) status = getString(R.string.status_ongoing);
                binding.tvStatusStats.setText(getString(R.string.status_prefix, status));
                
                Long views = doc.getLong("views");
                binding.tvViewCountStats.setText(views != null ? views.toString() : "0");

                Glide.with(this)
                    .load(doc.getString("coverUrl"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.ivCover);
            });
    }

    private void fetchChapters(String storyId) {
        db.collection("stories").document(storyId)
            .collection("chapters")
            .orderBy("number")
            .get()
            .addOnSuccessListener(result -> {
                List<Chapter> chapters = new ArrayList<>();
                for (DocumentSnapshot doc : result.getDocuments()) {
                    Chapter chapter = doc.toObject(Chapter.class);
                    if (chapter != null) {
                        chapter.setId(doc.getId());
                        chapter.setStoryId(storyId);
                        chapters.add(chapter);
                    }
                }

                if (!chapters.isEmpty()) {
                    firstChapterId = chapters.get(0).getId();
                    binding.tvChapterTitleHeader.setText(getString(R.string.chapter_count_format, chapters.size()));
                    binding.tvChapterCountStats.setText(String.valueOf(chapters.size()));
                }

                setupChapterList(chapters);
            })
            .addOnFailureListener(e -> {
                db.collection("stories").document(storyId)
                    .collection("chapters")
                    .get()
                    .addOnSuccessListener(result -> {
                         List<Chapter> chapters = new ArrayList<>();
                         for (DocumentSnapshot doc : result.getDocuments()) {
                            Long numLong = doc.getLong("number");
                            if (numLong == null) numLong = doc.getLong("chapterNumber");
                            int number = numLong != null ? numLong.intValue() : 0;
                            
                            Chapter chapter = new Chapter(
                                doc.getId(),
                                doc.getString("title") != null ? doc.getString("title") : "",
                                number,
                                number,
                                doc.getString("content") != null ? doc.getString("content") : "",
                                storyId,
                                doc.getString("timestamp") != null ? doc.getString("timestamp") : ""
                            );
                            chapters.add(chapter);
                        }
                        if (!chapters.isEmpty()) {
                            firstChapterId = chapters.get(0).getId();
                            binding.tvChapterTitleHeader.setText(getString(R.string.chapter_count_format, chapters.size()));
                        }
                        setupChapterList(chapters);
                    });
            });
    }

    private void setupChapterList(List<Chapter> chapters) {
        ChapterAdapter adapter = new ChapterAdapter(chapters, chapter -> {
            Intent intent = new Intent(this, ReadChapterActivity.class);
            intent.putExtra("chapterId", chapter.getId());
            intent.putExtra("storyId", chapter.getStoryId());
            intent.putExtra("chapterNumber", chapter.getNumber());
            startActivity(intent);
        });
        binding.rvChapters.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChapters.setAdapter(adapter);
    }
}
