package com.example.appctruyn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.appctruyn.databinding.FragmentTatCaBinding
import com.example.appctruyn.model.Story
import com.google.firebase.firestore.FirebaseFirestore

class TatCaFragment : Fragment() {

    private var _binding: FragmentTatCaBinding? = null
    private val binding get() = _binding

    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTatCaBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupClickListeners()
        fetchData()
    }

    private fun setupRecyclerViews() {
        binding?.apply {
            rvDeCu.layoutManager = GridLayoutManager(requireContext(), 3)
            rvMoiDang.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvMoiHoanThanh.layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupClickListeners() {
        binding?.layoutHeaderDeCu?.setOnClickListener {
            (activity as? MainActivity)?.switchToRanking("Đề cử")
        }
    }

    private fun fetchData() {
        // Banner
        db.collection("stories")
            .whereEqualTo("isHot", true)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(Story::class.java)
                if (_binding != null && list.isNotEmpty()) {
                    setupBanner(list)
                }
            }

        // Đề cử
        db.collection("stories")
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                val stories = documents.toObjects(Story::class.java)
                if (_binding != null && stories.isNotEmpty()) {
                    binding?.rvDeCu?.adapter = DeCuAdapter(stories) { story ->
                        navigateToStoryDetail(story.id)
                    }
                }
            }

        // Mới đăng
        db.collection("stories")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val stories = documents.toObjects(Story::class.java)
                if (_binding != null && stories.isNotEmpty()) {
                    binding?.rvMoiDang?.adapter = MoiDangAdapter(stories) { story ->
                        navigateToStoryDetail(story.id)
                    }
                }
            }

        // Mới hoàn thành
        db.collection("stories")
            .whereEqualTo("status", "Full")
            .limit(4)
            .get()
            .addOnSuccessListener { documents ->
                var list = documents.toObjects(Story::class.java)
                if (_binding == null) return@addOnSuccessListener

                if (list.isEmpty()) {
                    db.collection("stories")
                        .whereEqualTo("status", "Hoàn thành")
                        .limit(4)
                        .get()
                        .addOnSuccessListener { docs ->
                            if (_binding == null) return@addOnSuccessListener
                            list = docs.toObjects(Story::class.java)
                            if (list.isNotEmpty()) {
                                binding?.rvMoiHoanThanh?.adapter = MoiHoanThanhAdapter(list) { story ->
                                    navigateToStoryDetail(story.id)
                                }
                            } else {
                                db.collection("stories").limit(4).get().addOnSuccessListener { docs2 ->
                                    if (_binding == null) return@addOnSuccessListener
                                    val fallbackList = docs2.toObjects(Story::class.java)
                                    binding?.rvMoiHoanThanh?.adapter = MoiHoanThanhAdapter(fallbackList) { story ->
                                        navigateToStoryDetail(story.id)
                                    }
                                }
                            }
                        }
                } else {
                    binding?.rvMoiHoanThanh?.adapter = MoiHoanThanhAdapter(list) { story ->
                        navigateToStoryDetail(story.id)
                    }
                }
            }
    }

    private fun setupBanner(list: List<Story>) {
        val adapter = BannerAdapter(list) { story ->
            navigateToStoryDetail(story.id)
        }
        binding?.viewPagerBanner?.adapter = adapter

        if (list.isNotEmpty()) {
            setupIndicators(list.size)
            binding?.viewPagerBanner?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicators(position)
                }
            })
            startAutoSlide(list.size)
        }
    }

    private fun setupIndicators(size: Int) {
        binding?.layoutIndicator?.removeAllViews()
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }
        for (i in 0 until size) {
            val indicator = ImageView(requireContext()).apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive))
                layoutParams = params
            }
            binding?.layoutIndicator?.addView(indicator)
        }
    }

    private fun updateIndicators(position: Int) {
        binding?.layoutIndicator?.let { layout ->
            for (i in 0 until layout.childCount) {
                val imageView = layout.getChildAt(i) as ImageView
                if (i == position) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active))
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive))
                }
            }
        }
    }

    private fun startAutoSlide(size: Int) {
        bannerRunnable?.let { handler.removeCallbacks(it) }
        bannerRunnable = Runnable {
            binding?.viewPagerBanner?.let { viewPager ->
                var currentItem = viewPager.currentItem
                currentItem = (currentItem + 1) % size
                viewPager.currentItem = currentItem
                handler.postDelayed(bannerRunnable!!, 3000)
            }
        }
        handler.postDelayed(bannerRunnable!!, 3000)
    }

    private fun navigateToStoryDetail(storyId: String?) {
        if (!storyId.isNullOrEmpty()) {
            val intent = Intent(requireContext(), StoryDetailActivity::class.java)
            intent.putExtra("storyId", storyId)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.story_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
