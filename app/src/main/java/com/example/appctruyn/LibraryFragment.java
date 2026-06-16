package com.example.appctruyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.model.LibraryStory;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LibraryFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView rvLibrary;
    private TextView tvEmpty;
    private LibraryAdapter libraryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayoutLibrary);
        rvLibrary = view.findViewById(R.id.rvLibrary);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvLibrary.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupTabs();
        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload khi quay lại fragment để cập nhật lịch sử mới nhất
        int currentTab = tabLayout.getSelectedTabPosition();
        if (currentTab == 0) loadHistory(); else loadBookmarks();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.library_history)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.library_bookmarks)));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) loadHistory(); else loadBookmarks();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadHistory() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE);
        String historyJson = prefs.getString("history_list", "[]");

        List<LibraryStory> historyItems = parseHistoryJson(historyJson);

        if (historyItems.isEmpty()) {
            showEmpty(getString(R.string.library_empty_history));
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        rvLibrary.setVisibility(View.VISIBLE);

        libraryAdapter = new LibraryAdapter(
            historyItems,
            item -> {
                Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
                intent.putExtra("storyId", item.getStoryId());
                startActivity(intent);
            },
            (item, anchor) -> showStoryMenu(item)
        );
        rvLibrary.setAdapter(libraryAdapter);
    }

    private void loadBookmarks() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE);
        String bookmarkJson = prefs.getString("bookmark_list", "[]");
        List<LibraryStory> bookmarks = parseHistoryJson(bookmarkJson);

        if (bookmarks.isEmpty()) {
            showEmpty(getString(R.string.library_empty_bookmarks));
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        rvLibrary.setVisibility(View.VISIBLE);

        libraryAdapter = new LibraryAdapter(
            bookmarks,
            item -> {
                Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
                intent.putExtra("storyId", item.getStoryId());
                startActivity(intent);
            },
            (item, anchor) -> showStoryMenu(item)
        );
        rvLibrary.setAdapter(libraryAdapter);
    }

    private void showEmpty(String msg) {
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
        rvLibrary.setVisibility(View.GONE);
    }

    private void showStoryMenu(LibraryStory item) {
        LibraryBottomSheet sheet = LibraryBottomSheet.newInstance(item);
        sheet.setOnDeleteClickListener(() -> {
            removeFromHistory(item.getStoryId());
            loadHistory();
        });
        sheet.setOnBookmarkToggleListener(isBookmarked -> {
            if (isBookmarked) {
                addBookmark(getContext(), item);
            } else {
                removeBookmark(item.getStoryId());
            }
        });
        sheet.show(getChildFragmentManager(), "LibraryMenu");
    }

    private void removeFromHistory(String storyId) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE);
        List<LibraryStory> list = new ArrayList<>(parseHistoryJson(prefs.getString("history_list", "[]")));
        list.removeIf(it -> it.getStoryId().equals(storyId));
        prefs.edit().putString("history_list", toHistoryJson(list)).apply();
        Toast.makeText(requireContext(), getString(R.string.library_removed), Toast.LENGTH_SHORT).show();
    }

    private void addBookmarkInternal(LibraryStory item) {
        addBookmark(getContext(), item);
    }

    private void removeBookmark(String storyId) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE);
        List<LibraryStory> list = new ArrayList<>(parseHistoryJson(prefs.getString("bookmark_list", "[]")));
        list.removeIf(it -> it.getStoryId().equals(storyId));
        prefs.edit().putString("bookmark_list", toHistoryJson(list)).apply();
    }

    // JSON helpers
    public static List<LibraryStory> parseHistoryJson(String json) {
        try {
            List<LibraryStory> result = new ArrayList<>();
            if (json == null || json.equals("[]") || json.trim().isEmpty()) return result;
            String trimmed = json.trim();
            if (trimmed.startsWith("[")) trimmed = trimmed.substring(1);
            if (trimmed.endsWith("]")) trimmed = trimmed.substring(0, trimmed.length() - 1);
            
            List<String> items = splitJsonObjects(trimmed);
            for (String item : items) {
                String storyId = extractField(item, "storyId");
                String title = extractField(item, "title");
                String coverUrl = extractField(item, "coverUrl");
                String lastChapStr = extractField(item, "lastChap");
                String totalChapStr = extractField(item, "totalChap");
                int lastChap = 0;
                int totalChap = 0;
                try {
                    lastChap = Integer.parseInt(lastChapStr);
                    totalChap = Integer.parseInt(totalChapStr);
                } catch (NumberFormatException ignored) {}
                
                boolean notifyEnabled = "true".equals(extractField(item, "notifyEnabled"));
                if (!storyId.isEmpty()) {
                    result.add(new LibraryStory(storyId, title, coverUrl, lastChap, totalChap, notifyEnabled));
                }
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String extractField(String json, String key) {
        String regex = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"|\"" + key + "\"\\s*:\\s*([^,}]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String val1 = matcher.group(1);
            if (val1 != null && !val1.isEmpty()) return val1;
            String val2 = matcher.group(2);
            if (val2 != null) return val2.trim();
        }
        return "";
    }

    private static List<String> splitJsonObjects(String s) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    result.add(s.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    public static String toHistoryJson(List<LibraryStory> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            LibraryStory item = list.get(i);
            sb.append("{")
                .append("\"storyId\":\"").append(item.getStoryId()).append("\",")
                .append("\"title\":\"").append(item.getTitle()).append("\",")
                .append("\"coverUrl\":\"").append(item.getCoverUrl()).append("\",")
                .append("\"lastChap\":").append(item.getLastChap()).append(",")
                .append("\"totalChap\":").append(item.getTotalChap()).append(",")
                .append("\"notifyEnabled\":").append(item.isNotifyEnabled())
                .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Lưu vào Lịch sử đọc
     */
    public static void saveToHistory(Context context, String storyId, String title, String coverUrl, int lastChap, int totalChap) {
        SharedPreferences prefs = context.getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE);
        List<LibraryStory> list = new ArrayList<>(parseHistoryJson(prefs.getString("history_list", "[]")));

        // Xóa nếu đã có rồi (để đưa lên đầu)
        list.removeIf(it -> it.getStoryId().equals(storyId));

        // Thêm vào đầu danh sách
        list.add(0, new LibraryStory(storyId, title, coverUrl, lastChap, totalChap, false));

        // Giới hạn 100 truyện
        if (list.size() > 100) {
            list = list.subList(0, 100);
        }
        prefs.edit().putString("history_list", toHistoryJson(list)).apply();
    }

    /**
     * Thêm vào Đánh dấu (Tủ truyện)
     */
    public static void addBookmark(Context context, LibraryStory item) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE);
        List<LibraryStory> list = new ArrayList<>(parseHistoryJson(prefs.getString("bookmark_list", "[]")));
        
        boolean alreadyExists = false;
        for (LibraryStory s : list) {
            if (s.getStoryId().equals(item.getStoryId())) {
                alreadyExists = true;
                break;
            }
        }
        
        if (!alreadyExists) {
            list.add(0, item);
            prefs.edit().putString("bookmark_list", toHistoryJson(list)).apply();
            Toast.makeText(context, context.getString(R.string.library_added_bookmark), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Truyện đã có trong tủ", Toast.LENGTH_SHORT).show();
        }
    }
}
