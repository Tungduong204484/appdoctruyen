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
import android.widget.Toast;

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

import java.util.List;

public class TatCaFragment extends Fragment {

    private FragmentTatCaBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int loadCount = 0;
    private final int TOTAL_LOADS = 4;

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
        
        // Bắt đầu Shimmer
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
    }

    private void checkAllLoaded() {
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
                .whereEqualTo("isHot", true)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> list = task.getResult().toObjects(Story.class);
                        if (binding != null && !list.isEmpty()) {
                            setupBanner(list);
                        }
                    }
                    checkAllLoaded();
                });

        // 2. Đề cử
        db.collection("stories")
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> stories = task.getResult().toObjects(Story.class);
                        if (binding != null && !stories.isEmpty()) {
                            binding.rvDeCu.setAdapter(new DeCuAdapter(stories, story -> navigateToStoryDetail(story.getId())));
                        }
                    }
                    checkAllLoaded();
                });

        // 3. Mới đăng
        db.collection("stories")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> stories = task.getResult().toObjects(Story.class);
                        if (binding != null && !stories.isEmpty()) {
                            binding.rvMoiDang.setAdapter(new MoiDangAdapter(stories, story -> navigateToStoryDetail(story.getId())));
                        }
                    }
                    checkAllLoaded();
                });

        // 4. Mới hoàn thành
        db.collection("stories")
                .whereEqualTo("status", "Full")
                .limit(4)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Story> list = task.getResult().toObjects(Story.class);
                        if (binding != null && !list.isEmpty()) {
                            binding.rvMoiHoanThanh.setAdapter(new MoiHoanThanhAdapter(list, story -> navigateToStoryDetail(story.getId())));
                        }
                    }
                    checkAllLoaded();
                });
    }

    private void setupBanner(List<Story> list) {
        BannerAdapter adapter = new BannerAdapter(list, story -> navigateToStoryDetail(story.getId()));
        binding.viewPagerBanner.setAdapter(adapter);

        if (!list.isEmpty()) {
            setupIndicators(list.size());
            binding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateIndicators(position);
                }
            });
            startAutoSlide(list.size());
        }
    }

    private void setupIndicators(int size) {
        binding.layoutIndicator.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < size; i++) {
            ImageView indicator = new ImageView(requireContext());
            indicator.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
            indicator.setLayoutParams(params);
            binding.layoutIndicator.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        if (binding == null) return;
        LinearLayout layout = binding.layoutIndicator;
        for (int i = 0; i < layout.getChildCount(); i++) {
            ImageView imageView = (ImageView) layout.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
            }
        }
    }

    private void startAutoSlide(int size) {
        if (bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
        }
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null) {
                    int currentItem = binding.viewPagerBanner.getCurrentItem();
                    currentItem = (currentItem + 1) % size;
                    binding.viewPagerBanner.setCurrentItem(currentItem);
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(bannerRunnable, 3000);
    }

    private void navigateToStoryDetail(String storyId) {
        if (storyId != null && !storyId.isEmpty()) {
            Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
            intent.putExtra("storyId", storyId);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), getString(R.string.story_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
        }
        binding = null;
    }
}
