package com.example.appctruyn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appctruyn.auth.AuthManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnSearch: ImageView
    private lateinit var btnFilter: ImageView
    private lateinit var dropdownFilter: LinearLayout
    private lateinit var appBarLayout: AppBarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)
        btnSearch = findViewById(R.id.btnSearch)
        btnFilter = findViewById(R.id.btnFilter)
        dropdownFilter = findViewById(R.id.dropdownFilter)
        appBarLayout = findViewById(R.id.appBarLayout)

        if (savedInstanceState == null) {
            loadFragment(TatCaFragment())
            bottomNav.selectedItemId = R.id.nav_explore
        }

        setupTopBar()
        setupBottomNav()
    }

    private fun loadFragment(fragment: Fragment) {
        // Ẩn AppBar với Account và Library (chúng có header riêng)
        appBarLayout.visibility = when (fragment) {
            is AccountFragment, is LibraryFragment -> View.GONE
            else -> View.VISIBLE
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupTopBar() {
        btnSearch.setOnClickListener {
            Toast.makeText(this, getString(R.string.search_hint), Toast.LENGTH_SHORT).show()
        }

        btnFilter.setOnClickListener {
            Toast.makeText(this, getString(R.string.filter_genre), Toast.LENGTH_SHORT).show()
        }

        dropdownFilter.setOnClickListener {
            Toast.makeText(this, getString(R.string.filter_gender), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_library -> {
                    loadFragment(LibraryFragment())
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
                    loadFragment(AccountFragment())
                    true
                }
                else -> false
            }
        }
    }

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