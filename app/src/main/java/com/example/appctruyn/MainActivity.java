package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.appctruyn.auth.AuthManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ImageView btnSearch;
    private ImageView btnFilter;
    private LinearLayout dropdownFilter;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
        dropdownFilter = findViewById(R.id.dropdownFilter);
        appBarLayout = findViewById(R.id.appBarLayout);

        if (savedInstanceState == null) {
            loadFragment(new TatCaFragment());
            bottomNav.setSelectedItemId(R.id.nav_explore);
        }

        setupTopBar();
        setupBottomNav();
    }

    private void loadFragment(Fragment fragment) {
        // Ẩn AppBar với Account và Library
        if (fragment instanceof AccountFragment || fragment instanceof LibraryFragment) {
            appBarLayout.setVisibility(View.GONE);
        } else {
            appBarLayout.setVisibility(View.VISIBLE);
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

        btnFilter.setOnClickListener(v -> 
            Toast.makeText(this, getString(R.string.filter_genre), Toast.LENGTH_SHORT).show()
        );

        dropdownFilter.setOnClickListener(v -> 
            Toast.makeText(this, getString(R.string.filter_gender), Toast.LENGTH_SHORT).show()
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
