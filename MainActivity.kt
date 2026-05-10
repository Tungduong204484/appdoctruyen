package com.example.appctruyn

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnSearch: ImageView
    private lateinit var btnFilter: ImageView
    private lateinit var dropdownFilter: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        bottomNav = findViewById(R.id.bottomNav)
        btnSearch = findViewById(R.id.btnSearch)
        btnFilter = findViewById(R.id.btnFilter)
        dropdownFilter = findViewById(R.id.dropdownFilter)

        // Load default fragment (Trang Khám Phá - TatCaFragment)
        if (savedInstanceState == null) {
            loadFragment(TatCaFragment())
        }

        setupTopBar()
        setupBottomNav()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupTopBar() {
        btnSearch.setOnClickListener {
            Toast.makeText(this, "Tìm kiếm truyện...", Toast.LENGTH_SHORT).show()
        }

        btnFilter.setOnClickListener {
            Toast.makeText(this, "Lọc theo thể loại", Toast.LENGTH_SHORT).show()
        }

        dropdownFilter.setOnClickListener {
            Toast.makeText(this, "Thay đổi bộ lọc giới tính", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_explore

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_library -> {
                    Toast.makeText(this, "Tủ Truyện", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_explore -> {
                    loadFragment(TatCaFragment())
                    true
                }
                R.id.nav_ranking -> {
                    loadFragment(RankingFragment())
                    true
                }
                R.id.nav_account -> {
                    Toast.makeText(this, "Tài Khoản", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // Chuyển sang trang Xếp hạng và mở tab chỉ định (ví dụ: "Đề cử")
    fun switchToRanking(targetTab: String) {
        val rankingFragment = RankingFragment().apply {
            arguments = Bundle().apply {
                putString("TARGET_TAB", targetTab)
            }
        }
        loadFragment(rankingFragment)
        bottomNav.selectedItemId = R.id.nav_ranking
    }
}
