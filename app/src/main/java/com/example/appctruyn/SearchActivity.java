package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appctruyn.databinding.ActivitySearchBinding;
import com.example.appctruyn.model.Story;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StoryAdapter adapter;
    private final List<Story> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new StoryAdapter(searchResults, story -> {
            Intent intent = new Intent(this, StoryDetailActivity.class);
            intent.putExtra("storyId", story.getId());
            startActivity(intent);
        });
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    performSearch(s.toString().trim());
                } else if (s.length() == 0) {
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setText("Nhập tên truyện để tìm kiếm");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);

        // Firebase Firestore doesn't support full-text search natively with 'contains'.
        // This is a simple prefix search work-around. For better search, Algolia is recommended.
        db.collection("stories")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        searchResults.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Story story = document.toObject(Story.class);
                            story.setId(document.getId());
                            searchResults.add(story);
                        }
                        adapter.notifyDataSetChanged();

                        if (searchResults.isEmpty()) {
                            binding.tvEmptyState.setVisibility(View.VISIBLE);
                            binding.tvEmptyState.setText("Không tìm thấy kết quả nào");
                        }
                    }
                });
    }
}
