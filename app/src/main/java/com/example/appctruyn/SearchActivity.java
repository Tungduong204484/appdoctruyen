package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appctruyn.databinding.ActivitySearchBinding;
import com.example.appctruyn.model.Story;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StoryAdapter adapter;
    private final List<Story> searchResults = new ArrayList<>();
    private Bundle activeFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activeFilters = getIntent().getExtras();

        setupRecyclerView();
        setupListeners();

        if (activeFilters != null && !activeFilters.isEmpty()) {
            performFilteredSearch();
        } else {
            binding.tvEmptyState.setText("Nhập tên truyện hoặc sử dụng bộ lọc");
        }
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
                activeFilters = null;
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
                    activeFilters = null;
                    performSearch(s.toString().trim());
                } else if (s.length() == 0 && activeFilters == null) {
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String queryText) {
        if (queryText.isEmpty()) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);

        // Tìm kiếm theo tên (Firestore bắt đầu bằng...)
        db.collection("stories")
                .orderBy("title")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff")
                .limit(50)
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        searchResults.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Story story = document.toObject(Story.class);
                            story.setId(document.getId());
                            searchResults.add(story);
                        }
                        adapter.notifyDataSetChanged();
                        if (searchResults.isEmpty()) {
                            binding.tvEmptyState.setVisibility(View.VISIBLE);
                            binding.tvEmptyState.setText("Không tìm thấy truyện nào");
                        }
                    }
                });
    }

    private void performFilteredSearch() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);

        db.collection("stories")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> allStories = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Story s = doc.toObject(Story.class);
                            s.setId(doc.getId());
                            if (isMatchFilter(s)) {
                                allStories.add(s);
                            }
                        }
                        
                        sortStories(allStories);

                        searchResults.clear();
                        searchResults.addAll(allStories);
                        adapter.notifyDataSetChanged();

                        if (searchResults.isEmpty()) {
                            binding.tvEmptyState.setVisibility(View.VISIBLE);
                            binding.tvEmptyState.setText("Không có truyện nào khớp với bộ lọc");
                        }
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isMatchFilter(Story story) {
        if (activeFilters == null) return true;

        // 1. Tình trạng
        String filterStatus = activeFilters.getString("status");
        if (!TextUtils.isEmpty(filterStatus)) {
            String s = story.getStatus() != null ? story.getStatus() : "";
            if (filterStatus.equals("Còn tiếp")) {
                if (!s.equals("Đang ra") && !s.equals("Còn tiếp")) return false;
            } else if (filterStatus.equals("Hoàn thành")) {
                if (!s.equals("Hoàn thành") && !s.equalsIgnoreCase("Full")) return false;
            } else if (filterStatus.equals("Tạm dừng")) {
                if (!s.equals("Tạm dừng")) return false;
            }
        }

        // 2. Thể loại
        ArrayList<String> filterGenres = activeFilters.getStringArrayList("genres");
        if (filterGenres != null && !filterGenres.isEmpty()) {
            String storyGenre = story.getGenre() != null ? story.getGenre() : "";
            boolean hasGenre = false;
            for (String g : filterGenres) {
                if (storyGenre.toLowerCase().contains(g.toLowerCase())) {
                    hasGenre = true;
                    break;
                }
            }
            if (!hasGenre) return false;
        }

        // 3. Số chương
        String filterChapters = activeFilters.getString("chapters");
        if (!TextUtils.isEmpty(filterChapters)) {
            int count = story.getTotalChapters();
            if (filterChapters.equals("< 300")) {
                if (count >= 300) return false;
            } else if (filterChapters.equals("300-600")) {
                if (count < 300 || count > 600) return false;
            } else if (filterChapters.equals("600-1000")) {
                if (count < 600 || count > 1000) return false;
            } else if (filterChapters.equals("> 1000")) {
                if (count <= 1000) return false;
            }
        }

        // 4. Thời gian đăng
        String filterDate = activeFilters.getString("publishDate");
        if (!TextUtils.isEmpty(filterDate) && !"Tất cả".equals(filterDate)) {
            if (story.getCreatedAt() == null) return false;
            long diff = System.currentTimeMillis() - story.getCreatedAt().getTime();
            long days = diff / (24 * 60 * 60 * 1000);
            if (filterDate.equals("Hôm nay") && days > 1) return false;
            if (filterDate.equals("3 ngày") && days > 3) return false;
            if (filterDate.equals("7 ngày") && days > 7) return false;
            if (filterDate.equals("30 ngày") && days > 30) return false;
        }

        return true;
    }

    private void sortStories(List<Story> list) {
        String sort = activeFilters != null ? activeFilters.getString("sort") : "";
        if (TextUtils.isEmpty(sort)) {
            // Mặc định sắp xếp theo thời gian mới nhất
            Collections.sort(list, (o1, o2) -> {
                if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            });
            return;
        }

        Collections.sort(list, (o1, o2) -> {
            switch (sort) {
                case "Lượt đọc":
                    return Integer.compare(o2.getViews(), o1.getViews());
                case "Điểm đánh giá":
                    return Float.compare(o2.getRating(), o1.getRating());
                case "Số chương":
                    return Integer.compare(o2.getTotalChapters(), o1.getTotalChapters());
                case "Tên truyện":
                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                case "Mới đăng":
                default:
                    if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        });
    }
}
