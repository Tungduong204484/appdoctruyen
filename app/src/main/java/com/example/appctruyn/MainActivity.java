package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.appctruyn.auth.AuthManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ImageView btnSearch;
    private ImageView btnFilter;
    private LinearLayout dropdownFilter;
    private AppBarLayout appBarLayout;
    private TextView tvCurrentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Ánh xạ View
        bottomNav = findViewById(R.id.bottomNav);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
        dropdownFilter = findViewById(R.id.dropdownFilter);
        appBarLayout = findViewById(R.id.appBarLayout);
        tvCurrentFilter = findViewById(R.id.tvCurrentFilter);

        if (savedInstanceState == null) {
            loadFragment(new TatCaFragment());
            bottomNav.setSelectedItemId(R.id.nav_explore);
        }

        setupTopBar();
        setupBottomNav();
    }

    private void loadFragment(Fragment fragment) {
        if (fragment instanceof AccountFragment || fragment instanceof LibraryFragment) {
            appBarLayout.setVisibility(View.GONE);
        } else {
            appBarLayout.setVisibility(View.VISIBLE);
            // Cập nhật tiêu đề dựa trên fragment (tùy chọn)
            if (fragment instanceof TatCaFragment) {
                tvCurrentFilter.setText("Khám phá");
            } else if (fragment instanceof RankingFragment) {
                tvCurrentFilter.setText("Xếp hạng");
            }
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupTopBar() {
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Đảm bảo icon bộ lọc (phễu) hoạt động
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
                filterBottomSheet.setOnFilterApplyListener(filters -> {
                    // Xử lý dữ liệu lọc ở đây
                    String sort = filters.getString("sort", "");
                    Toast.makeText(this, "Đang lọc: " + sort, Toast.LENGTH_SHORT).show();
                });
                filterBottomSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
            });
        }

        dropdownFilter.setOnClickListener(v -> 
            Toast.makeText(this, "Tính năng thay đổi bộ lọc nhanh", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_library) {
                loadFragment(new LibraryFragment());
                return true;
            } else if (itemId == R.id.nav_explore) {
                loadFragment(new TatCaFragment());
                return true;
            } else if (itemId == R.id.nav_ranking) {
                loadFragment(new RankingFragment());
                return true;
            } else if (itemId == R.id.nav_account) {
                loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });
    }

    public void switchToRanking(String targetTab) {
        RankingFragment rankingFragment = new RankingFragment();
        Bundle args = new Bundle();
        args.putString("TARGET_TAB", targetTab);
        rankingFragment.setArguments(args);
        loadFragment(rankingFragment);
        bottomNav.setSelectedItemId(R.id.nav_ranking);
    }
}
