package com.example.appctruyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appctruyn.databinding.ActivityStoryDetailBinding;
import com.example.appctruyn.model.Chapter;
import com.example.appctruyn.model.Comment;
import com.example.appctruyn.model.LibraryStory;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StoryDetailActivity extends AppCompatActivity {

    private ActivityStoryDetailBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String firstChapterId;
    private String storyId;
    private String lastChapterId;
    private int lastChapterNumber = 1;

    private String storyTitle = "";
    private String storyCoverUrl = "";
    private int totalChaptersCount = 0;

    private CommentAdapter commentAdapter;
    private final List<Comment> commentList = new ArrayList<>();

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
        setupComments();
        fetchStoryDetail(storyId);
        fetchChapters(storyId);
        loadStoryComments();
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
        
        binding.btnAddLibrary.setOnClickListener(v -> {
            if (storyId == null || storyTitle.isEmpty()) {
                Toast.makeText(this, getString(R.string.loading), Toast.LENGTH_SHORT).show();
                return;
            }
            LibraryStory libraryItem = new LibraryStory(storyId, storyTitle, storyCoverUrl, lastChapterNumber, totalChaptersCount, false);
            LibraryFragment.addBookmark(this, libraryItem);
        });

        binding.btnSendComment.setOnClickListener(v -> postStoryComment());
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
                // Ẩn/Hiện layout tương ứng với tab
                binding.layoutIntro.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                binding.layoutComments.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
                binding.layoutChapters.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupComments() {
        commentAdapter = new CommentAdapter(commentList);
        binding.rvStoryComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStoryComments.setAdapter(commentAdapter);
    }

    private void loadStoryComments() {
        // Loại bỏ orderBy để tránh lỗi Index, sẽ sắp xếp thủ công bên dưới
        db.collection("comments")
                .whereEqualTo("storyId", storyId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("StoryDetail", "Error loading comments", error);
                        Toast.makeText(this, "Lỗi tải bình luận: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        commentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            comment.setId(doc.getId());
                            commentList.add(comment);
                        }
                        
                        // Sắp xếp bình luận mới nhất lên đầu
                        Collections.sort(commentList, (c1, c2) -> {
                            if (c1.getTimestamp() == null || c2.getTimestamp() == null) return 0;
                            return c2.getTimestamp().compareTo(c1.getTimestamp());
                        });

                        commentAdapter.notifyDataSetChanged();
                        binding.tvNoComments.setVisibility(commentList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void postStoryComment() {
        String content = binding.etCommentInput.getText().toString().trim();
        if (content.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.login_to_comment), Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = user.getDisplayName();
        if (userName == null || userName.isEmpty()) userName = user.getEmail();

        Comment comment = new Comment(user.getUid(), userName, content, storyId, 0);
        
        binding.btnSendComment.setEnabled(false);
        db.collection("comments").add(comment)
                .addOnSuccessListener(documentReference -> {
                    binding.etCommentInput.setText("");
                    binding.btnSendComment.setEnabled(true);
                    Toast.makeText(this, "Đã đăng bình luận", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnSendComment.setEnabled(true);
                    Log.e("StoryDetail", "Error posting comment", e);
                    Toast.makeText(this, "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchStoryDetail(String storyId) {
        db.collection("stories").document(storyId)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                storyTitle = doc.getString("title");
                binding.tvTitle.setText(storyTitle);
                binding.tvAuthor.setText(doc.getString("author"));
                binding.tvGenre.setText(doc.getString("genre"));
                binding.tvDescription.setText(doc.getString("description"));

                Double rating = doc.getDouble("rating");
                binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating != null ? rating : 0.0));
                binding.ratingBar.setRating(rating != null ? rating.floatValue() : 0f);

                Long totalChap = doc.getLong("totalChapters");
                totalChaptersCount = totalChap != null ? totalChap.intValue() : 0;
                binding.tvChapterCountStats.setText(String.valueOf(totalChaptersCount));
                
                binding.tvViewCountStats.setText(String.valueOf(doc.getLong("views") != null ? doc.getLong("views") : 0));
                storyCoverUrl = doc.getString("coverUrl");
                Glide.with(this).load(storyCoverUrl).placeholder(R.drawable.ic_launcher_background).into(binding.ivCover);
            });
    }

    private void fetchChapters(String storyId) {
        db.collection("stories").document(storyId).collection("chapters").orderBy("number").get()
            .addOnSuccessListener(result -> {
                List<Chapter> chapters = new ArrayList<>();
                for (DocumentSnapshot doc : result.getDocuments()) {
                    Chapter chapter = doc.toObject(Chapter.class);
                    if (chapter != null) {
                        chapter.setId(doc.getId());
                        chapters.add(chapter);
                    }
                }
                if (!chapters.isEmpty()) {
                    firstChapterId = chapters.get(0).getId();
                    binding.tvChapterTitleHeader.setText(getString(R.string.chapter_count_format, chapters.size()));
                }
                setupChapterList(chapters);
            });
    }

    private void setupChapterList(List<Chapter> chapters) {
        ChapterAdapter adapter = new ChapterAdapter(chapters, chapter -> {
            Intent intent = new Intent(this, ReadChapterActivity.class);
            intent.putExtra("chapterId", chapter.getId());
            intent.putExtra("storyId", storyId);
            intent.putExtra("chapterNumber", chapter.getNumber());
            startActivity(intent);
        });
        binding.rvChapters.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChapters.setAdapter(adapter);
    }
}
