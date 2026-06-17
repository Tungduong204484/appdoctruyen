package com.example.appctruyn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView rvLibrary;
    private TextView tvEmpty;
    private LibraryAdapter libraryAdapter;
    private final List<LibraryStory> displayList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        libraryAdapter = new LibraryAdapter(displayList, 
            item -> {
                Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
                intent.putExtra("storyId", item.getStoryId());
                startActivity(intent);
            },
            (item, anchor) -> showStoryMenu(item)
        );
        rvLibrary.setAdapter(libraryAdapter);

        setupTabs();
        loadDataFromFirestore(true); // Mặc định load Lịch sử
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.library_history)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.library_bookmarks)));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadDataFromFirestore(tab.getPosition() == 0);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadDataFromFirestore(boolean isHistory) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showEmpty(getString(R.string.login_hint));
            return;
        }

        String collectionName = isHistory ? "history" : "bookmarks";
        db.collection("users").document(user.getUid())
                .collection(collectionName)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("LibrarySync", "Error loading library", error);
                        return;
                    }
                    if (value != null) {
                        displayList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            LibraryStory item = doc.toObject(LibraryStory.class);
                            displayList.add(item);
                        }
                        
                        if (displayList.isEmpty()) {
                            showEmpty(isHistory ? getString(R.string.library_empty_history) : getString(R.string.library_empty_bookmarks));
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvLibrary.setVisibility(View.VISIBLE);
                            libraryAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void showEmpty(String msg) {
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
        rvLibrary.setVisibility(View.GONE);
    }

    private void showStoryMenu(LibraryStory item) {
        LibraryBottomSheet sheet = LibraryBottomSheet.newInstance(item);
        sheet.setOnDeleteClickListener(() -> removeFromHistory(item.getStoryId()));
        sheet.setOnBookmarkToggleListener(isBookmarked -> {
            if (isBookmarked) addBookmark(getContext(), item);
            else removeBookmark(item.getStoryId());
        });
        sheet.show(getChildFragmentManager(), "LibraryMenu");
    }

    private void removeFromHistory(String storyId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .collection("history").document(storyId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), getString(R.string.library_removed), Toast.LENGTH_SHORT).show());
    }

    private void removeBookmark(String storyId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .collection("bookmarks").document(storyId)
                .delete();
    }

    /**
     * Lưu vào Lịch sử đọc lên Firebase
     */
    public static void saveToHistory(Context context, String storyId, String title, String coverUrl, int lastChap, int totalChap) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("storyId", storyId);
        data.put("title", title);
        data.put("coverUrl", coverUrl);
        data.put("lastChap", lastChap);
        data.put("totalChap", totalChap);
        data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("history").document(storyId)
                .set(data);
    }

    /**
     * Thêm vào Đánh dấu (Tủ truyện) lên Firebase
     */
    public static void addBookmark(Context context, LibraryStory item) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("storyId", item.getStoryId());
        data.put("title", item.getTitle());
        data.put("coverUrl", item.getCoverUrl());
        data.put("lastChap", item.getLastChap());
        data.put("totalChap", item.getTotalChap());
        data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("bookmarks").document(item.getStoryId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    if (context != null) Toast.makeText(context, context.getString(R.string.library_added_bookmark), Toast.LENGTH_SHORT).show();
                });
    }
}
