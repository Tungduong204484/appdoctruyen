package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.model.Story;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CompletedStoriesActivity extends AppCompatActivity {

    private RecyclerView rvCompletedStories;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_stories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rvCompletedStories = findViewById(R.id.rvCompletedStories);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvCompletedStories.setLayoutManager(new LinearLayoutManager(this));

        fetchCompletedStories();
    }

    private void fetchCompletedStories() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        db.collection("stories")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> completedList = new ArrayList<>();
                        int total = task.getResult().size();
                        
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Story s = doc.toObject(Story.class);
                            String status = s.getStatus() != null ? s.getStatus().trim() : "";
                            if (status.equalsIgnoreCase("Hoàn thành") || status.equalsIgnoreCase("Full")) {
                                completedList.add(s);
                            }
                        }

                        if (completedList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            String msg = "Không tìm thấy truyện 'Hoàn thành' hoặc 'Full'.\n(Tổng số truyện trong DB: " + total + ")";
                            tvEmpty.setText(msg);
                        } else {
                            setupRecyclerView(completedList);
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView(List<Story> stories) {
        StoryAdapter adapter = new StoryAdapter(stories, story -> {
            Intent intent = new Intent(CompletedStoriesActivity.this, StoryDetailActivity.class);
            intent.putExtra("storyId", story.getId());
            startActivity(intent);
        });
        rvCompletedStories.setAdapter(adapter);
    }
}
