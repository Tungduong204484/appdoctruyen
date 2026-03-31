package com.example.appctruyn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RankingFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private val tabs = arrayOf("Đề cử", "Bình luận", "Lượt đọc")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)
        tabLayout = view.findViewById(R.id.tabLayoutRanking)
        viewPager = view.findViewById(R.id.viewPagerRanking)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabs.size
            override fun createFragment(position: Int): Fragment {
                return RankingTabFragment.newInstance(tabs[position])
            }
        }

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        // Xử lý nhảy đến tab cụ thể khi được yêu cầu từ Fragment khác
        val targetTab = arguments?.getString("TARGET_TAB")
        targetTab?.let {
            val index = tabs.indexOf(it)
            if (index != -1) {
                // Sử dụng post để đảm bảo ViewPager đã thiết lập adapter xong
                viewPager.post {
                    viewPager.setCurrentItem(index, false)
                }
            }
        }

        return view
    }
}
