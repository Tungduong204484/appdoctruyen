package com.example.appctruyn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RankingFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private final String[] tabs = {"Đề cử", "Bình luận", "Lượt đọc"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);
        tabLayout = view.findViewById(R.id.tabLayoutRanking);
        viewPager = view.findViewById(R.id.viewPagerRanking);

        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return RankingTabFragment.newInstance(tabs[position]);
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        };

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabs[position])).attach();

        // Xử lý nhảy đến tab cụ thể khi được yêu cầu từ Fragment khác
        if (getArguments() != null) {
            String targetTab = getArguments().getString("TARGET_TAB");
            if (targetTab != null) {
                int index = -1;
                for (int i = 0; i < tabs.length; i++) {
                    if (tabs[i].equals(targetTab)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    final int finalIndex = index;
                    // Sử dụng post để đảm bảo ViewPager đã thiết lập adapter xong
                    viewPager.post(() -> viewPager.setCurrentItem(finalIndex, false));
                }
            }
        }

        return view;
    }
}
