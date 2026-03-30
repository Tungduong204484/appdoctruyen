package com.example.appctruyn;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ImageView btnSearch;
    private ImageView btnFilter;
    private LinearLayout dropdownFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupTopBar();
        setupBottomNav();
    }

    private void setupTopBar() {
        btnSearch      = findViewById(R.id.btnSearch);
        btnFilter      = findViewById(R.id.btnFilter);
        dropdownFilter = findViewById(R.id.dropdownFilter);

        // Nút tìm kiếm
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Tìm kiếm truyện...", Toast.LENGTH_SHORT).show();
            // TODO tuần 3: mở SearchActivity
        });

        // Nút lọc
        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Lọc theo thể loại", Toast.LENGTH_SHORT).show();
            // TODO tuần 3: mở BottomSheetDialog
        });

        // Dropdown "Tất cả"
        dropdownFilter.setOnClickListener(v -> showGenderDropdown());
    }

    private void showGenderDropdown() {
        String[] options = {"Tất cả", "Nam", "Nữ"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) ->
                        Toast.makeText(this, "Lọc: " + options[which], Toast.LENGTH_SHORT).show())
                .show();
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);

        // Mặc định chọn tab Khám Phá
        bottomNav.setSelectedItemId(R.id.nav_explore);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_library) {
                Toast.makeText(this, "Tủ Truyện", Toast.LENGTH_SHORT).show();
                // TODO tuần 2: loadFragment(new LibraryFragment());
                return true;
            } else if (id == R.id.nav_explore) {
                Toast.makeText(this, "Khám Phá", Toast.LENGTH_SHORT).show();
                // TODO tuần 2: loadFragment(new ExploreFragment());
                return true;
            } else if (id == R.id.nav_ranking) {
                Toast.makeText(this, "Xếp Hạng", Toast.LENGTH_SHORT).show();
                // TODO tuần 3: loadFragment(new RankingFragment());
                return true;
            } else if (id == R.id.nav_account) {
                Toast.makeText(this, "Tài Khoản", Toast.LENGTH_SHORT).show();
                // TODO tuần 4: loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });
    }
}