package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appctruyn.databinding.FragmentTatCaBinding;
import com.example.appctruyn.model.Story;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TatCaFragment extends Fragment {

    private FragmentTatCaBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int loadCount = 0;
    private static final int TOTAL_LOADS = 4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTatCaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupClickListeners();
        
        binding.shimmerView.startShimmer();
        fetchData();
    }

    private void setupRecyclerViews() {
        binding.rvDeCu.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvMoiDang.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvMoiHoanThanh.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void setupClickListeners() {
        binding.layoutHeaderDeCu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToRanking("Đề cử");
            }
        });

        binding.layoutHeaderMoiHoanThanh.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CompletedStoriesActivity.class);
            startActivity(intent);
        });
    }

    private synchronized void checkAllLoaded() {
        loadCount++;
        if (loadCount >= TOTAL_LOADS && binding != null) {
            binding.shimmerView.stopShimmer();
            binding.shimmerView.setVisibility(View.GONE);
            binding.mainContent.setVisibility(View.VISIBLE);
        }
    }

    private void fetchData() {
        loadCount = 0;

        // 1. Banner
        db.collection("stories")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        setupBanner(task.getResult().toObjects(Story.class));
                    }
                    checkAllLoaded();
                });

        // 2. Truyện đề cử
        db.collection("stories")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> stories = task.getResult().toObjects(Story.class);
                        if (binding != null) binding.rvDeCu.setAdapter(new DeCuAdapter(stories, story -> navigateToStoryDetail(story.getId())));
                    }
                    checkAllLoaded();
                });

        // 3. Mới đăng
        db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (binding != null) binding.rvMoiDang.setAdapter(new MoiDangAdapter(task.getResult().toObjects(Story.class), story -> navigateToStoryDetail(story.getId())));
                    }
                    checkAllLoaded();
                });

        // 4. Mới hoàn thành - Lọc chính xác trạng thái
        db.collection("stories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> completedList = new ArrayList<>();
                        for (Story s : task.getResult().toObjects(Story.class)) {
                            String status = s.getStatus() != null ? s.getStatus().trim() : "";
                            if (status.equalsIgnoreCase("Hoàn thành") || status.equalsIgnoreCase("Full")) {
                                completedList.add(s);
                            }
                            if (completedList.size() >= 4) break;
                        }
                        if (binding != null) {
                            binding.rvMoiHoanThanh.setAdapter(new MoiHoanThanhAdapter(completedList, story -> navigateToStoryDetail(story.getId())));
                        }
                    }
                    checkAllLoaded();
                });
    }

    private void setupBanner(List<Story> list) {
        if (list == null || list.isEmpty() || binding == null) return;
        BannerAdapter adapter = new BannerAdapter(list, story -> navigateToStoryDetail(story.getId()));
        binding.viewPagerBanner.setAdapter(adapter);
        setupIndicators(list.size());
        binding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });
        startAutoSlide(list.size());
    }

    private void setupIndicators(int size) {
        if (binding == null) return;
        binding.layoutIndicator.removeAllViews();
        for (int i = 0; i < size; i++) {
            ImageView indicator = new ImageView(requireContext());
            indicator.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);
            binding.layoutIndicator.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        if (binding == null) return;
        for (int i = 0; i < binding.layoutIndicator.getChildCount(); i++) {
            ImageView img = (ImageView) binding.layoutIndicator.getChildAt(i);
            img.setImageDrawable(ContextCompat.getDrawable(requireContext(), 
                i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive));
        }
    }

    private void startAutoSlide(int size) {
        if (bannerRunnable != null) handler.removeCallbacks(bannerRunnable);
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && size > 0) {
                    binding.viewPagerBanner.setCurrentItem((binding.viewPagerBanner.getCurrentItem() + 1) % size, true);
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(bannerRunnable, 3000);
    }

    private void navigateToStoryDetail(String storyId) {
        if (storyId == null || storyId.isEmpty()) return;
        Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
        intent.putExtra("storyId", storyId);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerRunnable != null) handler.removeCallbacks(bannerRunnable);
        binding = null;
    }
}
